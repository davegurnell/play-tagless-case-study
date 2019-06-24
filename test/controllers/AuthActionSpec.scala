package controllers

import cats.implicits._
import com.google.inject.{AbstractModule, TypeLiteral}
import org.scalatest.{Matchers, WordSpec}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{AnyContent, Request, Results}
import play.api.test.{DefaultAwaitTimeout, FakeRequest, FutureAwaits}
import services.{BasicAuth, InMemoryPasswordStore, PasswordStore}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

//noinspection TypeAnnotation
class AuthActionSpec extends WordSpec with Matchers with FutureAwaits with DefaultAwaitTimeout {
  def authedRequest(username: String, password: String): Request[AnyContent] =
    FakeRequest().withHeaders("Authorization" -> BasicAuth.encode(username, password))

  "AuthAction" should {
    "reject an unauthenticated request" in new AppControllerMocks {
      val result = await(controller.index.apply(FakeRequest()))
      result.header.status shouldBe Results.Forbidden.header.status
    }

    "accept a valid user" in new AppControllerMocks {
      val result1 = await(controller.index.apply(authedRequest("user1", "pass1")))
      result1.header.status shouldBe Results.Ok.header.status
    }

    "accept another valid user" in new AppControllerMocks {
      val result2 = await(controller.index.apply(authedRequest("user2", "pass2")))
      result2.header.status shouldBe Results.Ok.header.status
    }

    "reject a user with an incorrect password" in new AppControllerMocks {
      val result = await(controller.index.apply(authedRequest("user1", "pass2")))
      result.header.status shouldBe Results.Forbidden.header.status
    }

    "reject an unrecognised user" in new AppControllerMocks {
      val result = await(controller.index.apply(authedRequest("nopeuser", "nopepass")))
      result.header.status shouldBe Results.Forbidden.header.status
    }
  }
}

//noinspection TypeAnnotation
class AppControllerMocks {

  val passwords: Map[String, String] =
    Map("user1" -> "pass1", "user2" -> "pass2")

  lazy val passwordStore: PasswordStore[Future] =
    new InMemoryPasswordStore[Future](passwords)

  val injector = new GuiceApplicationBuilder()
    // We can't use the `bind` DSL from play.api.inject
    // because it doesn't support Guice TypeLiterals,
    // so we create an `AbstractModule` and wire that instead.
    .overrides(new TestPasswordStoreModule(passwordStore))
    .injector()

  lazy val controller: AppController =
    injector.instanceOf[AppController]
}

class TestPasswordStoreModule(passwordStore: PasswordStore[Future]) extends AbstractModule {
  override def configure(): Unit = {
    bind(new TypeLiteral[PasswordStore[Future]] {}).toInstance(passwordStore)
  }
}
