package controllers

import models.UserUpdate
import services.{UserService, PasswordService}

import cats.implicits._
import java.util.UUID
import play.api.libs.json._
import play.api.mvc._
import scala.concurrent._

class AppController(
  val cc: ControllerComponents,
  val userService: UserService[Future],
  val passwordService: PasswordService[Future],
)(implicit ec: ExecutionContext) extends AbstractController(cc) {
  def createUser =
    Action(parse.json[UserUpdate]).async { request =>
      userService.create(request.body)
        .map(user => Ok(Json.toJson(user)))
    }

  def readUser(id: UUID) =
    Action.async { request =>
      userService.read(id).map {
        case Some(user) => Ok(Json.toJson(user))
        case None       => NotFound
      }
    }

  def updateUser(id: UUID) =
    Action(parse.json[UserUpdate]).async { request =>
      userService.update(id, request.body).map {
        case Some(user) => Ok(Json.toJson(user))
        case None       => NotFound
      }
    }

  def deleteUser(id: UUID) =
    Action.async { request =>
      userService.delete(id).map(_ => Ok)
    }

  def checkPassword(id: UUID) =
    Action(parse.json[PasswordRequest]).async { request =>
      userService.read(id).flatMap {
        case Some(user) =>
          passwordService.checkPassword(user.username, request.body.password).ifM(
            Future.successful(Ok : Result),
            Future.successful(Forbidden : Result),
          )

        case None =>
          Future.successful(NotFound : Result)
      }
    }

  def resetPassword(id: UUID) =
    Action(parse.json[PasswordRequest]).async { request =>
      userService.read(id).flatMap {
        case Some(user) =>
          passwordService.resetPassword(user.username, request.body.password)
            .map(_ => Ok)

        case None =>
          Future.successful(NotFound : Result)
      }
    }
}
