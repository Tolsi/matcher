package ru.tolsi.matcher.naive

import scala.collection.mutable
import scala.concurrent.Future
import ru.tolsi.matcher.OrderBook.AddOrMatchResult._
import ru.tolsi.matcher.{Order, OrderType, ReverseOrders, UnitSpec}

class SingleThreadOrderBookSpec extends UnitSpec {
  describe("dequeueOrderWhichCanApply method") {
    it("should dequeue reverse orders and remove it from orders map") {
      val order = Order(1, "1", OrderType.Sell, "a", 1, 2)
      val map: collection.Map[String, collection.Map[(Int, Int, OrderType.Value), mutable.Queue[Order]]] =
        Map("a" -> Map((1, 2, OrderType.Sell) -> mutable.Queue[Order](order)))
      SingleThreadOrderBook.dequeueOrderWhichCanApply(Order(2, "1", OrderType.Buy, "a", 1, 2), map) should be(Some(
        order))
      map("a")((1, 2, OrderType.Sell)) should be('empty)
    }
    it("should not dequeue partially executed orders") {
      val order = Order(1, "1", OrderType.Sell, "a", 1, 1)
      val map: collection.Map[String, collection.Map[(Int, Int, OrderType.Value), mutable.Queue[Order]]] =
        Map("a" -> Map((1, 1, OrderType.Sell) -> mutable.Queue[Order](order)))
      SingleThreadOrderBook.dequeueOrderWhichCanApply(Order(2, "1", OrderType.Buy, "a", 2, 2), map) should be(None)
      map("a")((1, 1, OrderType.Sell)) should be('nonEmpty)
    }
  }
  describe("addOrMatch method") {
    implicit val ec = scala.concurrent.ExecutionContext.global
    it("should store orders without pair") {
      val order = Order(1, "1", OrderType.Sell, "a", 1, 2)
      val b = new SingleThreadOrderBook
      val addFuture = b.addOrMatch(order)
      whenReady(addFuture) { result =>
        b.instrumentsOrderBook("a")((1, 2, OrderType.Sell)) should be('nonEmpty)
        result should be(Added)
      }
    }
    it("should match reverse orders") {
      val order = Order(1, "1", OrderType.Sell, "a", 1, 2)
      val reverseOrder = Order(1, "1", OrderType.Buy, "a", 1, 2)
      val b = new SingleThreadOrderBook
      val addFuture = b.addOrMatch(order)
      val matchFuture = b.addOrMatch(reverseOrder)
      whenReady(Future.sequence(Seq(addFuture, matchFuture))) { result =>
        result(0) should be(Added)
        result(1) should be(Matched(ReverseOrders(reverseOrder, order)))
        b.instrumentsOrderBook("a")((1, 2, OrderType.Sell)) should be('empty)
      }
    }
  }
}
