package com.example.demo

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.runBlocking

val primeArray = generatePrimeArray()
var primeMatrix = listOf<List<Int>>()
fun generatePrimeMatrix(rowSize: Int) {

    val resultMatrix = mutableListOf<List<Int>>()
    repeat(5) {
        resultMatrix.add(primeArray.shuffled().subList(0, rowSize))
    }
    primeMatrix = resultMatrix
}

fun getSequences(): MutableList<List<Int>> {
    val resultSequences = mutableListOf<List<Int>>()
    for (row in primeMatrix) {
        resultSequences.add(row.shuffled().subList(0, 6))
    }
    return resultSequences
}

fun generatePrimeArray(): List<Int> {
    val primes = mutableListOf<Int>()
    runBlocking {
        var cur = numbersFrom(2)
        repeat(1000) {
            val prime = cur.receive()
            primes.add(prime)
            cur = filter(cur, prime)
        }
        coroutineContext.cancelChildren() // cancel all children to let main finish
    }
    return primes
}

fun CoroutineScope.numbersFrom(start: Int) = produce<Int> {
    var x = start
    while (true) send(x++) // infinite stream of integers from start
}

fun CoroutineScope.filter(numbers: ReceiveChannel<Int>, prime: Int) = produce<Int> {
    for (x in numbers) if (x % prime != 0) send(x)
}
