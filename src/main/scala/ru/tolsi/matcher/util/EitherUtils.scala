package ru.tolsi.matcher.util

object EitherUtils {
  // todo test
  def splitEitherIterator[A, B](el: Iterator[Either[A, B]]): (Iterator[A], Iterator[B]) = {
    val (lefts, rights) = el.partition(_.isLeft)
    (lefts.map(_.left.get), rights.map(_.right.get))
  }
}
