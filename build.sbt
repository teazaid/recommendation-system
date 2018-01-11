name := "recommendation-system"

version := "0.1"

scalaVersion := "2.12.4"

libraryDependencies ++= List(
  "de.heikoseeberger" %% "akka-http-circe" % "1.19.0",

  "io.circe" %% "circe-core" % "0.9.0",
  "io.circe" %% "circe-generic" % "0.9.0",
  "io.circe" %% "circe-parser" % "0.9.0",

  "com.typesafe.akka" %% "akka-http" % "10.0.11",
  "com.typesafe.akka" %% "akka-http-testkit" % "10.0.11" % Test,

  "com.typesafe.akka" %% "akka-actor" % "2.5.8",
  "com.typesafe.akka" %% "akka-testkit" % "2.5.8" % Test,

  "org.typelevel" %% "cats-core" % "1.0.1",
  "org.typelevel" %% "cats-free" % "1.0.1",

  "org.scalatest" %% "scalatest" % "3.2.0-SNAP9" % Test
)

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.4")