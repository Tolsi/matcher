package ru.tolsi.matcher

import scala.concurrent.{ExecutionContext, Future}

object OrderBook {
  type AddOrMatchResult = Either[Unit, ReverseOrders]
  val Added = Left[Unit, ReverseOrders]()
  def Matched(reverseOrders: ReverseOrders) = Right[Unit, ReverseOrders](reverseOrders)
}
abstract class OrderBook {
  import OrderBook._
  def addOrMatch(order: Order)(implicit ec: ExecutionContext): Future[AddOrMatchResult]
}
