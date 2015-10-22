import sbt._

object Dependencies {
  val resolutionRepos = Seq(
    "spray repo" at "http://repo.spray.io",
    "eaio.com" at "http://eaio.com/maven2",
    "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases",
    "dmrolfs" at "http://dmrolfs.github.com/snapshots",
    "Typesafe releases" at "http://repo.typesafe.com/typesafe/releases"
  )

  def compile( deps: ModuleID* ): Seq[ModuleID] = deps map ( _ % "compile" )
  def provided( deps: ModuleID* ): Seq[ModuleID] = deps map ( _ % "provided" )
  def test( deps: ModuleID* ): Seq[ModuleID] = deps map ( _ % "test" )
  def runtime( deps: ModuleID* ): Seq[ModuleID] = deps map ( _ % "runtime" )
  def container( deps: ModuleID* ): Seq[ModuleID] = deps map ( _ % "container" )

  val sprayVersion = "1.3.1"
  def sprayModule( id: String ) = "io.spray" % id % sprayVersion

  val akkaVersion = "2.3.14"
  def akkaModule( id: String ) = "com.typesafe.akka" %% id % akkaVersion

  val akkaActor = akkaModule( "akka-actor" )
  val akkaContrib = akkaModule( "akka-contrib" )
  val akkaPersistence = akkaModule( "akka-persistence-experimental" )
  val akkaSlf4j = akkaModule( "akka-slf4j" )
  val akkaTestKit = akkaModule( "akka-testkit" )

  val config = "com.typesafe" % "config" % "1.3.0"
  val eaio = "com.eaio.uuid" % "uuid" % "3.4"
  val math3 = "org.apache.commons" % "commons-math3" % "3.5"
  val codec = "commons-codec" % "commons-codec" % "1.10"
  val json4sJackson = "org.json4s" %% "json4s-jackson" % "3.2.11"
  val scalalogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0"
  val logbackclassic = "ch.qos.logback" % "logback-classic" % "1.1.3"
  val scalatest = "org.scalatest" %% "scalatest" % "2.2.4"
  val specs2 = "org.specs2" %% "specs2-core" % "3.6"
  val specs2Extra = "org.specs2" %% "specs2-matcher-extra" % "3.6"
  val sprayHttp = sprayModule( "spray-http" )
  val sprayCan = sprayModule( "spray-can" )
  val sprayRouting = sprayModule( "spray-routing" )
  val sprayTestKit = sprayModule( "spray-testkit" )
  val twirlApi = "io.spray" %% "twirl-api" % "0.6.1"
  val shapelessBuilder = "com.github.dmrolfs" %% "shapeless-builder" % "0.1-SNAPSHOT"

  val joda = "joda-time" % "joda-time" % "2.8.2"
  val jodaConvert = "org.joda" % "joda-convert" % "1.7"
  val jscience = "org.jscience" % "jscience" % "4.3.1"
  val mysqlConnector = "mysql" % "mysql-connector-java" % "5.1.25"
  // val reactiveMongo = "org.reactivemongo" %% "reactivemongo" % "0.8"

  val scalaTime = "com.github.nscala-time" %% "nscala-time" % "2.2.0"
  val shapeless = "com.chuusai" %% "shapeless" % "2.2.5"
  val scalazCore = "org.scalaz" %% "scalaz-core" % "7.1.4"

  val slickPersistence = "com.typesafe.slick" %% "slick" % "2.0.0"
}