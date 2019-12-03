name := """akka-streams-exp"""
organization := "ravi.akka.learnings"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.8"
lazy val akkaVersion = "2.5.19"
lazy val scalaTestVersion = "3.0.5"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "org.scalatest" %% "scalatest" % scalaTestVersion
)

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "ravi.akka.learnings.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "ravi.akka.learnings.binders._"
