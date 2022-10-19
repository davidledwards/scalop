/*
 * Copyright 2020 David Edwards
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

lazy val scala_2_13 = "2.13.10"
lazy val scala_3_2 = "3.2.0"
lazy val supportedScalaVersions = List(scala_2_13, scala_3_2)

lazy val compilerSettings = Seq(
  scalaVersion := scala_2_13,
  crossScalaVersions := supportedScalaVersions,
  scalacOptions ++= Seq(
    "-release:11",
    "-deprecation",
    "-unchecked",
    "-feature",
    "-encoding", "UTF-8"
  )
)

lazy val dependencySettings = Seq(
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "3.2.13" % "test"
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

lazy val rootProject = (project in file(".")).
  settings(
    name := "scalop",
    organization := "com.loopfor.scalop",
    version := "2.3.1",
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
  settings(publishSettings: _*)
