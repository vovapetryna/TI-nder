package models.dto

import argonaut._

case class Entry(name: String, isMen: Boolean)
object Entry {
  implicit val r: DecodeJson[Entry] = DecodeJson.derive
  implicit val w: EncodeJson[Entry] = EncodeJson.derive
}
