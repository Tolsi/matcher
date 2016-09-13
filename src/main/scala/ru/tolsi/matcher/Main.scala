package ru.tolsi.matcher

import java.io.File
import scala.collection.mutable
import scala.io.Source
import scala.util.{Failure, Success, Try}
import com.typesafe.scalalogging.StrictLogging
import org.slf4j.MarkerFactory
import ru.tolsi.matcher.EitherUtils._

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
      val cashBalance = new mutable.AnyRefMap[String, Long]().withDefaultValue(0L)
      cashBalance += "USD" -> array(1).toLong
      val assetsBalance = new mutable.AnyRefMap[String, Long]().withDefaultValue(0L)
      Seq("A" -> array(2).toLong, "B" -> array(3).toLong, "C" -> array(4)
        .toLong, "D" -> array(5).toLong).foreach(assetsBalance +=)
      ClientInfo(array(0), new Balances(cashBalance, assetsBalance))
    }
  }
  class Balances(val cashBalance: mutable.Map[String, Long], val assetsBalances: mutable.Map[String, Long])
  case class ClientInfo(id: String, balances: Balances)

  trait OrderType {
    def apply(sellerBalance: Balances, buyerBalance: Balances, order: Order): Unit
  }
  object OrderType {
    object Sell extends OrderType {
      def apply(sellerBalance: Balances, buyerBalance: Balances, order: Order): Unit = {
        import order.info._
        sellerBalance.cashBalance += ("USD" -> (sellerBalance.cashBalance("USD") + (price * qty)))
        sellerBalance.assetsBalances += (asset -> (sellerBalance.assetsBalances(asset) - qty))
        buyerBalance.cashBalance += ("USD" -> (buyerBalance.cashBalance("USD") - (price * qty)))
        buyerBalance.assetsBalances += (asset -> (buyerBalance.assetsBalances(asset) + qty))
      }
      override def toString: String = "sell"
    }
    object Buy extends OrderType {
      def apply(sellerBalance: Balances, buyerBalance: Balances, order: Order): Unit =
        Sell.apply(buyerBalance, sellerBalance, order)
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

  val (clientsErrors, clients) = splitEitherList(ClientInfo.loadFromFile(
    new File(getClass.getResource("/clients.txt").toURI)))
  val (ordersErrors, createOrdersRequests) = splitEitherList(OrderOperation.Create.loadFromFile(new File(
    getClass.getResource(
      "/orders.txt").toURI)))
  (clientsErrors ++ ordersErrors).foreach(logger.warn(_))

  val clientsMap = new mutable.AnyRefMap[String, Balances](10)
  clients.foreach(c => clientsMap += c.id -> c.balances)

  object OrderBook {
    def findOrderWhichCanApply(
        order: Order,
        orderBook: collection.Map[String, collection.Map[(Int, Int, OrderType), Order]]): Option[Order] = {
      import order.info._
      instrumentsOrderBook.get(order.info.asset).flatMap(_.get((price, qty, OrderType.invert(`type`))))
    }
  }
  val instrumentsOrderBook = mutable.AnyRefMap.empty[String, mutable.AnyRefMap[(Int, Int, OrderType), Order]]
  def balanceString(balance: Balances): String = {
    Seq(balance.cashBalance("USD"), balance.assetsBalances("A"), balance.assetsBalances("B"),
      balance.assetsBalances("C"), balance.assetsBalances("D")).mkString("\t")
  }
  createOrdersRequests.zipWithIndex.map { case (r, id) => Order(id, r) }.foreach(order => {
    def logOrderResult(order: Order): Unit = logger.debug(
      s"Order with id '${order.ts }' was executed! User '${order.info.creator }' ${order.info.`type` } ${
        order.info
          .qty
      } '${order.info.asset }' for ${order.info.price } USD each (total ${order.info.qty * order.info.price } USD)")
    def logUserBalanceUpdate(id: String, balance: Balances): Unit = {
      logger.debug(s"User $id balance was updated: '${balanceString(balance) }'")
    }
    OrderBook.findOrderWhichCanApply(order, instrumentsOrderBook) match {
      case Some(executedOrder) =>
        logger.debug(s"Executing $order and $executedOrder was started")
        logger.debug(s"Users balances before:")
        logUserBalanceUpdate(order.info.creator, clientsMap(order.info.creator))
        logUserBalanceUpdate(executedOrder.info.creator, clientsMap(executedOrder.info.creator))

        order.info.`type`(clientsMap(order.info.creator), clientsMap(executedOrder.info.creator), order)

        logOrderResult(order)
        logOrderResult(executedOrder)

        logger.debug(s"Users balances after:")
        logUserBalanceUpdate(order.info.creator, clientsMap(order.info.creator))
        logUserBalanceUpdate(executedOrder.info.creator, clientsMap(executedOrder.info.creator))

        instrumentsOrderBook += order.info.asset -> (instrumentsOrderBook(order.info.asset) - ((executedOrder.info
          .price, executedOrder.info.qty, OrderType.invert(executedOrder.info.`type`))))
        logger.debug(s"Executing $order and $executedOrder was finished")
      case None =>
        instrumentsOrderBook += order.info.asset -> (instrumentsOrderBook.getOrElse(order.info.asset,
          mutable.AnyRefMap.empty) += ((order.info.price, order.info.qty, order.info.`type`) -> order))
    }
  })
  logger.info(s"Final results:")
  val resultsMarker = MarkerFactory.getMarker("results")
  clientsMap.toSeq.sortBy(_._1).foreach { case (id, balance) =>
    logger.info(resultsMarker, s"$id\t${balanceString(balance) }")
  }
}
