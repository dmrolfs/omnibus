package omnibus.akka.testkit

import scala.concurrent.Future
import java.util.concurrent.atomic.AtomicInteger

import akka.actor.ActorSystem
import akka.dispatch.Dispatchers
import akka.testkit.TestEvent.Mute
import akka.testkit.{ DeadLettersFilter, TestKit }
import cats.syntax.either._
import com.typesafe.config.Config
import org.scalatest.{ fixture, Outcome, ParallelTestExecution }
import com.github.ghik.silencer.silent
import journal._
import omnibus.core.syntax.clazz._

object ParallelAkkaSpec {
  private[testkit] val testPosition: AtomicInteger = new AtomicInteger()
}

trait ParallelAkkaSpec extends fixture.WordSpec with ParallelTestExecution { outer =>
  private val log = Logger[ParallelAkkaSpec]

  @silent def slugForTest( test: OneArgTest ): String = {
    s"Par-${getClass.safeSimpleName}-${ParallelAkkaSpec.testPosition.incrementAndGet()}"
  }

  @silent def systemForTest(
    test: OneArgTest,
    slug: String,
    config: Option[Config] = None
  ): ActorSystem = {
    log.debug( s"creating system[${slug}] for test:[${test.name}]" )

    ActorSystem(
      name = slug,
      config = config,
      classLoader = None,
      defaultExecutionContext = None
    )
  }

  @silent def configurationForTest( test: OneArgTest, slug: String ): Option[Config] = None

  def contextForTest( test: OneArgTest ): ( String, Option[Config], ActorSystem ) = {
    val slug = slugForTest( test )
    val config = configurationForTest( test, slug )
    val system = systemForTest( test, slug, config )
    ( slug, config, system )
  }

  type Fixture <: AkkaFixture
  type FixtureParam = Fixture

  def createAkkaFixture( test: OneArgTest, system: ActorSystem, slug: String ): Fixture

  class AkkaFixture(
    val slug: String,
    _system: ActorSystem,
    val config: Option[Config] = None
  ) extends TestKit( _system ) {
    @silent def before( test: OneArgTest ): Unit = {}
    @silent def after( test: OneArgTest ): Unit = {}

    def spawn( dispatcherId: String = Dispatchers.DefaultDispatcherId )( body: => Unit ): Unit = {
      Future { body }( system.dispatchers lookup dispatcherId )
    }

    def muteDeadLetters(
      messagesClasses: Class[_]*
    )( implicit sys: ActorSystem = system ): Unit = {
      if (!sys.log.isDebugEnabled) {
        def mute( clazz: Class[_] ): Unit = {
          sys.eventStream.publish(
            Mute( DeadLettersFilter( clazz )( occurrences = Int.MaxValue ) )
          )
        }

        if (messagesClasses.isEmpty) mute( classOf[AnyRef] )
        else messagesClasses foreach mute
      }
    }
  }

  override protected def withFixture( test: OneArgTest ): Outcome = {
    val slug = slugForTest( test )
    val config = configurationForTest( test, slug )
    val system = systemForTest( test, slug, config )

    Either
      .catchNonFatal { createAkkaFixture( test, system, slug ) }
      .map { f =>
        log.debug( ".......... before test .........." )
        f before test
        log.debug( "++++++++++ starting test ++++++++++" )
        ( test( f ), f )
      }
      .map {
        case ( outcome, f ) =>
          log.debug( "---------- finished test ------------" )
          f after test
          log.debug( ".......... after test .........." )

          Option( f.system ) foreach { s ⇒
            log.debug( s"terminating actor-system:${s.name}..." )
            f.shutdown( actorSystem = s, verifySystemShutdown = false )
            log.debug( s"actor-system:${s.name}.terminated" )
          }

          outcome
      }
      .valueOr { ex =>
        log.error( s"test[${test.name}] failed", ex )
        system.terminate()
        throw ex
      }
  }
}
