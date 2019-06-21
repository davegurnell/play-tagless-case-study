package startup

import cats.implicits._
import controllers._
import play.api.mvc.{BodyParsers, EssentialFilter}
import play.api.routing.Router
import play.api.{Application, ApplicationLoader, BuiltInComponentsFromContext}
import router.Routes
import services._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class AppLoader extends ApplicationLoader {
  def load(context: ApplicationLoader.Context): Application = {
    val passwords: Map[String, String] =
      Map(
        "garfield"    -> "iheartlasagne",
        "grumpycat"   -> "nope",
        "snagglepuss" -> "murgatroyd",
      )

    val module = new StartupModule(context)

    module.init(passwords)

    module.application
  }
}

class StartupModule(context: ApplicationLoader.Context) extends BuiltInComponentsFromContext(context) {
  val httpFilters: Seq[EssentialFilter] =
    Seq.empty

  lazy val passwordStore = new InMemoryPasswordStore[Future]()
  lazy val bodyParser    = new BodyParsers.Default(playBodyParsers)
  lazy val authAction    = new AuthAction(bodyParser, passwordStore)
  lazy val appController = new AppController(controllerComponents, authAction)

  val router: Router = new Routes(
    httpErrorHandler,
    appController,
    prefix = ""
  )

  def init(passwords: Map[String, String]): Unit =
    Await.result(passwordStore.init(passwords), Duration.Inf)
}
