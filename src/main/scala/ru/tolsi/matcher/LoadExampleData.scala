package ru.tolsi.matcher

import java.io.File
import com.typesafe.scalalogging.StrictLogging
import ru.tolsi.matcher.util.EitherUtils._

trait LoadExampleData extends StrictLogging {
  def loadClients: Seq[ClientInfo] = {
    val (clientsErrors, clients) = splitEitherIterator(ClientInfo.loadFromFile(
      new File(getClass.getResource("/clients.txt").toURI)))
    clientsErrors.foreach(logger.warn(_))
    clients.toSeq
  }
  def loadCreateOrdersRequests: Seq[OrderOperation.Create] = {
    val (ordersErrors, createOrdersRequests) = splitEitherIterator(OrderOperation.Create.loadFromFile(new File(
      getClass.getResource("/orders.txt").toURI)))
    ordersErrors.foreach(logger.warn(_))
    createOrdersRequests.toSeq
  }
}
