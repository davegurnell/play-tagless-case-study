package models

import java.util.UUID

case class User(
  username: String,
  name: String,
  id: UUID = UUID.randomUUID,
)

object User {
  import play.api.libs.json._

  implicit val format: OFormat[User] =
    Json.format
}
