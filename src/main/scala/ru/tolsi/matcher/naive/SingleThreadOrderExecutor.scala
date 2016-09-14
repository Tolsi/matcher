package ru.tolsi.matcher.naive

import scala.concurrent.{ExecutionContext, Future}
import ru.tolsi.matcher.{Client, ClientRepository, OrderType, ReverseOrders, ReverseOrdersExecutor}

private[naive] object SingleThreadOrderExecutor extends ReverseOrdersExecutor[Long] {
  // todo test
  def execute(orders: ReverseOrders, clientRepository: ClientRepository[Long])(implicit ec: ExecutionContext): Future[Unit] = {
    val ReverseOrders(order, reverseOrder) = orders
    for {
      orderCreatorOption <- clientRepository.get(order.creator)
      reverseOrderCreatorOption <- clientRepository.get(reverseOrder.creator)
    } yield {
      (orderCreatorOption, reverseOrderCreatorOption) match {
        case (Some(orderCreator), Some(reverseOrderCreator)) =>
          import order._
          def buy(buyer: Client[Long], seller: Client[Long]): Unit = {
            buyer.addDeltaToBalance(asset, price * qty)
            // todo currency?
            buyer.addDeltaToBalance("USD", -price * qty)
            seller.addDeltaToBalance(asset, -(price * qty))
            seller.addDeltaToBalance("USD", price * qty)
          }
          if (`type` == OrderType.Buy) {
            buy(orderCreator, reverseOrderCreator)
          } else {
            buy(reverseOrderCreator, orderCreator)
          }
        case _ => throw new IllegalStateException(s"At least one of orders '$orders' creators are not found")
      }
    }
  }
}
