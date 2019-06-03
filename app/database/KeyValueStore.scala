package database

import cats.Monad
import cats.implicits._
import play.api.libs.json._
import scala.concurrent._

trait KeyValueStore[F[_], K, V] {
  def get(key: K): F[Option[V]]
  def put(key: K, value: V): F[Unit]
  def delete(key: K): F[Unit]
}

class AsyncKeyValueStore[K, V](implicit ec: ExecutionContext) extends KeyValueStore[Future, K, V] {
  var data: Map[K, V] = Map()

  def get(key: K): Future[Option[V]] =
    Future {
      data.get(key)
    }

  def put(key: K, value: V): Future[Unit] =
    Future {
      data = data + (key -> value)
      ()
    }

  def delete(key: K): Future[Unit] =
    Future {
      data = data - key
      ()
    }
}

class JsonKeyValueStore[F[_]: Monad, K, V: Format](
  inner: KeyValueStore[F, K, JsValue]
) extends KeyValueStore[F, K, V] {
  def get(key: K): F[Option[V]] =
    inner.get(key).map(_.flatMap(_.validate[V].asOpt))

  def put(key: K, value: V): F[Unit] =
    inner.put(key, Json.toJson(value))

  def delete(key: K): F[Unit] =
    inner.delete(key)
}
