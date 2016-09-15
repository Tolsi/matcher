package ru.tolsi.matcher.naive

import scala.concurrent.Future
import ru.tolsi.matcher.{ClientInfo, Order, OrderType, ReverseOrders, UnitSpec}

class SingleThreadOrderExecutorSpec extends UnitSpec {
  describe("execute method") {
    implicit val ec = scala.concurrent.ExecutionContext.global
    it("should execure reverse orders on users from repo") {
      val executor = new SingleThreadOrderExecutor
      val repo = ThreadUnsafeClientRepository(Seq(ClientInfo("1", 0L, 0L, 0L, 0L, 0L), ClientInfo("2", 0L, 0L, 0L, 0L, 0L)).map(ThreadUnsafeClient.fromClientInfo))
      val future = executor.execute(ReverseOrders(Order(1, "1", OrderType.Buy, "A", 1, 1), Order(1, "2", OrderType.Sell, "A", 1, 1)), repo)
      val balancesFuture = for {
        _ <- future
        users <- repo.getAll
        userBalances <- Future.sequence(users.map(u => u.getAllBalances.map(b => u.id -> b)))
      } yield userBalances
      whenReady(balancesFuture) { case userBalances =>
        val userBalancesMap = userBalances.toMap
        userBalancesMap("1")("A") should be (1)
        userBalancesMap("1")("USD") should be (-1)
        userBalancesMap("2")("A") should be (-1)
        userBalancesMap("2")("USD") should be (1)
      }
    }
    it("should execure reverse orders of the same user") {
      val executor = new SingleThreadOrderExecutor
      val repo = ThreadUnsafeClientRepository(Seq(ClientInfo("1", 0L, 0L, 0L, 0L, 0L), ClientInfo("1", 0L, 0L, 0L, 0L, 0L)).map(ThreadUnsafeClient.fromClientInfo))
      val future = executor.execute(ReverseOrders(Order(1, "1", OrderType.Buy, "A", 1, 1), Order(1, "1", OrderType.Sell, "A", 1, 1)), repo)
      val balancesFuture = for {
        _ <- future
        users <- repo.getAll
        userBalances <- Future.sequence(users.map(u => u.getAllBalances.map(b => u.id -> b)))
      } yield userBalances
      whenReady(balancesFuture) { case userBalances =>
        val userBalancesMap = userBalances.toMap
        userBalancesMap("1")("A") should be (0)
        userBalancesMap("1")("USD") should be (0)
      }
    }
  }
}
