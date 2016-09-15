package ru.tolsi.matcher

import java.io.File
import scala.io.Source
import scala.util.{Failure, Success, Try}

object OrderOperation {
  object Create {
    private[matcher] def loadFromFile(file: File): Iterator[Either[String, Create]] = {
      Source.fromFile(file).getLines.zipWithIndex.map(l => l -> parseFromLine(l._1)).map {
        case (_, Success(info)) => Right(info)
        case (line, Failure(f)) => Left(s"Can't parse order info from line #${line._2 + 1 }: ${line._1 }")
      }
    }
    private[matcher] def parseFromLine(line: String): Try[Create] = Try {
      val array = line.split("\t")
      val orderType = array(1) match {
        case "s" => OrderType.Sell
        case "b" => OrderType.Buy
        case other => throw new IllegalArgumentException(s"Can't parse order type from '$other'")
      }
      Create(array(0), orderType, array(2), array(3).toInt, array(4).toInt)
    }
  }
  case class Create(creator: String, `type`: OrderType.Value, asset: String, price: Int, qty: Int)
    extends OrderOperation {
    require(!creator.isEmpty)
    require(!asset.isEmpty)
    require(qty > 0)
    require(price > 0)
  }
}
trait OrderOperation
