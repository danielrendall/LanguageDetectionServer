val scala3Version = "3.0.2"

lazy val root = project
  .in(file("."))
  .settings(
    name := "language-detection-server",
    version := "0.1.0",

    scalaVersion := scala3Version,

    assembly / mainClass := Some("uk.co.danielrendall.languagedetectionserver.languageDetectionServer"),

    libraryDependencies ++= Seq(
      "org.nanohttpd" % "nanohttpd" % "2.3.1"
    )
  )
