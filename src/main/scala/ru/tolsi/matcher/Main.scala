package ru.tolsi.matcher

import java.io.File
import scala.collection.mutable
import scala.io.Source
import scala.util.{Failure, Success, Try}
import EitherUtils._
import com.typesafe.scalalogging.StrictLogging
import org.slf4j.{Marker, MarkerFactory}
import org.slf4j.helpers.BasicMarker

object EitherUtils {
  def splitEitherList[A, B](el: Iterator[Either[A, B]]): (Iterator[A], Iterator[B]) = {
    val (lefts, rights) = el.partition(_.isLeft)
    (lefts.map(_.left.get), rights.map(_.right.get))
  }
}

object Main extends App with StrictLogging {
  object ClientInfo {
    private[matcher] def loadFromFile(file: File): Iterator[Either[String, ClientInfo]] = {
      Source.fromFile(file).getLines.zipWithIndex.map(l => l -> parseFromLine(l._1)).map {
        case (_, Success(info)) => Right(info)
        case (line, Failure(f)) => Left(s"Can't parse client info from line #${line._2 + 1 }: ${line._1 }")
      }
    }
    // todo move to example class
    private[matcher] def parseFromLine(line: String): Try[ClientInfo] = Try {
      val array = line.split("\t")
      ClientInfo(array(0), Balances(array(1).toLong, Map("a" -> array(2).toLong, "b" -> array(3).toLong, "c" -> array(4)
        .toLong, "d" -> array(5).toLong).withDefaultValue(0L)))
    }
  }
  case class Balances(usdBalance: Long, assetsBalances: Map[String, Long])
  case class ClientInfo(id: String, balances: Balances)

  trait OrderType {
    def apply(sellerBalance: Balances, buyerBalance: Balances, order: Order): (Balances, Balances)
  }
  object OrderType {
    object Sell extends OrderType {
      def apply(sellerBalance: Balances, buyerBalance: Balances, order: Order): (Balances, Balances) = {
        import order.info._
        val sellerUpdatedBalance = sellerBalance.copy(usdBalance = sellerBalance.usdBalance + (price * qty)).copy(
          assetsBalances = sellerBalance.assetsBalances + (asset -> (sellerBalance.assetsBalances(asset) - qty)))
        val buyerUpdatedBalance = buyerBalance.copy(usdBalance = buyerBalance.usdBalance - (price * qty)).copy(
          assetsBalances = buyerBalance.assetsBalances + (asset -> (buyerBalance.assetsBalances(asset) + qty)))
        (sellerBalance, buyerBalance)
      }
      override def toString: String = "sell"
    }
    object Buy extends OrderType {
      def apply(sellerBalance: Balances, buyerBalance: Balances, order: Order): (Balances, Balances) =
        Sell.apply(buyerBalance, sellerBalance, order).swap
      override def toString: String = "buy"
    }
    def invert(value: OrderType): OrderType = value match {
      case Sell => Buy
      case Buy => Sell
    }
    def withName(name: String): OrderType = name match {
      case "b" => Buy
      case "s" => Sell
      case other => throw new IllegalStateException(s"Can't find order type with value '$other'")
    }
  }

  trait OrderOperation
  object OrderOperation {
    object Create {
      private[matcher] def loadFromFile(file: File): Iterator[Either[String, Create]] = {
        Source.fromFile(file).getLines.zipWithIndex.map(l => l -> parseFromLine(l._1)).map {
          case (_, Success(info)) => Right(info)
          case (line, Failure(f)) => Left(s"Can't parse order info from line #${line._2 + 1 }: ${line._1 }")
        }
      }

      // todo move to example class
      private[matcher] def parseFromLine(line: String): Try[Create] = Try {
        val array = line.split("\t")
        Create(array(0), OrderType.withName(array(1)), array(2), array(3).toInt, array(4).toInt)
      }
    }
    case class Create(creator: String, `type`: OrderType, asset: String, price: Int, qty: Int)
      extends OrderOperation
  }
  case class Order(ts: Long, info: OrderOperation.Create)

  val (clientsErrors, clients) = splitEitherList(ClientInfo.loadFromFile(new File(getClass.getResource("/clients.txt")
    .toURI)))
  val (ordersErrors, createOrdersRequests) = splitEitherList(OrderOperation.Create.loadFromFile(new File(
    getClass.getResource(
      "/orders.txt").toURI)))
  (clientsErrors ++ ordersErrors).foreach(logger.warn(_))

  val clientsMap = new mutable.AnyRefMap[String, Balances](clients.map(c => c.id -> c.balances).toMap)
  object OrderBook {
    def findOrderWhichCanApply(
        order: Order,
        orderBook: collection.Map[String, collection.Map[(Int, OrderType), Order]]): Option[Order] = {
      import order.info._
      instrumentsOrderBook.get(order.info.asset).flatMap(_.get((price, OrderType.invert(`type`))))
    }
  }
  val instrumentsOrderBook = mutable.AnyRefMap.empty[String, mutable.AnyRefMap[(Int, OrderType), Order]]
  createOrdersRequests.toSeq.zipWithIndex.map{ case (r, id) => Order(id, r) }.foreach(order => {
    def logOrderResult(order: Order): Unit = logger.debug(
      s"Order created at '${order.ts}' was executed! User '${order.info.creator }' ${order.info.`type` } ${order.info.qty } '${order.info.asset }' for ${order.info.price } USD")
    OrderBook.findOrderWhichCanApply(order, instrumentsOrderBook) match {
      case Some(executedOrder) =>
        logOrderResult(order)
        logOrderResult(executedOrder)
        val (firstUpdated, secondUpdated) = order.info.`type`(clientsMap(order.info.creator), clientsMap(
          executedOrder.info.creator), order)
        clientsMap += order.info.creator -> firstUpdated
        clientsMap += executedOrder.info.creator -> secondUpdated
      case None =>
        instrumentsOrderBook += order.info.asset -> (instrumentsOrderBook.getOrElse(order.info.asset,
          mutable.AnyRefMap.empty) += (order.info.price, order.info.`type`) -> order)
    }
  })
  logger.info(s"Final results:")
  val resultsMarker = MarkerFactory.getMarker("results")
  clientsMap.toSeq.sortBy(_._1).foreach { case (id, balance) =>
    logger.info(resultsMarker, Seq(id, balance.usdBalance, balance.assetsBalances("a"), balance.assetsBalances("b"),
      balance.assetsBalances("c"), balance.assetsBalances("d")).mkString("\t"))
  }
}
