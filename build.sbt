import sbt.Keys.libraryDependencies

name := "state-calculator"
version := "0.1"
scalaVersion := "2.12.4"
assemblyJarName in assembly := "state-calculator.jar"

libraryDependencies ++= Seq(
  "org.json4s" %% "json4s-jackson" % "3.5.3",
  "com.github.scopt" %% "scopt" % "3.7.0",
  "com.beachape" %% "enumeratum" % "1.5.12",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test"
)

enablePlugins(JavaAppPackaging)
