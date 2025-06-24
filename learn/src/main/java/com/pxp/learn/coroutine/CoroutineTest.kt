package com.example.myandroid.coroutine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.NonCancellable
import org.junit.Test
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import kotlin.system.measureTimeMillis

class CoroutineTest {
    @Test
    fun test_coroutine_builder() = runBlocking {
        val job1 = launch {
            delay(2000)
            println("job1 finished")
        }

        val job2 = async {
            delay(2000)
            println("job1 finished")
            "job2 result"
        }

        println(job2.await())
    }

    @Test
    fun test_coroutine_join() = runBlocking {
        val job1 = launch {
            delay(2000)
            println("job1 finished")
        }
        job1.join()

        val job2 = launch {
            delay(2000)
            println("job2 finished")
        }

        val job3 = launch {
            delay(2000)
            println("job3 finished")
        }
    }

    @Test
    fun test_coroutine_wait() = runBlocking {
        val job1 = async {
            println("job1 finished")
            delay(2000)
        }
//        job1.await()
        job1.join()

        val job2 = async {
            delay(2000)
            println("job2 finished")
        }

        val job3 = async {
            delay(2000)
            println("job3 finished")
        }
    }

    /**
     * 组合并发
     */
    @Test
    fun test_coroutine_measure() = runBlocking {
        val time = measureTimeMillis {
            val one = doOne()
            val two = doTwo()
            println("The result is ${one + two}")
        }
        println("cpmpleted time is ${time}")
    }

    //并发
    @Test
    fun test_coroutine_measure2() = runBlocking {
        val time = measureTimeMillis {
//            val one = async { doOne() }
//            val two = async { doTwo() }
//            println("The result is ${one.await() + two.await()}")
            val one = launch { doOne() }
            val two = launch { doTwo() }
            //不加下面直接运行结束

//            one.join()
        }
        println("cpmpleted time is ${time}")
    }

    private suspend fun doOne(): Int {
        delay(1000)
        return 13
    }

    private suspend fun doTwo(): Int {
        delay(1000)
        return 14
    }

    /**
     *  协程的启动模式
     *  CoroutineStart.DEFAULT:协程创建后，立即开始调度，在调度前如果协程被取消，其将直接进入取消响应状态
     *  CoroutineStart.ATOMIC:协程创建后，立即开始调度，协程执行到第一个挂起点（例如delay()）之前不响应取消
     *  CoroutineStart.LAZY:只有协程被需要时，包括主动调用协程的start,join或者await等函数时才会开始调度，
     *                      如果调度前就被取消，那么该协程将直接进入异常状态。
     *  CoroutineStart.UNDISPATCHED:协程创建后立即在--当前函数调用栈中--执行，直到遇到第一个真正挂起的点。
     *                              理解为挂起点前在当前线程执行
     */
    @Test
    fun test_coroutine_start_mode() = runBlocking {
        val job = launch(start = CoroutineStart.DEFAULT) {
            println("job start")
            delay(10000)
            println("job finished")
        }
        delay(1000)
        job.cancel()
    }

    /**
     * 协程作用域
     * coroutineScope:一个协程失败了，所有其他兄弟协程也会被取消
     * supervisorScope:一个协程失败了，不会影响其他兄弟协程
     */
    @Test
    fun test_coroutine_scope() = runBlocking {
//        coroutineScope {
//            val job1 = launch {
//                println("job1 start")
//                delay(400)
//                println("job1 finished")
//            }
//            val job2 = launch {
//                println("job2 start")
//                delay(200)
//                println("job2 finished")
//                throw Exception()
//            }
//        }

        supervisorScope {
            val job1 = launch {
                println("job1 start")
                delay(400)
                println("job1 finished")
            }
            val job2 = launch {
                println("job2 start")
                delay(200)
                println("job2 finished")
                throw Exception()
            }
        }
    }

    /**
     * Job对象生命周期
     */


    /**
     * 协程作用域取消
     */
    @Test
    fun test_coroutine_scope_cancel() = runBlocking {
        val coroutineScope = CoroutineScope(Dispatchers.Default)
        val job1 = coroutineScope.launch {
            delay(1000)
            println("job1 finished")
        }
        val job2 = coroutineScope.launch {
            delay(1000)
            println("job2 finished")
        }
        delay(100)
//        coroutineScope.cancel()
        job1.cancel()   //被取消的协程不会影
        // 响其兄弟协程
//        delay(1000)
        delay(2000)

    }

    /**
     * 不能取消的任务
     * NonCancellable  CoroutineContext
     */
    @Test
    fun test_coroutine_no_cancel() = runBlocking {
        val job = launch {
            try {
                repeat(1000) {
                    println("job i'm sleeping")
                    delay(1000)
                }
            } finally {
                withContext(NonCancellable) {
                    println("job i'm finally")
                    delay(500)
                    println("non Cancellable")
                }
            }
        }
        delay(1300)
        println("main waiting")
        job.cancelAndJoin()
        println("main quit")
    }

    /**
     * 取消与异常
     */
}