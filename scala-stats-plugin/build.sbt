
lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "plugin",
      scalaVersion := "2.11.5",
      crossScalaVersions := Seq("2.11.5", "2.12.4"),
      version := "0.1.0-SNAPSHOT"
    )),
    name := "scala-stats-plugin",
    libraryDependencies ++= Seq(
      ("org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided")
    )
  )
