package ru.tolsi.matcher.util

import ru.tolsi.matcher.UnitSpec

class EitherUtilsSpec extends UnitSpec {
  describe("splitEitherIterator method") {
    it("should split either iterator to left and right iterators") {
      val (lefts, rigths) = EitherUtils.splitEitherIterator(Iterator(Left(0), Right(3), Left(1), Left(2), Right(4),
        Right(5)))
      lefts.toSeq contains inOrderOnly(0, 1, 2)
      rigths.toSeq contains inOrderOnly(3, 4, 5)
    }
    it("should split empty either iterator") {
      val (lefts, rigths) = EitherUtils.splitEitherIterator(Iterator.empty)
      lefts.toSeq should have size (0)
      rigths.toSeq should have size (0)
    }
  }
}
