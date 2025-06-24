package com.pxp.learn.coroutine

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

/**
 *  协程并发工具
 */
class CoroutineTest2 {

    @Test
    fun test_safe() = runBlocking {
        var count = 0
        List(1000) {
            GlobalScope.launch { count++ }
        }.joinAll()
        println(count)
    }

    @Test
    fun test_safe2() = runBlocking {
        val count = AtomicInteger(0)
        List(1000) {
            GlobalScope.launch { count.incrementAndGet() }
        }.joinAll()
        println(count)
    }

    @Test
    fun test_safe_concurrent_mutex() = runBlocking {
        var count = 0
        val mutex = Mutex()
        List(1000) {
            GlobalScope.launch {
                mutex.withLock {
                    count++
                }
            }
        }.joinAll()
        println(count)
    }

    @Test
    fun test_safe_concurrent_semaphore() = runBlocking {
        var count = 0
        val semaphore = Semaphore(1)
        List(1000) {
            GlobalScope.launch {
                semaphore.withPermit {
                    count++
                }
            }
        }.joinAll()
        println(count)
    }

    //外部控制并发
    @Test
    fun test_safe_concurrent_outer() = runBlocking {
        var count = 0
        val result = count + List(1000) {
            GlobalScope.async { 1 }
        }.map { it.await() }.sum()
        println(result)
    }

    //外部控制并发
    @Test
    fun test_mutex() = run {
        var count = 0
        val mutex = Mutex()
        runBlocking {
            repeat(1000) {
                launch(Dispatchers.IO) {
                    println("Running on ${Thread.currentThread().name}")
                    mutex.lock()
                    count++
                    mutex.unlock()
                }
            }
        }
        println(count)
    }

    //Actor 是一个并发同步模型，本质是基于 Channel 管道消息实现的。
    sealed class Msg {
        object AddMsg : Msg()
        class ResultMsg(val result: CompletableDeferred<Int>) : Msg()
    }
    @Test
    fun test_actor() = runBlocking {
        suspend fun addActor() = actor<Msg> {
            var count = 0;
            for (msg in channel) {
                when (msg) {
                    is Msg.AddMsg -> count++
                    is Msg.ResultMsg -> msg.result.complete(count)
                }
            }
        }

        val actor = addActor()
        val jobs = mutableListOf<Job>()

        repeat(10) {
            val job = launch(Dispatchers.Default) {
                repeat(1000) {
                    actor.send(Msg.AddMsg)
                }
            }
            jobs.add(job)
        }

        jobs.joinAll()

        val deferred = CompletableDeferred<Int>()
        actor.send(Msg.ResultMsg(deferred))

        val result = deferred.await()
        actor.close()

        println("i: $result")
    }
}