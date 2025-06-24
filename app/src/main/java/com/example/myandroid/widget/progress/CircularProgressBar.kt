package com.example.myandroid.widget.progress

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import com.example.myandroid.R
import org.jetbrains.annotations.Nullable

/**
 * @description 圆环进度条
 * https://llw-study.blog.csdn.net/article/details/130078072
 */
class CircularProgressBar : View {

    /**
     * 半径
     */
    private var mRadius = 0

    /**
     * 进度条宽度
     */
    private var mStrokeWidth = 0f

    /**
     * 进度条背景颜色
     */
    private var mProgressbarBgColor = 0

    /**
     * 进度条进度颜色
     */
    private var mProgressColor = 0

    /**
     * 开始角度
     */
    private val mStartAngle = 270f

    /**
     * 当前角度
     */
    private var mCurrentAngle = 0f

    /**
     * 结束角度
     */
    private val mEndAngle = 360f

    /**
     * 最大进度
     */
    private var mMaxProgress = 0f

    /**
     * 当前进度
     */
    private var mCurrentProgress = 0f

    /**
     * 文字
     */
    private var mText: String? = null

    /**
     * 文字颜色
     */
    private var mTextColor = 0

    /**
     * 文字大小
     */
    private var mTextSize = 0f

    /**
     * 动画的执行时长
     */
    private val mDuration: Long = 1000

    /**
     * 是否执行动画
     */
    private var isAnimation = false

    constructor(context: Context) : this(context, null)

    constructor(context: Context, @Nullable attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(
        context: Context,
        @Nullable attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr, defStyleAttr) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.CircularProgressBar)
        mRadius = array.getDimensionPixelSize(R.styleable.CircularProgressBar_radius, 80)
        mStrokeWidth =
            array.getDimensionPixelSize(R.styleable.CircularProgressBar_strokeWidth, 8).toFloat()
        mProgressbarBgColor = array.getColor(
            R.styleable.CircularProgressBar_progressbarBackgroundColor,
            ContextCompat.getColor(context, R.color.teal_700)
        )
        mProgressColor = array.getColor(
            R.styleable.CircularProgressBar_progressbarColor,
            ContextCompat.getColor(context, R.color.teal_200)
        )
        mMaxProgress = array.getInt(R.styleable.CircularProgressBar_maxProgress, 100).toFloat()
        mCurrentProgress = array.getInt(R.styleable.CircularProgressBar_progress, 0).toFloat()
        val text = array.getString(R.styleable.CircularProgressBar_text)
        mText = text ?: ""
        mTextColor = array.getColor(
            R.styleable.CircularProgressBar_textColor,
            ContextCompat.getColor(context, R.color.black)
        )
        mTextSize = array.getDimensionPixelSize(
            R.styleable.CircularProgressBar_textSize, TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_SP, 14f, resources.displayMetrics).toInt()
        ).toFloat()
        array.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        var width = 0
        when (MeasureSpec.getMode(widthMeasureSpec)) {
            MeasureSpec.UNSPECIFIED, MeasureSpec.AT_MOST -> width = mRadius * 2
            MeasureSpec.EXACTLY -> width = MeasureSpec.getSize(widthMeasureSpec)
        }
        //Set the measured width and height
        setMeasuredDimension(width, width)
    }

    override fun onDraw(canvas: Canvas) {
        val centerX = width / 2
        val rectF = RectF()
        rectF.left = mStrokeWidth.toFloat()
        rectF.top = mStrokeWidth.toFloat()
        rectF.right = (centerX * 2 - mStrokeWidth).toFloat()
        rectF.bottom = (centerX * 2 - mStrokeWidth).toFloat()

        //绘制进度条背景
        drawProgressbarBg(canvas, rectF)
        //绘制进度
        drawProgress(canvas, rectF)
        //绘制中心文本
        drawCenterText(canvas, centerX)
    }

    /**
     * 绘制进度条背景
     */
    private fun drawProgressbarBg(canvas: Canvas, rectF: RectF) {
        val mPaint = Paint()
        //画笔的填充样式，Paint.Style.STROKE 描边
        mPaint.setStyle(Paint.Style.STROKE)
        //圆弧的宽度
        mPaint.setStrokeWidth(mStrokeWidth)
        //抗锯齿
        mPaint.setAntiAlias(true)
        //画笔的颜色
        mPaint.setColor(mProgressbarBgColor)
        //画笔的样式 Paint.Cap.Round 圆形
        mPaint.setStrokeCap(Paint.Cap.ROUND)
        //开始画圆弧
        canvas.drawArc(rectF, mStartAngle, mEndAngle, false, mPaint)
    }

    /**
     * 绘制进度
     */
    private fun drawProgress(canvas: Canvas, rectF: RectF) {
        val paint = Paint()
        paint.setStyle(Paint.Style.STROKE)
        paint.setStrokeWidth(mStrokeWidth)
        paint.setColor(mProgressColor)
        paint.setAntiAlias(true)
        //是否圆角
//        paint.setStrokeCap(Paint.Cap.ROUND)
        if (!isAnimation) {
            mCurrentAngle = 360 * (mCurrentProgress / mMaxProgress)
        }
        canvas.drawArc(rectF, mStartAngle, mCurrentAngle, false, paint)
    }

    /**
     * 绘制中心文字
     */
    private fun drawCenterText(canvas: Canvas, centerX: Int) {
        val paint = Paint()
        paint.setAntiAlias(true)
        paint.setColor(mTextColor)
        paint.setTextAlign(Paint.Align.CENTER)
        paint.setTextSize(mTextSize)
        val textBounds = Rect()
        paint.getTextBounds(mText, 0, mText!!.length, textBounds)
        canvas.drawText(mText!!, centerX.toFloat(), textBounds.height() / 2f + height / 2, paint)
    }

    /**
     * 设置当前进度
     */
    fun setProgress(progress: Float) {
        var progress = progress
        require(progress >= 0) { "Progress value can not be less than 0" }
        if (progress > mMaxProgress) {
            progress = mMaxProgress
        }
        mCurrentProgress = progress
        mCurrentAngle = 360 * (mCurrentProgress / mMaxProgress)
        setAnimator(0f, mCurrentAngle)
    }

    /**
     * 设置文本
     */
    fun setText(text: String) {
        mText = text
    }

    /**
     * 设置文本的颜色
     */
    fun setTextColor(color: Int) {
        require(color > 0) { "Color value can not be less than 0" }
        mTextColor = color
    }

    /**
     * 设置文本的大小
     */
    fun setTextSize(textSize: Float) {
        require(textSize > 0) { "textSize can not be less than 0" }
        mTextSize = textSize
    }

    /**
     * 设置动画
     *
     * @param start  开始位置
     * @param target 结束位置
     */
    private fun setAnimator(start: Float, target: Float) {
        isAnimation = true
        val animator = ValueAnimator.ofFloat(start, target)
        animator.duration = mDuration
        animator.setTarget(mCurrentAngle)
        //动画更新监听
        animator.addUpdateListener { valueAnimator: ValueAnimator ->
            mCurrentAngle = valueAnimator.animatedValue as Float
            invalidate()
        }
        animator.start()
    }
}