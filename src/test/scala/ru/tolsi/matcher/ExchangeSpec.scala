package ru.tolsi.matcher

class ExchangeSpec extends UnitSpec {
  describe("createOrder method") {
    it("should create order") {
      Exchange.createOrder(1, OrderOperation.Create("A", OrderType.Buy, "A", 1, 3)) should
        be(Order(1, "A", OrderType.Buy, "A", 1, 3))
    }
  }
}
