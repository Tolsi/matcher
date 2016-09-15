package ru.tolsi.matcher

object OrderType extends Enumeration {
  val Sell = Value("sell")
  val Buy = Value("buy")
  def invert(value: OrderType.Value): OrderType.Value = value match {
    case Sell => Buy
    case Buy => Sell
  }
}

case class Order(id: Long, creator: String, `type`: OrderType.Value, asset: String, price: Int, qty: Int) {
  require(!creator.isEmpty)
  require(!asset.isEmpty)
  require(qty > 0)
  require(price > 0)
}
