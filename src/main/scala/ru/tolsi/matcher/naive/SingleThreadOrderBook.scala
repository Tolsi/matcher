package ru.tolsi.matcher.naive

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}
import com.typesafe.scalalogging.StrictLogging
import ru.tolsi.matcher.{Order, OrderBook, OrderType, ReverseOrders}

private[naive] object SingleThreadOrderBook {
  private[SingleThreadOrderBook] def findOrderWhichCanApply(
      order: Order,
      orderBook: collection.Map[String, collection.Map[(Int, Int, OrderType.Value), Order]]): Option[Order] = {
    import order._
    orderBook.get(order.asset).flatMap(_.get((price, qty, OrderType.invert(`type`))))
  }
}
private[naive] class SingleThreadOrderBook extends OrderBook with StrictLogging {
  import OrderBook._
  import SingleThreadOrderBook._
  private val instrumentsOrderBook = mutable.AnyRefMap.empty[String, mutable.AnyRefMap[(Int, Int, OrderType.Value), Order]]
  override def addOrMatch(order: Order)(implicit ec: ExecutionContext): Future[AddOrMatchResult] = Future.successful {
    findOrderWhichCanApply(order, instrumentsOrderBook) match {
      case Some(matchedOrder) =>
        instrumentsOrderBook(order.asset) - ((matchedOrder.price, matchedOrder.qty, OrderType.invert(matchedOrder.`type`)))
        Matched(ReverseOrders(order, matchedOrder))
      case None =>
        instrumentsOrderBook.getOrElseUpdate(order.asset, mutable.AnyRefMap.empty) += ((order.price, order.qty, order.`type`) -> order)
        Added
    }
  }
}
