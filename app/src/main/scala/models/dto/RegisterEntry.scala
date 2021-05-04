package models.dto

import argonaut._

case class RegisterEntry(name: String, isMen: Boolean, model: models.Model)
object RegisterEntry {
  implicit val r: DecodeJson[RegisterEntry] = DecodeJson.derive
  implicit val w: EncodeJson[RegisterEntry] = EncodeJson.derive
}
