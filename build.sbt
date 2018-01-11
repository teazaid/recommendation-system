name := "recommendation-system"

version := "0.1"

scalaVersion := "2.12.4"

val akkaHttpCirceVersion = "1.19.0"
val ioCircleVersion = "0.9.0"

val akkaHttpClientVersion = "10.1.0-RC1"

val akkaActorVersion = "2.5.9"

val catsVersion = "1.0.1"
val scalaTestVersion = "3.2.0-SNAP9"
val scalaMockVersion = "3.6.0"

libraryDependencies ++= List(
  "de.heikoseeberger" %% "akka-http-circe" % akkaHttpCirceVersion,

  "io.circe" %% "circe-core" % ioCircleVersion,
  "io.circe" %% "circe-generic" % ioCircleVersion,
  "io.circe" %% "circe-parser" % ioCircleVersion,

  "com.typesafe.akka" %% "akka-http" % akkaHttpClientVersion,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpClientVersion % Test,

  "com.typesafe.akka" %% "akka-actor" % akkaActorVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaActorVersion % Test,

  "com.typesafe.akka" %% "akka-stream" % akkaActorVersion,

  "org.typelevel" %% "cats-core" % catsVersion,

  "org.scalatest" %% "scalatest" % scalaTestVersion % Test,

  "org.scalamock" %% "scalamock-scalatest-support" % scalaMockVersion % Test

)

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.4")
