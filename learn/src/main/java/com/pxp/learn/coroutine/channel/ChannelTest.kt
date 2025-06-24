package com.example.myandroid.coroutine.channel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.select
import org.junit.Test


suspend fun CoroutineScope.getuserFromLocal(name: String) = async(Dispatchers.IO) {
    delay(1000)
    "from local $name"
}

suspend fun CoroutineScope.getuserFromNetwork(name: String) = async(Dispatchers.IO) {
    delay(2000)
    "from network $name"
}

class ChannelTest {

    @Test
    fun test_channel() = runBlocking {
        val channel = Channel<Int>()
        //生产者
        val producer = GlobalScope.launch {
            var i = 0
            while (true) {
                delay(1000)
                channel.send(++i)
                println("send $i")
            }
        }

        //消费者
        val consumer = GlobalScope.launch {
            while (true) {
                val element = channel.receive()
                println("receive $element")
            }
        }

        joinAll(producer, consumer)
    }

    //send 挂起
    @Test
    fun test_channel2() = runBlocking {
        //容量默认 capacity = 0
        val channel = Channel<Int>()
        //生产者
        val producer = GlobalScope.launch {
            var i = 0
            while (true) {
                delay(1000)
                channel.send(++i)
                println("send $i")
            }
        }

        //消费者
        val consumer = GlobalScope.launch {
            while (true) {
                delay(2000)
                val element = channel.receive()
                println("receive $element")
            }
        }

        joinAll(producer, consumer)
    }

    //迭代器
    @Test
    fun test_channel_iterate() = runBlocking {
        //容量默认 capacity = 0
        val channel = Channel<Int>(Channel.UNLIMITED)
        //生产者
        val producer = GlobalScope.launch {
            for (x in 1..5) {
                channel.send(x * x)
                println("sen ${x * x}")
            }
        }

        //消费者
        val consumer = GlobalScope.launch {
            /*val iterator = channel.iterator()
            while (iterator.hasNext()){
                val element = iterator.next()
                println("receive $element")
                delay(2000)
            }*/
            for (element in channel) {
                println("receive $element")
                delay(2000)
            }
        }

        joinAll(producer, consumer)
    }

    //produce 与 actor
    @Test
    fun test_channel_produce() = runBlocking {
        val receiveChannel = GlobalScope.produce {
            repeat(100) {
                delay(1000)
                send(it)
            }
        }

        val consumer = GlobalScope.launch {
            for (i in receiveChannel) {
                println("receive $i")
            }
        }

        consumer.join()
    }

    @Test
    fun test_channel_actor() = runBlocking {
        val sendChannel = GlobalScope.actor<Int> {
            while (true) {
                val element = receive()
                println("$element")
            }
        }

        val producer = GlobalScope.launch {
            for (i in 1..3) {
                sendChannel.send(i)
            }
        }

        producer.join()
    }

    //close
    @Test
    fun test_channel_close() = runBlocking {
        val channel = Channel<Int>(3)
        //生产者
        val producer = GlobalScope.launch {
            List(3) {
                channel.send(it)
                println("send $it")
            }
            channel.close()
            println("channel close | closedForSend ${channel.isClosedForSend} | isClosedForReceive ${channel.isClosedForReceive}")
        }

        //消费者
        val consumer = GlobalScope.launch {
            for (element in channel) {
                println("receive $element")
                delay(1000)
            }
            println("after consumer | closedForSend ${channel.isClosedForSend} | isClosedForReceive ${channel.isClosedForReceive}")
        }

        joinAll(producer, consumer)
    }

    /**
     * BroadcastChannel
     */
    @Test
    fun test_channel_broadcast() = runBlocking {
        val broadcastChannel = BroadcastChannel<Int>(3)

        val producer = GlobalScope.launch {
            List(3) {
                delay(100)
                broadcastChannel.send(it)
            }
            broadcastChannel.close()
        }

        List(3) {
            GlobalScope.launch {
                val receiveChannel = broadcastChannel.openSubscription()
                for (i in receiveChannel) {
                    println("$it received: $i")
                }
            }
        }.joinAll()
    }

    /**
     * 多路复用
     * select
     */

    @Test
    fun test_flow_select() = runBlocking {
        GlobalScope.launch {
            val localRequest = getuserFromLocal("xxx")
            val networkRequest = getuserFromNetwork("yyy")

            val select = select<String> {
                localRequest.onAwait {
                    it
                }
                networkRequest.onAwait {
                    it
                }
            }

            println("select result $select")
        }.join()
    }

    @Test
    fun test_flow_select2() = runBlocking {

        val channels = listOf(Channel<Int>(), Channel<Int>())
        GlobalScope.launch {
            delay(100)
            channels[0].send(200)
        }
        GlobalScope.launch {
            delay(50)
            channels[1].send(100)
        }

        val result = select<Int?> {
            channels.forEach {
                it.onReceive {
                    it
                }
            }
        }

        println("$result")
    }

    @Test
    fun test_flow_select3() = runBlocking {
        val job1 = GlobalScope.launch {
            delay(1000)
            println("job 1")
        }
        val job2 = GlobalScope.launch {
            delay(2000)
            println("job 2")
        }

        select {
            job1.onJoin {
                println("job 1 onJoin")
            }
            job2.onJoin {
                println("job 2 onJoin")
            }
        }
//        delay(3000)
    }


    @Test
    fun test_flow_select4() = runBlocking {
        val channels = listOf(Channel<Int>(), Channel<Int>())
        println(channels)

        launch {
            select {
                launch {
                    delay(10)
                    channels[1].onSend(200) {
                        //发送成功回调
                        println("send on $it")
                    }
                }
                launch {
                    delay(100)
                    channels[0].onSend(100) {
                        //发送成功回调
                        println("send on $it")
                    }
                }
            }
        }

        GlobalScope.launch {
            println(channels[0].receive())
        }
        GlobalScope.launch {
            println(channels[1].receive())
        }

        delay(3000)
    }


    /**
     * 不懂
     * 反射库导不进
     */
    @Test
    fun test_flow_select5() = runBlocking {
        //函数 -> 协程 -> Flow ->Flow合并
//        val name = "guest"
//        coroutineScope {
//            listOf(::getuserFromLocal, ::getuserFromNetwork)
//                .map {
//                    it.call(name)
//                }
//                .map {
//                    flow { emit(it.await()) }
//                }
//                .merge()
//                .collect{
//                    println(it)
//                }
//        }
    }
}