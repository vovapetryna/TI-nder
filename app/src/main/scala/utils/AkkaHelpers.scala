package utils

import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.StandardRoute
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshaller}
import akka.util.ByteString
import argonaut.Argonaut._
import argonaut._

trait AkkaHelpers {
  def contentTypeRanges: List[ContentTypeRange] = List(MediaTypes.`application/json`)

  implicit def fromEntityUnmarshaller[T](implicit evidence1: DecodeJson[T]): FromEntityUnmarshaller[T] =
    Unmarshaller.byteStringUnmarshaller.forContentTypes(contentTypeRanges: _*).map {
      case ByteString.empty => throw Unmarshaller.NoContentException
      case req =>
        println(req.utf8String)
        req.utf8String.decodeEither[T] match {
          case Left(ex)      => throw new IllegalArgumentException(ex)
          case Right(value) => value
        }
    }

  implicit def toEntityMarshaller[T](implicit evidence2: EncodeJson[T]): ToEntityMarshaller[T] =
    Marshaller.withFixedContentType(MediaTypes.`application/json`) { s =>
      HttpEntity(MediaTypes.`application/json`, s.asJson.nospaces)
    }

  case class Resp[T](res: T)
  object Resp {
    implicit def w[T](implicit wT: EncodeJson[T]): EncodeJson[Resp[T]] = EncodeJson.derive
    implicit def r[T](implicit rT: DecodeJson[T]): DecodeJson[Resp[T]] = DecodeJson.derive
  }
  implicit class ToResponse[T](value: T)(implicit w: EncodeJson[T]) {
    def toResp: Resp[T] = Resp(value)
  }

  implicit class CompliantOps[T](value: Resp[T])(implicit wT: ToEntityMarshaller[Resp[T]]) {
    def send: StandardRoute = complete(StatusCodes.OK, scala.collection.immutable.Seq.empty[HttpHeader], value)
  }
}
