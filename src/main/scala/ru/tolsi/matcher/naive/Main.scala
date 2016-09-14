package ru.tolsi.matcher.naive

import ru.tolsi.matcher.AbstractExample

object Main extends AbstractExample(ThreadUnsafeClient.fromClientInfo,
  ThreadUnsafeClientRepository.apply,
  SingleThreadOrderExecutor,
  new SingleThreadOrderBook)
