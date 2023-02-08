package com.esop.utils

object Counter {
    private var counter = 0L
    fun next(): Long {
        return counter++
    }
}