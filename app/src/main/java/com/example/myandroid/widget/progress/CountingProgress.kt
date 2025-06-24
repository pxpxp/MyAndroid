package com.example.myandroid.widget.progress

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View

/**
 * @author pxp
 * @description
 */
class CountingProgress : View {

    // 当前进度
    private var currentProgress = 0

    // 边距
    private val padding = dp2px(1.5f).toFloat()

    // 渐变开始颜色
    private val startColor = Color.parseColor("#FFC305")

    // 渐变结束颜色
    private val endColor = Color.parseColor("#80EE0B")

    // 当前矩形原角度
    private var cornerRadius = dp2px(10f).toFloat()

    // 当前进度条画笔的宽度
    private val curProgressWidth: Float by lazy {
        dp2px(40f).toFloat()
    }

    // 是否居中
    private var isCentered = false

    // 当前进度条画笔
    private val pathPaint: Paint by lazy {
        Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = curProgressWidth
            shader = LinearGradient(
                0f, 0f, width.toFloat(), 0f, startColor, endColor, Shader.TileMode.CLAMP
            )
        }
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    )

    // 设置进度
    fun setProgress(progress: Int) {
        currentProgress = progress
        invalidate()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // 测量路径
        val measure = PathMeasure(getMeasurePath(), true)
        // 创建目标路径
        val dst = Path()
        // 计算进度段
        val progressLength = measure.length * currentProgress / 100f
        // 获取截取路径
        if (progressLength > 0) {
            measure.getSegment(0f, progressLength, dst, true)
        }
        // 绘制路径
        canvas.drawPath(dst, pathPaint)
    }

    /**
     * 获取测量路径
     * @return Path
     */
    private fun getMeasurePath(): Path {
        // 矩形大小
        val maxRect = RectF(padding, padding, width - padding, height - padding)
        // 创建路径和测量工具
        val path = Path()
        // 是否从顶部居中开始
        if (isCentered) {
            // 从顶部中间开始绘制路径
            val topMiddleX = maxRect.left + maxRect.width() / 2
            path.moveTo(topMiddleX, maxRect.top)

            // 绘制到右上角并圆角化
            path.arcTo(RectF(maxRect.right - cornerRadius * 2, maxRect.top, maxRect.right, maxRect.top + cornerRadius * 2), -90f, 90f)
            // 绘制右边
            path.lineTo(maxRect.right, maxRect.bottom - cornerRadius)
            // 绘制右下角圆角
            path.arcTo(RectF(maxRect.right - cornerRadius * 2, maxRect.bottom - cornerRadius * 2, maxRect.right, maxRect.bottom), 0f, 90f)
            // 绘制底部
            path.lineTo(maxRect.left + cornerRadius, maxRect.bottom)
            // 绘制左下角圆角
            path.arcTo(RectF(maxRect.left, maxRect.bottom - cornerRadius * 2, maxRect.left + cornerRadius * 2, maxRect.bottom), 90f, 90f)
            // 绘制左边
            path.lineTo(maxRect.left, maxRect.top + cornerRadius)
            // 绘制左上角圆角
            path.arcTo(RectF(maxRect.left, maxRect.top, maxRect.left + cornerRadius * 2, maxRect.top + cornerRadius * 2), 180f, 90f)

            // 关闭路径形成闭环
            path.close()
        } else {
            path.addRoundRect(maxRect, cornerRadius, cornerRadius, Path.Direction.CW)
        }
        return path
    }

    fun dp2px(dipValue: Float): Int {
        val scale = context.applicationContext.resources.displayMetrics.density
        return (dipValue * scale + 0.5f).toInt()
    }
}
