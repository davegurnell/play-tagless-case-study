package models

import java.util.UUID

case class UserUpdate(username: String) {
  def createUser(id: UUID): User =
    User(username = username, id = id)

  def updateUser(user: User): User =
    User(username = username, id = user.id)
}

object UserUpdate {
  import play.api.libs.json._

  implicit val format: OFormat[UserUpdate] =
    Json.format
}
