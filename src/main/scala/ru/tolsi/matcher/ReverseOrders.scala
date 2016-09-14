package ru.tolsi.matcher

// todo test
case class ReverseOrders(order: Order, reverseOrder: Order) {
  require(order.price == reverseOrder.price)
  require(order.qty == reverseOrder.qty)
  require(order.`type` != reverseOrder.`type`)
}
