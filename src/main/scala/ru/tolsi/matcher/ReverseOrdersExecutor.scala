package ru.tolsi.matcher

import scala.concurrent.{ExecutionContext, Future}

abstract class ReverseOrdersExecutor[@specialized T](implicit num: Numeric[T]) {
  def execute(orders: ReverseOrders, clientRepository: ClientRepository[T])(implicit ec: ExecutionContext): Future[Unit]
}
