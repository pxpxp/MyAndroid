package com.pxp.learn.coroutine.flow

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/**
 * @author pxp
 * @description 使用flow来实现eventbus
 * https://juejin.cn/post/7054462654902960135/
 *
 * 订阅数据：
 * val liveData = FlowEventBus.getFlow<String>("action.onMessage").asLiveData()
 * fun subscribeEvent() {
 *     lifecycleScope.launch {
 *         FlowEventBus.subscribe<String>(lifecycle, "action.onMessage") {
 *             Log.d(TAG, "testShardFlow: getMsg $it")
 *         }
 *     }
 *     liveData.observe(this) {
 *         mDataBinding.tvEventBus.text = it
 *     }
 * }
 *
 * 发送数据：
 * fun testSharedFlow() {
 *     viewModelScope.launch {
 *         FlowEventBus.post("action.onMessage", "send msg")
 *     }
 * }
 *
 */
object FlowEventBus {
    private val bus: HashMap<String, MutableSharedFlow<out Any>> = hashMapOf()

    private fun <T : Any> with(key: String): MutableSharedFlow<T> {
        if (!bus.containsKey(key)) {
            val flow = MutableSharedFlow<T>()
            bus[key] = flow
        }
        return bus[key] as MutableSharedFlow<T>
    }

    /**
     * 对外只暴露SharedFlow
     * @param action String
     * @return SharedFlow<T>
     */
    fun <T> getFlow(action: String): SharedFlow<T> {
        return with(action)
    }


    /**
     * 挂起函数
     * @param action String
     * @param data T
     */
    suspend fun <T : Any> post(action: String, data: T) {
        with<T>(action).emit(data)
    }

    /**
     * 详见tryEmit和emit的区别
     * @param action String
     * @param data T
     * @return Boolean
     */
    fun <T : Any> tryPost(action: String, data: T): Boolean {
        return with<T>(action).tryEmit(data)
    }

    /**
     * sharedFlow会长久持有，所以要加声明周期限定，不然会出现内存溢出
     * @param lifecycle Lifecycle
     * @param action String
     * @param block Function1<T, Unit>
     */
    suspend fun <T : Any> subscribe(lifecycle: Lifecycle, action: String, block: (T) -> Unit) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
            with<T>(action).collect {
                block(it)
            }
        }
    }

    /**
     * 注意，使用这个方法需要将协程在合适的时候取消，否则会导致内存溢出
     * @param action String
     * @param block Function1<T, Unit>
     */
    suspend fun <T : Any> subscribe(action: String, block: (T) -> Unit) {
        with<T>(action).collect {
            block(it)
        }
    }
}
