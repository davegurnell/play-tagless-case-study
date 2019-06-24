package services

import akka.actor.ActorSystem
import cats.Applicative
import cats.implicits._
import com.google.inject.{AbstractModule, TypeLiteral}
import javax.inject.{Inject, Singleton}
import redis.RedisClient

import scala.concurrent.{ExecutionContext, Future}

trait PasswordStore[F[_]] {
  def init(passwords: Map[String, String]): F[Unit]
  def check(username: String, password: String): F[Boolean]
}

trait AsyncPasswordStore extends PasswordStore[Future]

class InMemoryPasswordStore[F[_]: Applicative](
  var passwords: Map[String, String] = Map.empty
) extends PasswordStore[F] {
  def init(passwords: Map[String, String]): F[Unit] = {
    this.passwords = passwords
    ().pure[F]
  }

  def check(username: String, password: String): F[Boolean] = {
    println("InMemoryPasswordStore.check " + username + " " + password + " " + passwords)
    passwords.get(username).fold(false)(_ == password).pure[F]
  }
}

@Singleton
class RedisPasswordStore @Inject() (implicit system: ActorSystem) extends PasswordStore[Future] with AsyncPasswordStore {
  implicit val ec: ExecutionContext =
    system.dispatcher

  private val client: RedisClient =
    RedisClient()

  private val keyPrefix: String =
    "taglessdemo:"

  def init(passwords: Map[String, String]): Future[Unit] =
    Future
      .traverse(passwords.toList) { case (k, v) => client.set(keyPrefix + k, v) }
      .map(_ => ())

  def check(username: String, password: String): Future[Boolean] = {
    println("RedisPasswordStore " + username + " " + password)
    client.get[String](keyPrefix + username).map(_.contains(password))
  }
}

class PasswordStoreModule extends AbstractModule {
  override def configure(): Unit = {
    bind(new TypeLiteral[PasswordStore[Future]] {}).to(classOf[RedisPasswordStore])
  }
}
