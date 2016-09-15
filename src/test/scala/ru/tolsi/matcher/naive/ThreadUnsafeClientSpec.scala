package ru.tolsi.matcher.naive

import scala.collection.mutable
import ru.tolsi.matcher.{ClientInfo, UnitSpec}

class ThreadUnsafeClientSpec extends UnitSpec {
  implicit val ec = scala.concurrent.ExecutionContext.global
  describe("fromClientInfo method") {
    it("should create ThreadUnsafeClient from ClientInfo") {
      val c = ThreadUnsafeClient.fromClientInfo(ClientInfo("1", 0, 1, 2, 3, 4))
      c.id should be("1")
      c.balances should be({
        val m = new mutable.AnyRefMap[String, Long]
        m += "USD" -> 0
        m += "A" -> 1
        m += "B" -> 2
        m += "C" -> 3
        m += "D" -> 4
        m
      })
    }
  }
  describe("getBalance method") {
    it("should return asset balance") {
      val c = ThreadUnsafeClient.fromClientInfo(ClientInfo("1", 0, 1, 2, 3, 4))
      whenReady(c.getBalance("A")) { balance =>
        balance should be (1)
      }
    }
  }
  describe("getAllBalances method") {
    it("should return all asset balances") {
      val c = ThreadUnsafeClient.fromClientInfo(ClientInfo("1", 0, 1, 2, 3, 4))
      whenReady(c.getAllBalances) { balances =>
        balances should be(Map("USD" -> 0L, "A" -> 1L, "B" -> 2L, "C" -> 3L, "D" -> 4L))
      }
    }
  }
  describe("addDeltaToBalance method") {
    it("should update asset by delta") {
      val c = ThreadUnsafeClient.fromClientInfo(ClientInfo("1", 0, 1, 2, 3, 4))
      val future = for {
        _ <- c.addDeltaToBalance("A", 100)
        balances <- c.getAllBalances
      } yield balances
      whenReady(future) { balances =>
        balances should be(Map("USD" -> 0L, "A" -> 101L, "B" -> 2L, "C" -> 3L, "D" -> 4L))
      }
    }
  }
}
