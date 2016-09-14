package ru.tolsi.matcher

import scala.concurrent.{ExecutionContext, Future}

abstract class ClientRepository[@specialized T](implicit num: Numeric[T]) {
  def get(id: String)(implicit ec: ExecutionContext): Future[Option[Client[T]]]
  def getAll(implicit ec: ExecutionContext): Future[Iterable[Client[T]]]
}
