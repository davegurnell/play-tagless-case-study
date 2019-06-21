package services

import akka.actor.ActorSystem
import com.google.inject.AbstractModule
import javax.inject.{Inject, Singleton}
import redis.RedisClient

import scala.concurrent.{ExecutionContext, Future}

trait PasswordStore {
  def init(passwords: Map[String, String]): Future[Unit]
  def check(username: String, password: String): Future[Boolean]
}

class InMemoryPasswordStore(var passwords: Map[String, String] = Map.empty) extends PasswordStore {
  def init(passwords: Map[String, String]): Future[Unit] = {
    this.passwords = passwords
    Future.successful(())
  }

  def check(username: String, password: String): Future[Boolean] =
    Future.successful(passwords.get(username).fold(false)(_ == password))
}

@Singleton
class RedisPasswordStore @Inject() (implicit system: ActorSystem) extends PasswordStore {
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

  def check(username: String, password: String): Future[Boolean] =
    client.get[String](keyPrefix + username).map(_.contains(password))
}

class PasswordStoreModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[PasswordStore]).to(classOf[RedisPasswordStore])
  }
}
