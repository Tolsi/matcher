package ru.tolsi.matcher.naive

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext
import ru.tolsi.matcher.{AbstractExchangeSpec, ClientInfo, UnitSpec}

class ExchangeSpec extends UnitSpec
  with AbstractExchangeSpec[ThreadUnsafeClientRepository, SingleThreadOrderBook, SingleThreadOrderExecutor] {
  override def ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())
  override def buildClientRepo(clients: Seq[ClientInfo]): ThreadUnsafeClientRepository = ThreadUnsafeClientRepository(
    clients.map(ThreadUnsafeClient.fromClientInfo))
  override def orderBook: SingleThreadOrderBook = new SingleThreadOrderBook
  override def ordersExecutor: SingleThreadOrderExecutor = new SingleThreadOrderExecutor

  override def extraCheck(
      repo: ThreadUnsafeClientRepository,
      orderBook: SingleThreadOrderBook,
      executor: SingleThreadOrderExecutor): Unit = {
    forAll(orderBook.instrumentsOrderBook.values.flatMap(_.values.map(_.isEmpty))) { isEmpty =>
      isEmpty should be(true)
    }
  }
}
