package ru.tolsi.matcher

import java.io.File
import scala.io.Source
import scala.util.{Failure, Success, Try}

object ClientInfo {
  private[matcher] def loadFromFile(file: File): Iterator[Either[String, ClientInfo]] = {
    Source.fromFile(file).getLines.zipWithIndex.map(l => l -> parseFromLine(l._1)).map {
      case (_, Success(info)) => Right(info)
      case (line, Failure(f)) => Left(s"Can't parse client info from line #${line._2 + 1 }: ${line._1 }")
    }
  }

  private[matcher] def parseFromLine(line: String): Try[ClientInfo] = Try {
    val array = line.split("\t")
    ClientInfo(array(0), array(1).toLong, array(2).toLong, array(3).toLong, array(4).toLong, array(5).toLong)
  }
}
case class ClientInfo(id: String, usdBalance: Long, aBalance: Long, bBalance: Long, cBalance: Long, dBalance: Long)
