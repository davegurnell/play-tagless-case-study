package controllers

import play.api.libs.json._

case class PasswordRequest(password: String)

object PasswordRequest {
  implicit val format: OFormat[PasswordRequest] =
    Json.format
}