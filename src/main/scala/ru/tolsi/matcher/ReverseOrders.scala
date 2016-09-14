package ru.tolsi.matcher

case class ReverseOrders(order: Order, reverseOrder: Order) {
  assert(order.price == reverseOrder.price)
  assert(order.qty == reverseOrder.qty)
  assert(order.`type` != reverseOrder.`type`)
}
