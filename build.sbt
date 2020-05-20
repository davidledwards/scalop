lazy val compilerSettings = Seq(
  scalaVersion := "2.13.2",
  scalacOptions ++= Seq(
    "-target:11",
    "-deprecation",
    "-unchecked",
    "-feature",
    "-encoding", "UTF-8"
  )
)

lazy val dependencySettings = Seq(
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "3.1.2" % "test"
  )
)

lazy val docSettings = Seq(
  Compile / doc / scalacOptions ++= Seq("-no-link-warnings"),
  autoAPIMappings := true,
  apiURL := Some(url(s"https://davidedwards.io/scalop/api/${version.value}/"))
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
  Test / publishArtifact := false,
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
    executionEnvironment := Some(EclipseExecutionEnvironment.JRE11)
  )
}

lazy val rootProject = (project in file(".")).
  settings(
    name := "scalop",
    organization := "com.loopfor.scalop",
    version := "2.3",
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
