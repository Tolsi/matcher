package ru.tolsi.matcher.naive

import ru.tolsi.matcher.AbstractExample

object Main extends AbstractExample(ThreadUnsafeClientRepository.apply, SingleThreadOrderExecutor, new SingleThreadOrderBook)
