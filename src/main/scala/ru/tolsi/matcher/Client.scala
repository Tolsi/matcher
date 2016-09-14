package ru.tolsi.matcher

import scala.concurrent.{ExecutionContext, Future}

abstract class Client[@specialized T](implicit num: Numeric[T]) {
  def id: String
  def getBalance(asset: String)(implicit ec: ExecutionContext): Future[T]
  def addDeltaToBalance(asset: String, delta: T)(implicit ec: ExecutionContext): Future[Unit]
  def getAllBalances(implicit ec: ExecutionContext): Future[Map[String, T]]
}
