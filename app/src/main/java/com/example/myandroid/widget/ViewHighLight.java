package com.example.myandroid.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.example.myandroid.R;

/**
 * 图片描边效果
 * https://juejin.cn/post/7310786575213920306
 *
 * @author pxp
 * @description
 */
public class ViewHighLight extends View {
    final Bitmap bms; //source 原图
    final Bitmap bmm; //mask 蒙版
    final Paint paint;
    final int width = 4;
    final int step = 15; // 1...45
    int index = -1;
    int max = 15;
    int[] colors = new int[max];
    final int[] highlightColors = {0xfff00000, 0, 0xffff9922, 0, 0xff00ff00, 0};

    public ViewHighLight(Context context) {
        this(context, null);
    }

    public ViewHighLight(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ViewHighLight(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ViewHighLight(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        bms = decodeBitmap(R.drawable.mm02);
        bmm = Bitmap.createBitmap(bms.getWidth(), bms.getHeight(), Bitmap.Config.ALPHA_8);
        Canvas canvas = new Canvas(bmm);
        canvas.drawBitmap(bms, 0, 0, null);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    private Bitmap decodeBitmap(int resId) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        return BitmapFactory.decodeResource(getResources(), resId, options);
    }

//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        // 父容器传过来的宽度方向上的模式
//        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
//        // 父容器传过来的高度方向上的模式
//        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
//
//        // 父容器传过来的宽度的值
//        int width = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft()
//                - getPaddingRight();
//        // 父容器传过来的高度的值
//        int height = MeasureSpec.getSize(heightMeasureSpec) - getPaddingLeft()
//                - getPaddingRight();
//
//        if (widthMode == MeasureSpec.EXACTLY
//                && heightMode != MeasureSpec.EXACTLY && ratio != 0.0f) {
//            // 判断条件为，宽度模式为Exactly，也就是填充父窗体或者是指定宽度；
//            // 且高度模式不是Exaclty，代表设置的既不是fill_parent也不是具体的值，于是需要具体测量
//            // 且图片的宽高比已经赋值完毕，不再是0.0f
//            // 表示宽度确定，要测量高度
//            height = (int) (width / ratio + 0.5f);
//            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height,
//                    MeasureSpec.EXACTLY);
//        } else if (widthMode != MeasureSpec.EXACTLY
//                && heightMode == MeasureSpec.EXACTLY && ratio != 0.0f) {
//            // 判断条件跟上面的相反，宽度方向和高度方向的条件互换
//            // 表示高度确定，要测量宽度
//            width = (int) (height * ratio + 0.5f);
//
//            widthMeasureSpec = MeasureSpec.makeMeasureSpec(width,
//                    MeasureSpec.EXACTLY);
//        }
//
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // draw blur shadow

        for (int i = 0; i < 360; i += step) {
            float x = width * (float) Math.cos(Math.toRadians(i));
            float y = width * (float) Math.sin(Math.toRadians(i));
            canvas.drawBitmap(bmm, x, y, paint);
        }
        canvas.drawBitmap(bms, 0, 0, null);

        if (index == -1) {
            return;
        }
        index++;
        if (index > max + 1) {
            return;
        }
        if (index >= max) {
            paint.setColor(Color.TRANSPARENT);
        } else {
            paint.setColor(colors[index]);
        }
        postInvalidateDelayed(200);
    }

    /** 图片宽和高的比例 */
//    private float ratio = 0.5f;
//
//    public void setRatio(float ratio) {
//        this.ratio = ratio;
//    }

    public void shake() {
        index = 0;
        for (int i = 0; i < max; i += 2) {
            colors[i] = highlightColors[i % highlightColors.length];
        }
        postInvalidate();
    }
}

