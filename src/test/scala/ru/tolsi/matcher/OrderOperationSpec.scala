package ru.tolsi.matcher

import java.io.File
import scala.util._
import ru.tolsi.matcher.util.EitherUtils._

trait OrderParserSpec { self: UnitSpec =>
  def checkOrdersErrors(errors: Seq[String]): Unit = {
    assert(errors.size == 2)
    errors should contain inOrderOnly(
      "Can't parse order info from line #2: 111!1",
      "Can't parse order info from line #6: oops"
    )
  }
  def checkOrders(orders: Seq[OrderOperation.Create]): Unit = {
    assert(orders.size == 4)
    orders should contain inOrderOnly(
      OrderOperation.Create("C8", OrderType.Buy, "C", 15, 4),
      OrderOperation.Create("C2", OrderType.Sell, "C", 14, 5),
      OrderOperation.Create("C2", OrderType.Sell, "C", 13, 2),
      OrderOperation.Create("C9", OrderType.Buy, "B", 6, 4)
    )
  }
}

class OrderOperationSpec extends UnitSpec with OrderParserSpec {
  describe("OrderOperation.Create") {
    describe("parseFromLine method") {
      it("should load OrderOperation.Create from correct line") {
        OrderOperation.Create.parseFromLine("A\tb\tA\t3\t5") should
          be(Success(OrderOperation.Create("A", OrderType.Buy, "A", 3, 5)))
      }
      it("should not load OrderOperation.Create from line with empty user id") {
        OrderOperation.Create.parseFromLine("\tb\tA\t3\t5") should be('failure)
      }
      it("should not load OrderOperation.Create from line with empty asset") {
        OrderOperation.Create.parseFromLine("A\tb\t\t3\t5") should be('failure)
      }
      it("should not load OrderOperation.Create from line with incorrect order type") {
        OrderOperation.Create.parseFromLine("A\tz\tA\t3\t5") should be('failure)
      }
      it("should not load OrderOperation.Create from line with negative price") {
        OrderOperation.Create.parseFromLine("A\tz\tA\t-3\t5") should be('failure)
      }
      it("should not load OrderOperation.Create from line with negative qty") {
        OrderOperation.Create.parseFromLine("A\tz\tA\t3\t-5") should be('failure)
      }
      it("should not load OrderOperation.Create from line with zero price") {
        OrderOperation.Create.parseFromLine("A\tz\tA\t0\t5") should be('failure)
      }
      it("should not load OrderOperation.Create from line with zero qty") {
        OrderOperation.Create.parseFromLine("A\tz\tA\t3\t0") should be('failure)
      }
      it("should not load OrderOperation.Create from incorrect line") {
        OrderOperation.Create.parseFromLine("omgpony") should be('failure)
      }
    }
    describe("loadFromFile method") {
      it("should load ClientInfo from file") {
        val (errors, clients) = splitEitherIterator(OrderOperation.Create.loadFromFile(
          new File(getClass.getResource("/orders.txt").toURI)))
        checkOrdersErrors(errors.toList)
        checkOrders(clients.toList)
      }
    }
    describe("on create") {
      it("should fail with empty id") {
        an[IllegalArgumentException] should be thrownBy OrderOperation.Create("", OrderType.Buy, "C", 15, 4)
      }
      it("should fail with empty asset") {
        an[IllegalArgumentException] should be thrownBy OrderOperation.Create("A", OrderType.Buy, "", 15, 4)
      }
      it("should fail with negative price") {
        an[IllegalArgumentException] should be thrownBy OrderOperation.Create("A", OrderType.Buy, "", -15, 4)
      }
      it("should fail with negative qty") {
        an[IllegalArgumentException] should be thrownBy OrderOperation.Create("A", OrderType.Buy, "", 15, -4)
      }
      it("should fail with zero price") {
        an[IllegalArgumentException] should be thrownBy OrderOperation.Create("A", OrderType.Buy, "", 0, 4)
      }
      it("should fail with zero qty") {
        an[IllegalArgumentException] should be thrownBy OrderOperation.Create("A", OrderType.Buy, "", 15, 0)
      }
    }
  }
}
