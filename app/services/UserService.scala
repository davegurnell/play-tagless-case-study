package services

import models.{User, UserUpdate}

import cats.Monad
import cats.implicits._
import database.KeyValueStore
import java.util.UUID

trait UserService[F[_]] {
  def create(update: UserUpdate): F[User]
  def read(id: UUID): F[Option[User]]
  def update(id: UUID, update: UserUpdate): F[Option[User]]
  def delete(id: UUID): F[Unit]
}

class GenericUserService[F[_]: Monad](
  store: KeyValueStore[F, UUID, User]
) extends UserService[F] {
  def create(update: UserUpdate): F[User] = {
    val id = UUID.randomUUID
    val user = update.createUser(id)
    store.put(id, user).map(_ => user)
  }

  def read(id: UUID): F[Option[User]] =
    store.get(id)

  def update(id: UUID, update: UserUpdate): F[Option[User]] =
    store.get(id).flatMap {
      case Some(user) =>
        val updated = update.updateUser(user)
        store.put(id, updated).map(_ => Some(updated))

      case None =>
        Option.empty[User].pure[F]
    }

  def delete(id: UUID): F[Unit] =
    store.delete(id)
}
