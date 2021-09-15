package uk.co.danielrendall.languagedetectionserver

import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.Response.Status
import fi.iki.elonen.NanoHTTPD.{IHTTPSession, Response, newFixedLengthResponse}

import javax.xml.transform.{Templates, TransformerFactory}
import scala.collection.mutable

/**
 * The server itself
 */
class LanguageDetectionServerApp(port: Int) extends NanoHTTPD(port):

  object Constants:
    val QUIT = "_quit"

  println("Server using port " + port)

  override def serve(session: IHTTPSession): Response =
    // URI always starts with "/"
    session.getUri.tail.split("/").filterNot(_.isEmpty).toList match {
      case head::tail =>
        if (head == Constants.QUIT) {
          quit()
        } else {
          newFixedLengthResponse(Status.OK, "text/plain", "Hello world")
        }
      case _ =>
        runningMessage()
    }

  private def quit(): Response =
    println("Quitting")
    stop()
    newFixedLengthResponse(Status.OK, "text/plain", "")

  private def runningMessage(): Response =
    newFixedLengthResponse(Status.OK, "text/plain", "LanguageDetectionServer is running")


