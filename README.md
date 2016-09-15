# matcher [![Build Status](https://travis-ci.org/Tolsi/matcher.svg?branch=master)](https://travis-ci.org/Tolsi/matcher)

Exchange matcher example.

I would like to add several concurrent and more scalable solutions to this problem in the future , but until there is a single-thread naive solution.

# How to use

## Naive solution
Use can run it using `sbt "runMain ru.tolsi.matcher.naive.Main"` with embedded input files or you can pass absolute paths to input files `sbt "runMain ru.tolsi.matcher.naive.Main /.../clients.txt /.../orders.txt"`.

# Tests
You can start tests using `sbt test`.
