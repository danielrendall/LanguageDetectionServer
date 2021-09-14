package uk.co.danielrendall.languagedetectionserver

@main def languageDetectionServer() =
  val port = Option(System.getProperty("port")).map(_.toInt).getOrElse(8080)
  println("PORT =" + port)

