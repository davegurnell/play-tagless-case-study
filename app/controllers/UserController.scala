package controllers

import models.UserUpdate
import services.UserService

import java.util.UUID
import play.api.libs.json._
import play.api.mvc._
import scala.concurrent._

class UserController(
  val cc: ControllerComponents,
  val userService: UserService[Future],
)(implicit ec: ExecutionContext) extends AbstractController(cc) {
  val UserUpdateRequest = Action(parse.json[UserUpdate])

  def create = UserUpdateRequest.async { request =>
    userService.create(request.body)
      .map(user => Ok(Json.toJson(user)))
  }

  def read(id: UUID) = Action.async { request =>
    userService.read(id).map {
      case Some(user) => Ok(Json.toJson(user))
      case None       => NotFound
    }
  }

  def update(id: UUID) = UserUpdateRequest.async { request =>
    userService.update(id, request.body).map {
      case Some(user) => Ok(Json.toJson(user))
      case None       => NotFound
    }
  }

  def delete(id: UUID) = Action.async { request =>
    userService.delete(id).map(_ => Ok)
  }
}
