package com.example.myandroid.util

import android.annotation.SuppressLint
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.ViewGroupUtils
import com.example.myandroid.util.delegate.MultiTouchDelegate


/**
 * @param interval 当小于等于0时 没有间隔
 * @param action 点击行为
 */
fun View.singleClickListener(
    interval: Long = ClickFilterUtils.interval, action: ((View) -> Unit)?,
) {
    if (action == null) {
        this.setOnClickListener(null)
    } else {
        this.setOnClickListener(
            DoubleFilterListener(
                interval = interval,
                isFilter = interval > 0,
                action = action
            )
        )
    }
}

/**
 * 全局防抖动点击
 * @param action 点击行为
 */
fun View.globalClickListener(action: ((View) -> Unit)?) {
    ClickFilterUtils.setGlobalFilterClickListener(this, action)
}

fun View.gone() {
    this.visibility = View.GONE
}

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

/**
 * 不需要
 * 设置View的显示状态
 */
fun setViewVisibility(vis: Int, vararg views: View?) {
    views.forEach {
        it?.visibility = vis
    }
}

/**
 * 增大View的点击事件范围
 * @param dx  宽   单位 px
 * @param dy  高   单位 px
 */
@SuppressLint("RestrictedApi")
fun View.expand(dx: Int, dy: Int) {
    // 获取当前控件的父控件,若父控件不是 ViewGroup, 则直接返回
    val parentView = parent as? ViewGroup ?: return
    // 若父控件未设置触摸代理，则构建 MultiTouchDelegate 并设置给它
    if (parentView.touchDelegate == null) parentView.touchDelegate =
        MultiTouchDelegate(delegateView = this)
    post {
        val rect = Rect()
        // 获取子控件在父控件中的区域
        ViewGroupUtils.getDescendantRect(parentView, this, rect)
        // 将响应区域扩大
        rect.inset(-dx, -dy)
        // 将子控件作为代理控件添加到 MultiTouchDelegate 中
        (parentView.touchDelegate as? MultiTouchDelegate)?.delegateViewMap?.put(this, rect)
    }
}