package ru.tolsi.matcher

import scala.concurrent.{ExecutionContext, Future}

object OrderBook {
  object AddOrMatchResult {
    val Added = Left[Unit, ReverseOrders]()
    def Matched(reverseOrders: ReverseOrders) = Right[Unit, ReverseOrders](reverseOrders)
  }
  type AddOrMatchResult = Either[Unit, ReverseOrders]
}
abstract class OrderBook {
  import OrderBook._
  def addOrMatch(order: Order)(implicit ec: ExecutionContext): Future[AddOrMatchResult]
}
