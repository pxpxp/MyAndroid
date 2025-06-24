package com.example.myandroid.util

import android.view.View

/**
 * @description  view 点击事件工具类
 */
object ClickFilterUtils {

    private var lastTime = 0L

    /**
     * 全局时间间隔
     */
    var interval = 800L

    /**
     * 设置单个防抖动点击
     */
    fun setSingleFilterClickListener(view: View?, listener: DoubleFilterListener?) {
        view ?: return
        view.setOnClickListener(listener)
    }

    /**
     * 设置全局防抖动点击
     */
    fun setGlobalFilterClickListener(view: View?, action: ((View) -> Unit)? = null) {
        view ?: return
        if (action == null) {
            view.setOnClickListener(null)
        } else {
            view.setOnClickListener {
                val currentTimeMillis = System.currentTimeMillis()
                if (currentTimeMillis > (lastTime + interval)) {
                    lastTime = currentTimeMillis
                    action.invoke(it)
                }
            }
        }
    }
}