package models.dto

import argonaut._
import scalaz._

case class AnswerEntry(name: String, isMen: Boolean, answer: Ordering)
object AnswerEntry {
  def parse(value: String): Ordering = value match {
    case "<" => Ordering.LT
    case ">" => Ordering.GT
    case _   => Ordering.LT
  }
  def encode(value: Ordering): String = value match {
    case Ordering.LT => "<"
    case Ordering.GT => ">"
    case _           => "<"
  }
  implicit val r: DecodeJson[Ordering]     = implicitly[DecodeJson[String]].map(parse)
  implicit val w: EncodeJson[Ordering]     = implicitly[EncodeJson[String]].contramap[Ordering](encode)
  implicit val rA: DecodeJson[AnswerEntry] = DecodeJson.derive
  implicit val wA: EncodeJson[AnswerEntry] = EncodeJson.derive
}
