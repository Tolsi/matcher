package ru.tolsi.matcher

import scala.concurrent.{ExecutionContext, Future}

class Matcher(
    clientRepository: ClientRepository[Long],
    ordersExecutor: ReverseOrdersExecutor[Long],
    orderBook: OrderBook) {
  private def createOrder(id: Int, createOperation: OrderOperation.Create): Order = {
    import createOperation._
    Order(id, creator, `type`, asset, price, qty)
  }
  def apply(orders: Seq[OrderOperation.Create])(implicit ec: ExecutionContext): Future[Unit] = {
    val ordersFutures = orders.view.zipWithIndex.map { case (c, index) => createOrder(index, c) }.map(executeOrder)
    Future.sequence(ordersFutures).map(_ => ())
  }

  private def executeOrder(order: Order): Future[Unit] = {
    orderBook.addOrMatch(order).flatMap {
      case Right(reverseOrders) =>
        ordersExecutor.execute(reverseOrders, clientRepository)
      case _ =>
        Future.successful()
    }
  }
}
