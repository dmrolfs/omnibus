package omnibus.akka.persistence.query

import akka.NotUsed
import akka.actor.ActorSystem
import akka.persistence.cassandra.journal.CassandraJournal
import akka.persistence.cassandra.query.scaladsl.CassandraReadJournal
import akka.persistence.query.journal.leveldb.scaladsl.LeveldbReadJournal
import akka.persistence.query.{EventEnvelope, Offset, PersistenceQuery}
import akka.persistence.query.scaladsl._
import akka.stream.scaladsl.Source
import com.typesafe.config.{ConfigObject, ConfigValueType}
import com.typesafe.scalalogging.LazyLogging


/**
  * Created by rolfsd on 2/15/17.
  */
object QueryJournal extends LazyLogging {
  def fromSystem( system: ActorSystem ): Journal = {
    journalFQN( system ) match {
      case fqn if fqn == classOf[CassandraJournal].getName ⇒ {
        logger.info( "cassandra journal recognized" )
        PersistenceQuery( system ).readJournalFor[CassandraReadJournal]( CassandraReadJournal.Identifier )
      }

      case fqn if fqn == "akka.persistence.journal.leveldb.LeveldbJournal" ⇒ {
        logger.info( "leveldb journal recognized" )
        PersistenceQuery( system ).readJournalFor[LeveldbReadJournal]( LeveldbReadJournal.Identifier )
      }

      case fqn ⇒ {
        logger.warn( "journal FQN not recognized - creating empty read journal:[{}]", fqn )
        QueryJournal.empty
      }
    }
  }

  type Journal = ReadJournal
    with PersistenceIdsQuery
    with CurrentPersistenceIdsQuery
    with EventsByPersistenceIdQuery
    with CurrentEventsByPersistenceIdQuery
    with EventsByTagQuery
    with CurrentEventsByTagQuery


  object empty extends ReadJournal
                       with PersistenceIdsQuery
                       with CurrentPersistenceIdsQuery
                       with EventsByPersistenceIdQuery
                       with CurrentEventsByPersistenceIdQuery
                       with EventsByTagQuery
                       with CurrentEventsByTagQuery {
    override def persistenceIds(): Source[String, NotUsed] = Source.empty[String]

    override def currentPersistenceIds(): Source[String, NotUsed] = Source.empty[String]

    override def eventsByPersistenceId(
      persistenceId: String,
      fromSequenceNr: Long,
      toSequenceNr: Long
    ): Source[EventEnvelope, NotUsed] = Source.empty[EventEnvelope]

    override def currentEventsByPersistenceId(
      persistenceId: String,
      fromSequenceNr: Long,
      toSequenceNr: Long
    ): Source[EventEnvelope, NotUsed] = Source.empty[EventEnvelope]

    override def eventsByTag( tag: String, offset: Offset ): Source[EventEnvelope, NotUsed] = Source.empty[EventEnvelope]

    override def currentEventsByTag( tag: String, offset: Offset ): Source[EventEnvelope, NotUsed] = {
      Source.empty[EventEnvelope]
    }
  }


  private def journalFQN( system: ActorSystem ): String = {
    import shapeless.syntax.typeable._

    val JournalPluginPath = "akka.persistence.journal.plugin"
    val config = system.settings.config

    if ( config.hasPath( JournalPluginPath ) ) {
      val jplugin = config.getValue( JournalPluginPath )
      jplugin.valueType match {
        case ConfigValueType.STRING ⇒ {
          val fqn = {
            jplugin.unwrapped.cast[String]
            .map { path ⇒
              if ( config.hasPath( path ) ) {
                logger.debug( "looking for class in config path:[{}]", path )
                config.getConfig( path ).getString( "class" )
              } else {
                logger.warn( "no configuration found for path:[{}] - return empty FQN", path )
                ""
              }
            }
            .getOrElse { "" }
          }
          logger.info( "journal plugin string classname found:[{}]", fqn )
          fqn
        }

        case ConfigValueType.OBJECT ⇒ {
          import scala.reflect._
          //          import scala.collection.JavaConversions._
          val ConfigObjectType = classTag[ConfigObject]
          val jconfig = config.getConfig( JournalPluginPath )
          if ( jconfig.hasPath( "class" ) ) {
            val fqn = jconfig.getString( "class" )
            logger.info( "journal plugin class property found:[{}]", fqn )
            fqn
          } else {
            logger.warn( "no class specified for journal plugin" )
            ""
          }
        }

        case t ⇒ {
          logger.warn( "unrecognized config type:[{}] for path:[{}]", t.toString, JournalPluginPath )
          ""
        }
      }
    } else {
      logger.warn( "no journal plugin specified" )
      ""
    }
  }
}
