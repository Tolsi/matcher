package ru.tolsi.matcher.naive

import scala.concurrent.ExecutionContext
import scala.concurrent.forkjoin.ForkJoinPool
import ru.tolsi.matcher.AbstractExample

object Main extends AbstractExample(ThreadUnsafeClient.fromClientInfo,
  ThreadUnsafeClientRepository.apply,
  SingleThreadOrderExecutor,
  new SingleThreadOrderBook)(ExecutionContext.fromExecutor(new ForkJoinPool(1)))
