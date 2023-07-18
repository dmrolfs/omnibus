import BuildSettings._
import Dependencies._
import sbtassembly.AssemblyPlugin.autoImport.MergeStrategy

name := "omnibus-commons"

description := "lorem ipsum."

scalacOptions := scalacBuildOptions

resolvers += "omen-bintray" at "http://dl.bintray.com/omen/maven"

libraryDependencies ++=
  commonDependencies ++
  monix.all ++
  time.all ++
  Seq(
    "org.scala-lang.modules" %% "scala-xml" % "1.3.1",
    "com.github.dmrolfs" %% "shapeless-builder" % "1.0.1",
    "commons-codec" % "commons-codec" % "1.14",
    akka.actor,
    json4sJackson,
    math3,
    squants
  )


testOptions in Test += Tests.Argument( "-oDF" )

assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false, includeDependency = false)

assemblyJarName in assembly := s"${organizationName.value}-${name.value}-${version.value}.jar"
