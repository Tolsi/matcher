package ru.tolsi.matcher

import scala.concurrent.duration.Duration
import scala.concurrent.forkjoin.ForkJoinPool
import scala.concurrent.{Await, ExecutionContext, Future}
import com.typesafe.scalalogging.StrictLogging
import org.slf4j.MarkerFactory

abstract class AbstractExample[C <: Client[Long]](
    buildClient: ClientInfo => C,
    clientRepository: Seq[C] => ClientRepository[Long],
    orderExecutor: ReverseOrdersExecutor[Long],
    orderBook: OrderBook) extends App with LoadExampleData with StrictLogging {

  val ioEc = ExecutionContext.global
  implicit val ec = ExecutionContext.fromExecutor(new ForkJoinPool(1))

  val loadClientsFuture = Future(loadClients.map(buildClient))(ioEc)
  val loadCreateOrdersRequestsFuture = Future(loadCreateOrdersRequests)(ioEc)

  val processFuture = for {
    clients <- loadClientsFuture
    clientsRepository = clientRepository(clients)
    createOrdersRequests <- loadCreateOrdersRequestsFuture
    _ <- {
      logger.debug("Calculation started")
      val exchange = new Exchange(clientsRepository, orderExecutor, orderBook)
      exchange(createOrdersRequests)
    }
    clients <- clientsRepository.getAll
    clientsBalances <- Future.sequence(clients.map(c => c.getAllBalances.map(balance => c.id -> balance)))
  } yield {
    def logClientBalance(userInfo: (String, Map[String, Long])): Unit = {
      val (id, balances) = userInfo
      // todo log to file with marker
      logger.info(MarkerFactory.getMarker("results"), Seq(id, balances("USD"), balances("A"), balances("B"), balances(
        "C"), balances("D")).mkString("\t"))
    }
    logger.info(s"Final results:")
    clientsBalances.toSeq.sortBy(_._1).foreach(logClientBalance)
  }
  Await.result(processFuture, Duration.Inf)
  logger.debug("Calculation finished")
}
