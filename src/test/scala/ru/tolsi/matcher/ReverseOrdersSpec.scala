package ru.tolsi.matcher

class ReverseOrdersSpec extends UnitSpec {
  describe("on create") {
    it("should fail with not reverse orders") {
      an[IllegalArgumentException] should be thrownBy ReverseOrders(Order(1, "A", OrderType.Buy, "A", 1, 1), Order(1,
        "A", OrderType.Buy, "A", 1, 1))
      an[IllegalArgumentException] should be thrownBy ReverseOrders(Order(1, "A", OrderType.Buy, "A", 1, 1), Order(1,
        "A", OrderType.Sell, "A", 2, 1))
      an[IllegalArgumentException] should be thrownBy ReverseOrders(Order(1, "A", OrderType.Buy, "A", 1, 1), Order(1,
        "A", OrderType.Sell, "A", 1, 2))
    }
  }
}
