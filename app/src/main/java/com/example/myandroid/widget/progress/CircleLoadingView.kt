package com.example.myandroid.widget.progress

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class CircleLoadingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0
) : View(
    context,
    attrs,
    defStyleAttr
) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var sweepDegree = 0f

    init {
        paint.strokeWidth = 4f
    }

    fun setProgress(progress: Float) {
        sweepDegree = 360f * progress
        if (sweepDegree > 360f) sweepDegree = 360f
        if (sweepDegree < 0f) sweepDegree = 0f
        postInvalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (sweepDegree > 360f) sweepDegree = 0f

        paint.style = Paint.Style.STROKE
        paint.color = Color.parseColor("#8A8A8E")
        canvas.drawCircle(width / 2f, width / 2f, width / 2f - (paint.strokeWidth / 2), paint)

        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE
        canvas.rotate(270 - sweepDegree, width / 2f, width / 2f)
        canvas.drawArc(
            width / 2f - width * 13 / 36f + (paint.strokeWidth / 2),
            width / 2f - width * 13 / 36f + (paint.strokeWidth / 2),
            width / 2f + width * 13 / 36f - (paint.strokeWidth / 2),
            width / 2f + width * 13 / 36f - (paint.strokeWidth / 2),
            0f, sweepDegree, true, paint
        )
        canvas.rotate(-(270 - sweepDegree), width / 2f, width / 2f)
    }
}