lazy val compilerSettings = Seq(
  scalaVersion := "2.11.7",
  scalacOptions ++= Seq(
    "-target:jvm-1.6",
    "-deprecation",
    "-unchecked",
    "-feature",
    "-encoding", "UTF-8"
  )
)

lazy val dependencySettings = Seq(
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "2.2.4" % "test"
  )
)

lazy val docSettings = Seq(
  scalacOptions in (Compile, doc) ++= Seq("-no-link-warnings"),
  autoAPIMappings := true,
  apiURL := Some(url("http://www.loopfor.com/scalop/api/2.0/"))
)

lazy val publishSettings = Seq(
  pomIncludeRepository := { _ => false },
  pomExtra :=
    <developers>
      <developer>
        <id>davidledwards</id>
        <name>David Edwards</name>
        <email>david.l.edwards@gmail.com</email>
      </developer>
    </developers>,
  publishMavenStyle := true,
  publishArtifact in Test := false,
  publishTo := Some(
    if (version.value endsWith "SNAPSHOT")
      "Sonatype Nexus Snapshot Repository" at "https://oss.sonatype.org/content/repositories/snapshots/"
    else
      "Sonatype Nexus Release Repository" at "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
  )
)

lazy val eclipseSettings = {
  import EclipseKeys._
  Seq(
    executionEnvironment := Some(EclipseExecutionEnvironment.JavaSE16)
  )
}

lazy val rootProject = (project in file(".")).
  settings(
    name := "scalop",
    organization := "com.loopfor.scalop",
    version := "2.0",
    description := "Scala option parser",
    homepage := Some(url("https://github.com/davidledwards/scalop")),
    licenses := Seq("Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),
    scmInfo := Some(ScmInfo(
      url("https://github.com/davidledwards/scalop"),
      "scm:git:https://github.com/davidledwards/scalop.git",
      Some("scm:git:https://github.com/davidledwards/scalop.git")
    ))
  ).
  settings(compilerSettings: _*).
  settings(dependencySettings: _*).
  settings(docSettings: _*).
  settings(publishSettings: _*).
  settings(eclipseSettings: _*)
