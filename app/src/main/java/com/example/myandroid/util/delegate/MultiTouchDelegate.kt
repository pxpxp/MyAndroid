package com.example.myandroid.util.delegate

import android.graphics.Rect
import android.view.MotionEvent
import android.view.TouchDelegate
import android.view.View


/**
 * @description 自定义点击触摸事件代理
 */
class MultiTouchDelegate(bound: Rect? = null, delegateView: View) : TouchDelegate(bound, delegateView) {
    val delegateViewMap = mutableMapOf<View, Rect>()
    private var delegateView: View? = null

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x.toInt()
        val y = event.y.toInt()
        var handled = false
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                delegateView = findDelegateViewUnder(x, y)
            }
            MotionEvent.ACTION_CANCEL -> {
                delegateView = null
            }
        }
        delegateView?.let {
            event.setLocation(it.width / 2f, it.height / 2f)
            handled = it.dispatchTouchEvent(event)
        }
        return handled
    }

    private fun findDelegateViewUnder(x: Int, y: Int): View? {
        delegateViewMap.forEach { entry -> if (entry.value.contains(x, y)) return entry.key }
        return null
    }
}