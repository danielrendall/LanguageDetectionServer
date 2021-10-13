package uk.co.danielrendall.languagedetectionserver

object Exceptions:

  case class RequestTooBigException(bodySize: Int) extends Exception(s"Request of size $bodySize is too big")

  case object NoDocumentSuppliedException extends Exception("No document supplied")
