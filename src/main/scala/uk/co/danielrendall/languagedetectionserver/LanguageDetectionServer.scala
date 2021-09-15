package uk.co.danielrendall.languagedetectionserver

import fi.iki.elonen.NanoHTTPD

@main def languageDetectionServer() =
  val port = Option(System.getProperty("port")).map(_.toInt).getOrElse(8080)
  new LanguageDetectionServerApp(port).start(NanoHTTPD.SOCKET_READ_TIMEOUT, false)

