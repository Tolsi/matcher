package ru.tolsi.matcher

import scala.concurrent.{ExecutionContext, Future}

object Exchange {
  private[matcher] def createOrder(id: Int, createOperation: OrderOperation.Create): Order = {
    import createOperation._
    Order(id, creator, `type`, asset, price, qty)
  }
}

class Exchange(
    clientRepository: ClientRepository[Long],
    ordersExecutor: ReverseOrdersExecutor[Long],
    orderBook: OrderBook) {
  import Exchange._

  def apply(orders: Seq[OrderOperation.Create])(implicit ec: ExecutionContext): Future[Unit] = {
    val ordersFutures = orders.view.zipWithIndex.map { case (c, index) => createOrder(index, c) }.map(executeOrder)
    Future.sequence(ordersFutures).map(_ => ())
  }

  private[matcher] def executeOrder(order: Order)(implicit ec: ExecutionContext): Future[Unit] = {
    orderBook.addOrMatch(order).flatMap {
      case Right(reverseOrders) =>
        ordersExecutor.execute(reverseOrders, clientRepository)
      case _ =>
        Future.successful()
    }
  }
}
