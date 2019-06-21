package controllers

import org.scalatest.{Matchers, WordSpec}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{AnyContent, Request, Results}
import play.api.test.{DefaultAwaitTimeout, FakeRequest, FutureAwaits}
import services.{BasicAuth, InMemoryPasswordStore, PasswordStore}

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

  lazy val passwordStore: PasswordStore =
    new InMemoryPasswordStore(passwords)

  val injector = new GuiceApplicationBuilder()
    .overrides(bind[PasswordStore].toInstance(passwordStore))
    .injector()

  lazy val controller: AppController =
    injector.instanceOf[AppController]
}
