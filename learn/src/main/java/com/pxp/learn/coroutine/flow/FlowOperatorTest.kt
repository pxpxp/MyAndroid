package com.pxp.learn.coroutine.flow

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.reduce
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.runBlocking
import org.junit.Test

class FlowOperatorTest {

    @Test
    fun test_flow_back_pressure() = runBlocking {
        (1..5).asFlow()
//            .flowOn(Dispatchers.IO)
//            .buffer(50)
//            .conflate() //只打印 1 5
//            .collectLatest {  //只打印5
            .collect {
                delay(1000)
                println("collect $it ${Thread.currentThread().name}")
            }
    }

    @Test
    fun test_flow_transform() = runBlocking {
        (1..5).asFlow()
            .transform {
                emit("making request1 $it")
                emit("making request2 $it")
            }
            .collect {
                println("$it")
            }
    }

    @Test
    fun test_flow_limit() = runBlocking {
        flow {
            try {
                emit(1)
                emit(2)
                println("this line will not execute")
                emit(3)
            } finally {
                println("finally in number")
            }
        }.take(2)
            .collect {
                println("$it")
            }
    }

    @Test
    fun test_flow_reduce() = runBlocking {
        (1..5).asFlow()
            .map {
                it * it
            }
            .reduce { accumulator, value ->
                println("$accumulator  $value")
                accumulator + value
            }
        println("done")
    }

    @Test
    fun test_flow_zip() = runBlocking {
        val nums = (1..3).asFlow().onEach { delay(300) }
        val strs = flowOf("one", "two", "three").onEach { delay(400) }
        val startTime = System.currentTimeMillis()
        nums.zip(strs) { a, b ->
            "$a -> $b"
        }.collect {
            println("$it as ${System.currentTimeMillis() - startTime} ms form start")
        }
    }


}