package controllers

import cats.data.OptionT
import cats.implicits._
import javax.inject.Inject
import play.api.mvc._
import services.{BasicAuth, PasswordStore}

import scala.concurrent.{ExecutionContext, Future}

class AuthAction @Inject() (
  val parser: BodyParsers.Default,
  val passwordStore: PasswordStore
)(
  implicit val executionContext: ExecutionContext
) extends ActionBuilder[AuthRequest, AnyContent] with ActionRefiner[Request, AuthRequest] {

  override def refine[A](request: Request[A]): Future[Either[Result, AuthRequest[A]]] = {
    val authedUser: OptionT[Future, String] =
      for {
        header <- OptionT.fromOption[Future](request.headers.get("Authorization"))
        (u, p) <- OptionT.fromOption[Future](BasicAuth.decode(header))
        ok     <- OptionT.liftF(passwordStore.check(u, p))
        result <- OptionT.fromOption[Future](if(ok) Some(u) else None)
      } yield result

    authedUser.value.map {
      case Some(username) =>
        Right(AuthRequest(username, request))

      case None =>
        Left(Results.Forbidden("Nope!"))
    }
  }
}
