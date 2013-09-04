import ReleaseKeys._

name := "scalop"

organization := "com.loopfor.scalop"

description := "Scala option parser"

homepage := Some(url("https://github.com/davidledwards/scalop"))

licenses := Seq("Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

scmInfo := Some(ScmInfo(
  url("https://github.com/davidledwards/scalop"),
  "scm:git:https://github.com/davidledwards/scalop.git",
  Some("scm:git:https://github.com/davidledwards/scalop.git")
))

scalaVersion := "2.10.2"

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-feature",
  "-encoding", "UTF-8"
)

javacOptions ++= Seq(
  "-source", "1.6",
  "-target", "1.6"
)

// Test dependencies.
libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "1.9.1" % "test"
)

// Merges release settings.
releaseSettings

tagName <<= version map { "release-" + _ }

tagComment <<= version map { "releasing " + _ }

commitMessage <<= version map { "setting version to " + _ }

publishMavenStyle := true

publishArtifact in Test := false

publishTo <<= version { v =>
  val repo = if (v endsWith "SNAPSHOT")
    "Sonatype Nexus Snapshot Repository" at "https://oss.sonatype.org/content/repositories/snapshots/"
  else
    "Sonatype Nexus Release Repository" at "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
  Some(repo)
}

// Ensures that published POM has no repository dependencies.
pomIncludeRepository := { _ => false }

pomExtra := (
  <developers>
    <developer>
      <id>davidledwards</id>
      <name>David Edwards</name>
      <email>david.l.edwards@gmail.com</email>
    </developer>
  </developers>
)
