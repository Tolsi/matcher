package ru.tolsi.matcher

import scala.concurrent.{ExecutionContext, Future}

class Exchange(
    clientRepository: ClientRepository[Long],
    ordersExecutor: ReverseOrdersExecutor[Long],
    orderBook: OrderBook) {

  def apply(orders: Seq[OrderOperation.Create])(implicit ec: ExecutionContext): Future[Unit] = {
    val ordersFutures = orders.view.zipWithIndex.map { case (c, index) => createOrder(index, c) }.map(executeOrder)
    Future.sequence(ordersFutures).map(_ => ())
  }

  private def createOrder(id: Int, createOperation: OrderOperation.Create): Order = {
    import createOperation._
    Order(id, creator, `type`, asset, price, qty)
  }

  private def executeOrder(order: Order)(implicit ec: ExecutionContext): Future[Unit] = {
    orderBook.addOrMatch(order).flatMap {
      case Right(reverseOrders) =>
        ordersExecutor.execute(reverseOrders, clientRepository)
      case _ =>
        Future.successful()
    }
  }
}
