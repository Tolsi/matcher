package ru.tolsi.matcher

import java.io.File
import com.typesafe.scalalogging.StrictLogging
import ru.tolsi.matcher.util.EitherUtils._

object LoadExampleData {
  val defaultOrdersFilePath = "/orders.txt"
  val defaultClientsFilePath = "/clients.txt"
}
trait LoadExampleData extends StrictLogging {
  import LoadExampleData._
  def loadClients(pathOpt: Option[String] = None): Seq[ClientInfo] = {
    val (clientsErrors, clients) = splitEitherIterator(
      ClientInfo.loadFromFile(
        pathOpt.map(new File(_)).getOrElse(new File(getClass.getResource(defaultClientsFilePath).toURI))))
    clientsErrors.foreach(logger.warn(_))
    clients.toSeq
  }
  def loadCreateOrdersRequests(pathOpt: Option[String] = None): Seq[OrderOperation.Create] = {
    val (ordersErrors, createOrdersRequests) = splitEitherIterator(
      OrderOperation.Create.loadFromFile(
        pathOpt.map(new File(_)).getOrElse(new File(getClass.getResource(defaultOrdersFilePath).toURI))))
    ordersErrors.foreach(logger.warn(_))
    createOrdersRequests.toSeq
  }
}
