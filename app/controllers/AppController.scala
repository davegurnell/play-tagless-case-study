package controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc._

import scala.concurrent._

@Singleton
class AppController @Inject() (
  val cc: ControllerComponents,
  val AuthAction: AuthAction
)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  def index: Action[AnyContent] =
    AuthAction { request =>
      Ok(s"Hi ${request.username}!")
    }
}
