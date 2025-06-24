package com.example.myandroid.util

import android.view.View


/**
 * @description 防抖动监听
 */
open class DoubleFilterListener(
    private val interval: Long = ClickFilterUtils.interval,
    private val isFilter: Boolean = true,
    private val action: (View) -> Unit
) : View.OnClickListener {

    private var lastTime = 0L

    override fun onClick(v: View) {
        if (isFilter) {
            val currentTimeMillis = System.currentTimeMillis()
            if (interval < (currentTimeMillis - lastTime)) {
                action.invoke(v)
                lastTime = currentTimeMillis
            }
        } else {
            action.invoke(v)
        }
    }
}