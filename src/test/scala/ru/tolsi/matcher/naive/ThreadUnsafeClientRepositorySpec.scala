package ru.tolsi.matcher.naive

import scala.collection.mutable
import ru.tolsi.matcher.{ClientInfo, UnitSpec}

class ThreadUnsafeClientRepositorySpec extends UnitSpec {
  implicit val ec = scala.concurrent.ExecutionContext.global
  describe("apply method") {
    it("should create ThreadUnsafeClientRepository with given clients") {
      val c = ThreadUnsafeClient.fromClientInfo(ClientInfo("1", 0, 1, 2, 3, 4))
      val repo = ThreadUnsafeClientRepository(Seq(c))
      whenReady(repo.getAll) { users =>
        val user = users.head.asInstanceOf[ThreadUnsafeClient]
        user.id should be("1")
        user.balances should be({
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
  }
  describe("get method") {
    it("should return user by id") {
      val c = ThreadUnsafeClient.fromClientInfo(ClientInfo("1", 0, 1, 2, 3, 4))
      val repo = ThreadUnsafeClientRepository(Seq(c))
      whenReady(repo.get("1")) { userOpt =>
        val user = userOpt.get.asInstanceOf[ThreadUnsafeClient]
        user.id should be("1")
        user.balances should be({
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
    it("should return none for unexisted user by id") {
      val c = ThreadUnsafeClient.fromClientInfo(ClientInfo("1", 0, 1, 2, 3, 4))
      val repo = ThreadUnsafeClientRepository(Seq(c))
      whenReady(repo.get("2")) { userOpt =>
        userOpt should be ('empty)
      }
    }
  }
}
