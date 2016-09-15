package ru.tolsi.matcher

import java.util.concurrent.Executors
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

trait AbstractExchangeSpec[R <: ClientRepository[Long], B <: OrderBook, E <: ReverseOrdersExecutor[Long]] { self: UnitSpec =>

  def ec: ExecutionContext
  def buildClientRepo(clients: Seq[ClientInfo]): R
  def orderBook: B
  def ordersExecutor: E
  def extraCheck(repo: R, orderBook: B, executor: E): Unit = {}

  describe("apply") {
    it("should match and execute orders in simple case") {
      implicit val ec = ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())
      val repo = buildClientRepo(Seq(
        ClientInfo("a", 1000, 10, 10, 10, 10),
        ClientInfo("b", 1000, 10, 10, 10, 10)
      ))
      val exchange = new Exchange(repo, ordersExecutor, orderBook)
      val resultFuture = for {
        _ <- exchange(Seq(
          OrderOperation.Create("b", OrderType.Buy, "A", 10, 4),
          OrderOperation.Create("b", OrderType.Buy, "A", 15, 4),
          OrderOperation.Create("a", OrderType.Sell, "A", 10, 4),
          OrderOperation.Create("b", OrderType.Sell, "B", 1, 3),
          OrderOperation.Create("a", OrderType.Sell, "A", 15, 4),
          OrderOperation.Create("a", OrderType.Buy, "B", 1, 3)
        ))
        clients <- repo.getAll
        balances <- Future.sequence(clients.map(c => c.getAllBalances.map(balance => c.id -> balance)))
      } yield {
        val balancesMap = balances.toMap
        balancesMap("a") should be(Map("A" -> 2L, "B" -> 13L, "C" -> 10L, "D" -> 10L, "USD" -> 1097L))
        balancesMap("b") should be(Map("A" -> 18L, "B" -> 7L, "C" -> 10L, "D" -> 10L, "USD" -> 903L))
        extraCheck(repo, orderBook, ordersExecutor)
      }
      Await.result(resultFuture, 5 seconds)
    }

    it("should match and execute orders in case same orders from different users") {
      implicit val ec = ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())
      val repo = buildClientRepo(Seq(
        ClientInfo("a", 1000, 10, 10, 10, 10),
        ClientInfo("b", 1000, 10, 10, 10, 10)
      ))
      val exchange = new Exchange(repo, ordersExecutor, orderBook)
      val resultFuture = for {
        _ <- exchange(Seq(
          OrderOperation.Create("b", OrderType.Buy, "A", 10, 4),
          OrderOperation.Create("b", OrderType.Buy, "A", 10, 4),
          OrderOperation.Create("a", OrderType.Sell, "A", 10, 4),
          OrderOperation.Create("b", OrderType.Sell, "B", 1, 3),
          OrderOperation.Create("a", OrderType.Sell, "A", 10, 4),
          OrderOperation.Create("a", OrderType.Buy, "B", 1, 3)
        ))
        clients <- repo.getAll
        balances <- Future.sequence(clients.map(c => c.getAllBalances.map(balance => c.id -> balance)))
      } yield {
        val balancesMap = balances.toMap
        balancesMap("a") should be(Map("A" -> 2L, "B" -> 13L, "C" -> 10L, "D" -> 10L, "USD" -> 1077L))
        balancesMap("b") should be(Map("A" -> 18L, "B" -> 7L, "C" -> 10L, "D" -> 10L, "USD" -> 923L))
        extraCheck(repo, orderBook, ordersExecutor)
      }
      Await.result(resultFuture, 5 seconds)
    }
  }
}
