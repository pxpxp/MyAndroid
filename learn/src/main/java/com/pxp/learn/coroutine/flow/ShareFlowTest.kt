package com.pxp.learn.coroutine.flow

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * @author pxp
 * @description Flow,SharedFlow,StateFlow的使用及原理
 * https://juejin.cn/post/7275975684148723746
 */
class ShareFlowTest {
    @Test
    fun sharedFlow(){
        runBlocking {
            val _sharedFlow = MutableSharedFlow<Int>(replay = 3)
            val sharedFlow = _sharedFlow.asSharedFlow()
            launch(Dispatchers.IO) {
                for(i in 0..50){
                    Log.d("123456", "emit：$i")
                    _sharedFlow.emit(i)
                    delay(50)
                }
            }
            delay(5000)
            //延迟5000毫秒去订阅,其实这时候流已经发送完了.相当于新的订阅者
            sharedFlow.onEach {
                Log.d("123456", "onEach：$it")
            }.launchIn(this)
            //输出结果:
            //emit:0
            //emit:1
            // ...
            //emit:50
            //onEach:48
            //onEach:49
            //onEach:50
        }
    }

    @Test
    fun shareIn(){
        runBlocking {
            flowOf(0,1,2,3,4)
                .shareIn(this, SharingStarted.WhileSubscribed())    //SharedFlow
//                .collect()
        }
    }
}