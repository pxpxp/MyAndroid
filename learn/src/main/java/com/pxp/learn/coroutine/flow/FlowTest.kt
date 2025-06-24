package com.pxp.learn.coroutine.flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Test

class FlowTest {

    val flow = flow<Int> {
        for (i in 1..3) {
            delay(1100)
            emit(i)
        }
    }

    @Test
    fun test_flow() = runBlocking {
        flow.collect {
            println(it)
        }
    }

    @Test
    fun test_flow_cold() = runBlocking {
        flow.collect {
            println(it)
        }
        println("第二次")
        flow.collect {
            println(it)
        }
    }

    @Test
    fun test_flow_builder() = runBlocking {
        //第一种，函数    flow<T>{}
        //第二种
        flowOf("one", "two", "three")
            .onEach {
                delay(1000)
            }
            .collect {
                println(it)
            }
        //第三种
        (1..5).asFlow()
            .collect {
                println(it)
            }
    }

    /**
     * flow会保存协程上下文，可以通过flowOn去修改
     * flowOn更改流发射的上下文
     */
    val flowContext = flow<Int> {
        println("Flow started ${Thread.currentThread().name}")
        for (i in 1..3) {
            delay(1100)
            emit(i)
        }
    }.flowOn(Dispatchers.IO)

    @Test
    fun test_flow_context() = runBlocking {
        flowContext
            .onEach {
                println("Flow onEach ${Thread.currentThread().name}")
                println(it)
            }
//            .flowOn(Dispatchers.IO)
//            .flowOn(Dispatchers.IO)
            .collect {
                println("Flow collect ${Thread.currentThread().name}")
                println(it)
            }
    }

    /**
     * launchIn 返回一个job
     * 指定一个协程收集流
     */
    @Test
    fun test_flow_launchIn() = runBlocking {
        val job = (1..3).asFlow()
//            .flowOn(Dispatchers.IO) //无效
            .onEach {
                println("on Each1 $it ${Thread.currentThread().name}")
            }
            .flowOn(Dispatchers.IO) //Each1,子线程；Each2主线程
            .onEach {
                println("on Each2 $it ${Thread.currentThread().name}")
            }
//            .flowOn(Dispatchers.IO)//Each1,子线程；Each2子线程
//            .launchIn(CoroutineScope(Dispatchers.Default))//Each1,子线程；Each2子线程
            .launchIn(this)
        job.join()
    }

    /**
     * flow cancel
     */
    @Test
    fun test_flow_cancel() = runBlocking {
        withTimeoutOrNull(2500) {
            val flow = flow<Int> {
                for (i in 1..3) {
                    delay(1000)
                    emit(i)
                    println("Emiting $i")
                }
            }
            flow.collect {


            }
        }
        println("done")
    }

    @Test
    fun test_flow_cancel2() = runBlocking {
        /*val flow = flow<Int> {
            for (i in 1..5) {
                emit(i)
                println("Emiting $i")
            }
        }
        flow.collect {
            println(it)
            if (it == 3) cancel()
        }*/

        (1..5).asFlow()
//            .cancellable()    //添加上才能取消
            .collect{
                println(it)
                if (it == 3) cancel()   //这里取消不成功
            }
    }

    

}