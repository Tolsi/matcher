name := "matcher"

version := "1.0"

scalaVersion := "2.11.8"

val akkaVersion = "2.4.10"

libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.1.7",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.4.0",
  "org.scalatest" %% "scalatest" % "3.0.0" % "test",
  "org.mockito" % "mockito-all" % "1.10.19" % "test",
  "org.scalamock" %% "scalamock-scalatest-support" % "3.2.2" % "test"
)
