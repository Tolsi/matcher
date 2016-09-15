package ru.tolsi.matcher.naive

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}
import com.typesafe.scalalogging.StrictLogging
import ru.tolsi.matcher.{Order, OrderBook, OrderType, ReverseOrders}
import ru.tolsi.matcher.OrderBook.AddOrMatchResult._

private[naive] object SingleThreadOrderBook {
  private[naive] def dequeueOrderWhichCanApply(
      order: Order,
      orderBook: collection.Map[String, collection.Map[(Int, Int, OrderType.Value), mutable.Queue[Order]]]): Option[Order] = {
    import order._
    orderBook.get(order.asset).flatMap(_.get((price, qty, OrderType.invert(`type`))).flatMap(_.dequeueFirst(_ => true)))
  }
}
private[naive] class SingleThreadOrderBook extends OrderBook with StrictLogging {
  import OrderBook._
  import SingleThreadOrderBook._

  private[naive] val instrumentsOrderBook = mutable.AnyRefMap
    .empty[String, mutable.AnyRefMap[(Int, Int, OrderType.Value), mutable.Queue[Order]]]

  override def addOrMatch(order: Order)(implicit ec: ExecutionContext): Future[AddOrMatchResult] = Future.successful {
    dequeueOrderWhichCanApply(order, instrumentsOrderBook) match {
      case Some(matchedOrder) =>
        Matched(ReverseOrders(order, matchedOrder))
      case None =>
        import order._
        instrumentsOrderBook.getOrElseUpdate(order.asset,
          new mutable.AnyRefMap[(Int, Int, OrderType.Value), mutable.Queue[Order]]())
          .getOrElseUpdate((price, qty, `type`), mutable.Queue.empty[Order]) += order
        Added
    }
  }
}
