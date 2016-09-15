package ru.tolsi.matcher

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import com.typesafe.scalalogging.StrictLogging
import org.slf4j.MarkerFactory

abstract class AbstractExample[C <: Client[Long]](
    buildClient: ClientInfo => C,
    clientRepository: Seq[C] => ClientRepository[Long],
    orderExecutor: ReverseOrdersExecutor[Long],
    orderBook: OrderBook)(implicit val ec: ExecutionContext) extends App with LoadExampleData with StrictLogging {

  val ioEc = ExecutionContext.global

  val loadClientsFuture = Future(loadClients(args.lift(0)).map(buildClient))(ioEc)
  val loadCreateOrdersRequestsFuture = Future(loadCreateOrdersRequests(args.lift(1)))(ioEc)

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
      logger.info(MarkerFactory.getMarker("results"), Seq(id, balances("USD"), balances("A"), balances("B"), balances(
        "C"), balances("D")).mkString("\t"))
    }
    logger.debug(s"Final results:")
    clientsBalances.toSeq.sortBy(_._1).foreach(logClientBalance)
  }
  Await.result(processFuture, Duration.Inf)
  logger.debug("Calculation finished")
}
