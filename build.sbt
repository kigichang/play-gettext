name := """play-gettext"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "tv.cntt" %% "scaposer" % "1.10",
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test
)

autoCompilerPlugins := true
addCompilerPlugin("tv.cntt" %% "xgettext" % "1.5.1")
scalacOptions += "-P:xgettext:services.PlayGetText"
scalacOptions += "-P:xgettext:sourceLang:en"
