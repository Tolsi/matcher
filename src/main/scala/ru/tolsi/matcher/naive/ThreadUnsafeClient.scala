package ru.tolsi.matcher.naive

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}
import ru.tolsi.matcher.{Client, ClientInfo}

private[naive] object ThreadUnsafeClient {
  def fromClientInfo(clientInfo: ClientInfo): ThreadUnsafeClient = {
    val balances = new mutable.AnyRefMap[String, Long]()
    Seq("USD" -> clientInfo.usdBalance, "A" -> clientInfo.aBalance, "B" -> clientInfo.bBalance,
      "C" -> clientInfo.cBalance, "D" -> clientInfo.dBalance).foreach(balances += _)
    new ThreadUnsafeClient(clientInfo.id, balances)
  }
}

private[naive] class ThreadUnsafeClient(val id: String, private[naive] val balances: mutable.AnyRefMap[String, Long])
  extends Client[Long] {
  require(!id.isEmpty)
  override def getBalance(asset: String)(implicit ec: ExecutionContext): Future[Long] =
    Future.successful(balances.getOrElse(asset, 0L))
  override def getAllBalances(implicit ec: ExecutionContext): Future[Map[String, Long]] =
    Future.successful(balances.toMap.withDefaultValue(0L))
  override def addDeltaToBalance(asset: String, value: Long)(implicit ec: ExecutionContext): Future[Unit] =
    Future.successful(balances += asset -> (balances.getOrElseUpdate(asset, 0L) + value))
}
