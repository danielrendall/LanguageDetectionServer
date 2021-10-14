package uk.co.danielrendall.languagedetectionserver

import com.sixtysevenbricks.text.languagedetection.{LanguageDetector, LanguageFingerprint, NGramProfiler}
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.*
import fi.iki.elonen.NanoHTTPD.Response.Status
import uk.co.danielrendall.languagedetectionserver.Exceptions.{BadVerb, NoDocumentSuppliedException, RequestTooBigException}

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.nio.charset.StandardCharsets
import javax.xml.transform.{Templates, TransformerFactory}
import scala.collection.mutable
import scala.jdk.CollectionConverters.{ListHasAsScala, MapHasAsScala}
import scala.util.{Failure, Success, Try}

/**
 * The server itself
 */
class LanguageDetectionServerApp(port: Int) extends NanoHTTPD(port):

  private val languageDetector: LanguageDetector = LanguageDetector.newEmpty

  object Constants:
    val QUIT = "_quit"
    val LIST = "_list"
    val DETECT = "_detect"
    val FINGERPRINT = "_fingerprint"
    // cURL sents an "Expect" header if the size is too big; rather than figure out how to deal with that,
    // we impose a limit which should be safe. See https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Expect
    val MAX_BODY_SIZE = 8388603 // 8MB

  println("Server using port " + port)

  override def serve(session: IHTTPSession): Response =
    // URI always starts with "/"
    session.getUri.tail.split("/").filterNot(_.isEmpty).toList match {
      case head::tail =>
        if (head == Constants.QUIT) {
          quit()
        } else {
          session.getMethod match {
            case Method.GET => get(session, head, tail)
            case Method.PUT => put(session, head, tail)
            case Method.POST => post(session, head, tail)
            case Method.DELETE => delete(session, head, tail)
            case _ => badRequest("Unsupported method: " + session.getMethod.name())
          }
        }
      case _ =>
        runningMessage()
    }

  private def get(session: IHTTPSession, first: String, rest: List[String]) =
    if (first == Constants.LIST) {
      okMsg(languageDetector.getAvailableLanguages.mkString("\n"))
    } else {
      okMsg("GET " + first)
    }

  private def put(session: IHTTPSession, first: String, rest: List[String]) =
    if (first.startsWith("_")) {
      badRequest("Identifiers starting with underscores are reserved")
    } else {
      (for {
        byteArray <- getUploadedBytes(session)
        _ <- if (byteArray.nonEmpty) saveFingerprint(first, byteArray) else Success(())
      } yield ()) match {
        case Failure(ex) =>
          badRequest(ex.getMessage)
        case Success(_) =>
          okMsg(s"Put $first")
      }
    }

  private def post(session: IHTTPSession, first: String, rest: List[String]): Response =
    if (first == Constants.DETECT || first == Constants.FINGERPRINT) {
      (for {
        byteArray <- getUploadedBytes(session)
        _ <- if (byteArray.nonEmpty) Success(()) else Failure(NoDocumentSuppliedException)
        result <- process(first, byteArray)
      } yield result) match {
        case Success(result) =>
          okMsg(result)
        case Failure(ex) =>
          badRequest(ex.getMessage)
      }
    } else {
      badRequest(s"Didn't understand $first")
    }


  private def delete(session: IHTTPSession, first: String, rest: List[String]) =
    if (first.startsWith("_")) {
      badRequest("Identifiers starting with underscores are reserved")
    } else {
      languageDetector.removeFingerprint(first)
      okMsg(s"Removed $first")
    }

  private def getUploadedBytes(session: IHTTPSession): Try[Array[Byte]] =
    val bodySize: Int = getBodySize(session)
    if (bodySize >  Constants.MAX_BODY_SIZE)
      Failure(RequestTooBigException(bodySize))
    else
      Try {
        val baos = new ByteArrayOutputStream(bodySize)
        StreamUtils.copy(session.getInputStream(), baos, bodySize)
        baos.toByteArray
      }

  private def getBodySize(session: IHTTPSession): Int =
    session.getHeaders.asScala.get("content-length").map(_.toInt).getOrElse(0)

  private def saveFingerprint(name: String, bytes: Array[Byte]): Try[Unit] =
    Try {
      val bais = new ByteArrayInputStream(bytes)
      languageDetector.addOrUpdateFingerprint(LanguageFingerprint.load(name, bais))
    }

  private def process(action: String, bytes: Array[Byte]): Try[String] =
    Try {
      val text = new String(bytes, StandardCharsets.UTF_8)
      action match {
        case Constants.DETECT =>
          languageDetector.identifyLanguage(text)
        case Constants.FINGERPRINT =>
          NGramProfiler.createNgramProfile(text, 5).mkString("\n")
        case _ =>
          throw BadVerb(action)
      }
    }


  private def quit(): Response =
    println("Quitting")
    stop()
    okMsg("")

  private def runningMessage(): Response =
    okMsg("LanguageDetectionServer is running")

  private def okMsg(msg: String): Response =
    newFixedLengthResponse(Status.OK, MIME_PLAINTEXT, msg)

  private def badRequest(msg: String): Response =
    newFixedLengthResponse(Status.BAD_REQUEST, MIME_PLAINTEXT, msg)

