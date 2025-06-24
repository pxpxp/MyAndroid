package com.example.myandroid.widget.behavior

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetBehavior

/**
 * @author pxp
 * @description 根据BottomSheetBehavior控制View的显示隐藏以避让BottomSheet
 * https://juejin.cn/post/7360497239871651880
 * https://mp.weixin.qq.com/s/04OFp3W6Ko2klg_3fF1q1g
 */
class VisibilityByBottomSheetBehavior(
    context: Context,
    attributeSet: AttributeSet,
) : CoordinatorLayout.Behavior<View>(context, attributeSet) {

    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: View,
        dependency: View
    ): Boolean {
        // 如果依赖的View是BottomSheet，则建立依赖关系
        return isBottomSheet(dependency)
    }

    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: View,
        dependency: View
    ): Boolean {
        if (isBottomSheet(dependency)) {
            // 如果依赖的View的顶部位置大于等于child的底部位置，则隐藏child
            child.isVisible = dependency.top >= child.bottom
        }
        // 返回false，布局的不会再次计算和重绘
        return false
    }

    private fun isBottomSheet(view: View): Boolean {
        val lp = view.layoutParams
        return if (lp is CoordinatorLayout.LayoutParams) {
            lp.behavior is BottomSheetBehavior<*>
        } else false
    }
}