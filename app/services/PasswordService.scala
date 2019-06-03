package services

import models.{User, UserUpdate}

import cats.Monad
import cats.implicits._
import database.KeyValueStore
import java.util.UUID

trait PasswordService[F[_]] {
  def resetPassword(username: String, password: String): F[Unit]
  def checkPassword(username: String, password: String): F[Boolean]
}

class GenericPasswordService[F[_]: Monad](
  store: KeyValueStore[F, String, String]
) extends PasswordService[F] {
  def resetPassword(username: String, password: String): F[Unit] =
    store.put(username, password)

  def checkPassword(username: String, password: String): F[Boolean] =
    store.get(username)
      .map(_.fold(false)(pwd => pwd == password))
}
