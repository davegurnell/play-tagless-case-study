package controllers

import play.api.mvc._

import scala.concurrent._

class AppController(
  val cc: ControllerComponents,
  val AuthAction: AuthAction
)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  def index: Action[AnyContent] =
    AuthAction { request =>
      Ok(s"Hi ${request.username}!")
    }
}
