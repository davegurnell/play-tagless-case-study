package startup

import controllers._
import database._
import models._
import services._
import router.Routes

import cats.implicits._
import java.util.UUID
import play.api.libs.json._
import play.api.mvc.EssentialFilter
import play.api.routing.Router
import play.api.{Application, ApplicationLoader, BuiltInComponentsFromContext}
import scala.concurrent._

class AppLoader extends ApplicationLoader {
  def load(context: ApplicationLoader.Context): Application = {
    val module = new StartupModule(context)
    module.application
  }
}

class StartupModule(context: ApplicationLoader.Context) extends BuiltInComponentsFromContext(context) {
  val httpFilters: Seq[EssentialFilter] =
    Seq.empty

  lazy val store          = new AsyncKeyValueStore[UUID, JsValue]()
  lazy val userDatabase   = new JsonKeyValueStore[Future, UUID, User](store)
  lazy val userService    = new GenericUserService(userDatabase)
  lazy val userController = new UserController(controllerComponents, userService)

  val router: Router = new Routes(
    httpErrorHandler,
    userController,
    prefix = ""
  )
}