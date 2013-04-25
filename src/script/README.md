# Sample Scripts

To run the sample scripts, start the shell and load file:

    $ ./bin/shell
    > $l dphil.m

## dphil.m
Implementation of the "Dining Philosophers" problem.
http://en.wikipedia.org/wiki/Dining_philosophers_problem

## prballs.m

## prbrain.m

## primage.m

## primesieve.m

## prsg.m

## prsort.m

## spell.m
Spelling corrector based on http://norvig.com/spell-correct.html
Executes the correction testcases in both serial mode and parallel mode while
recording the execution time for each.

## timeflies.m
Time flies example, inspired by
http://blogs.msdn.com/b/jeffva/archive/2010/03/17/reactive-extensions-for-javascript-the-time-flies-like-an-arrow-sample.aspx

This is a gui application that produces a mouse trail with the string
"time flies like an arrow".


## urlpoll.m
Port of Go example http://golang.org/doc/codewalk/sharemem/, which uses
channels and message passing to orchestrate a fixed number of concurrent
consumer tasks over shared work queue. The code is somewhat heavily shaped
by the channel topology and message passing logistics. Here we simply share
state directly.

This version also adds some object-like encapsulation: the pollsvc() function
returns a record with "methods" for starting and stopping the service, for
adding and removing URLs from the poll, and for obtaining a snapshot of the
current status map.

## wrap

## misc/bank.m
Bank account demo showing concurrent withdrawl and deposit while maintaining
account balance integrity.

When the samples runs a transfer() and a withdraw() are concurrently executed.
Whichever one is executed first will cause the other to then fail due to
insufficient funds.

If the transfer succeeds, the final balances are mark 50, tami 250.
If the withdrawal succeeds, the final balances are mark 25, tami 200.

## misc/fact.m
Contains three different implementations for calculating a factorial:
1. recusion
2. reduce
3. iteration

## misc/fib.m
Contains two different implementation for generating a fibonacci series:
1. recurison
2. iteration

## misc/mapred.m
Map-reduce

## misc/ss.m
HTTP server that logs the request headers and keeps track of the number of
times each request has been made. The server runs on port 8080, so once you
start the server you can make requests via http://localhost:8080.

## misc/tween.m

## misc/variadic.m