package ru.tolsi.matcher

import org.scalatest._
import org.scalatest.concurrent.ScalaFutures

abstract class UnitSpec extends FunSpec with Matchers with
  OptionValues with Inside with Inspectors with ScalaFutures with BeforeAndAfter
