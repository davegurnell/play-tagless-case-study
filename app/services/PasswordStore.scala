package services

import akka.actor.ActorSystem
import cats.Applicative
import cats.implicits._
import redis.RedisClient

import scala.concurrent.{ExecutionContext, Future}

trait PasswordStore[F[_]] {
  def init(passwords: Map[String, String]): F[Unit]
  def check(username: String, password: String): F[Boolean]
}

class InMemoryPasswordStore[F[_]: Applicative](var passwords: Map[String, String] = Map.empty) extends PasswordStore[F] {
  def init(passwords: Map[String, String]): F[Unit] = {
    this.passwords = passwords
    ().pure[F]
  }

  def check(username: String, password: String): F[Boolean] =
    passwords.get(username).fold(false)(_ == password).pure[F]
}

class RedisPasswordStore(system: ActorSystem) extends PasswordStore[Future] {
  implicit val ec: ExecutionContext =
    system.dispatcher

  private val client: RedisClient =
    RedisClient()(system)

  private val keyPrefix: String =
    "taglessdemo:"

  def init(passwords: Map[String, String]): Future[Unit] =
    Future
      .traverse(passwords.toList) { case (k, v) => client.set(keyPrefix + k, v) }
      .map(_ => ())

  def check(username: String, password: String): Future[Boolean] =
    client.get[String](keyPrefix + username).map(_.contains(password))
}
