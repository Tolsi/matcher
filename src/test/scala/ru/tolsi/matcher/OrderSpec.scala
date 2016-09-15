package ru.tolsi.matcher

class OrderSpec extends UnitSpec {
  describe("on create") {
    it("should fail with empty creator") {
      an[IllegalArgumentException] should be thrownBy Order(1, "", OrderType.Buy, "A", 3, 4)
    }
    it("should fail with empty asset") {
      an[IllegalArgumentException] should be thrownBy Order(1, "A", OrderType.Buy, "", 3, 4)
    }
    it("should fail with negative price") {
      an[IllegalArgumentException] should be thrownBy Order(1, "", OrderType.Buy, "A", -3, 4)
    }
    it("should fail with negative qty") {
      an[IllegalArgumentException] should be thrownBy Order(1, "", OrderType.Buy, "A", 3, -4)
    }
    it("should fail with zero price") {
      an[IllegalArgumentException] should be thrownBy Order(1, "", OrderType.Buy, "A", 0, 4)
    }
    it("should fail with zero qty") {
      an[IllegalArgumentException] should be thrownBy Order(1, "", OrderType.Buy, "A", 3, 0)
    }
  }
}
