package ru.tolsi.matcher

import java.io.File
import java.net.URI
import com.typesafe.scalalogging.StrictLogging
import ru.tolsi.matcher.util.EitherUtils._

trait LoadExampleData extends StrictLogging {
  def loadClients(pathOpt: Option[String] = None): Seq[ClientInfo] = {
    val (clientsErrors, clients) = splitEitherIterator(
      ClientInfo.loadFromFile(
        new File(
          pathOpt.map(new URI(_)).getOrElse(getClass.getResource("/clients.txt").toURI))))
    clientsErrors.foreach(logger.warn(_))
    clients.toSeq
  }
  def loadCreateOrdersRequests(pathOpt: Option[String] = None): Seq[OrderOperation.Create] = {
    val (ordersErrors, createOrdersRequests) = splitEitherIterator(
      OrderOperation.Create.loadFromFile(
        new File(pathOpt.map(new URI(_)).getOrElse(getClass.getResource("/orders.txt").toURI))))
    ordersErrors.foreach(logger.warn(_))
    createOrdersRequests.toSeq
  }
}
