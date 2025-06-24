package com.example.myandroid.widget.timekids;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Rect;
import android.graphics.Region;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

/**
 * @author 时光少年
 * @description Region碰撞检测问题优化
 * https://juejin.cn/post/7310412252552085513
 */
public class RegionView extends View {

    private static String TAG = "RegionView";

    private final DisplayMetrics mDM;
    private TextPaint mCommonPaint;

    public RegionView(Context context) {
        this(context, null);
    }

    public RegionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDM = getResources().getDisplayMetrics();
        initPaint();
        setClickable(true); //触发hotspot
    }

    private void initPaint() {
        //否则提供给外部纹理绘制
        mCommonPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        mCommonPaint.setAntiAlias(true);
        mCommonPaint.setStyle(Paint.Style.FILL);
        mCommonPaint.setStrokeCap(Paint.Cap.ROUND);
        mCommonPaint.setFilterBitmap(true);
        mCommonPaint.setDither(true);
        mCommonPaint.setStrokeWidth(dp2px(20));

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        if (widthMode != MeasureSpec.EXACTLY) {
            widthSize = mDM.widthPixels / 2;
        }
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (heightMode != MeasureSpec.EXACTLY) {
            heightSize = widthSize / 2;
        }
        setMeasuredDimension(widthSize, heightSize);

    }

    private float x;
    private float y;

    //所以形状
    Path[] objectPaths = new Path[7];
    //形状区域检测
    Region objectRegion = new Region();

    //小圆球区域
    Region circleRegion = new Region();
    //小圆
    Path circlePath = new Path();
    //绘制区域
    Region mainRegion = new Region();

    Rect circleRect = new Rect();
    Rect objectRect = new Rect();

    float[] pos = new float[2];
    float[] tan = new float[2];

    PathMeasure pathMeasure = new PathMeasure();

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        if (width < 1 || height < 1) {
            return;
        }

        int save = canvas.save();
        canvas.translate(width / 2f, height / 2f);
        float radius = Math.min(width / 2f, height / 2f);

        Log.e(TAG, "radius:" + radius);

        mainRegion.set((int) -radius, (int) -radius, (int) radius, (int) radius);

        for (int i = 0; i < objectPaths.length; i++) {
            Path path = objectPaths[i];
            if (path == null) {
                path = new Path();
                objectPaths[i] = path;
            } else {
                path.reset();
            }
        }

        Path path = objectPaths[0];
        path.moveTo(radius / 2, -radius / 2);
        path.lineTo(0, -radius);
        path.lineTo(radius / 2, -radius);
        path.close();

        path = objectPaths[1];
        path.moveTo(-radius / 2, radius / 2);
        path.lineTo(-radius / 2 - 100, radius / 2);
        path.arcTo(-radius / 2 - 100, radius / 2, -radius / 2, radius / 2 + 100, 0, 180, false);
        path.lineTo(-radius / 2, radius / 2);
        path.close();

        path = objectPaths[2];
        path.addCircle(-radius + 200f, -radius + 200f, 50f, Path.Direction.CCW);

        path = objectPaths[3];
        path.addRoundRect(-radius + 50, -radius / 2, -radius  + 90, 0, 10, 10, Path.Direction.CCW);

        path = objectPaths[4];
        path.addRect(120, 120, 200, 200, Path.Direction.CCW);

        path = objectPaths[5];
        path.addCircle(250, 0, 100, Path.Direction.CCW);

        Path tmp = new Path();
        tmp.addCircle(250,-80,80,Path.Direction.CCW);
        path.op(tmp, Path.Op.DIFFERENCE);

        tmp.reset();
        path = objectPaths[6];
        path.addCircle(0, 0, 100, Path.Direction.CCW);
        tmp.addCircle(0, 0, 80, Path.Direction.CCW);
        path.op(tmp, Path.Op.DIFFERENCE);


        circlePath.reset();
        circlePath.addCircle(x- width/2f,y - height/2f,20, Path.Direction.CCW);
        circleRegion.setPath(circlePath,mainRegion);


        mCommonPaint.setColor(Color.CYAN);
        for (int i = 0; i < objectPaths.length; i++) {
            objectRegion.setPath(objectPaths[i],mainRegion);
            if(!objectRegion.quickReject(circleRegion)){
                if (circleRegion.getBounds(circleRect)
                        && objectRegion.getBounds(objectRect)) {

                    Region regionChecker = null;
                    if (circleRect.width() * circleRect.height() > objectRect.width() * objectRect.height()) {
                        pathMeasure.setPath(objectPaths[i], false);
                        regionChecker = circleRegion;
                    } else {
                        pathMeasure.setPath(circlePath, false);
                        regionChecker = objectRegion;
                    }

                    for (int len = 0; len < pathMeasure.getLength(); len++) {
                        pathMeasure.getPosTan(len, pos, tan);
                        if(regionChecker.contains((int) pos[0], (int) pos[1])){
                            Log.d("RegionView"," 可能发生了碰撞");
                            mCommonPaint.setColor(Color.YELLOW);
                        }
                    }

                }

            }else{
                mCommonPaint.setColor(Color.CYAN);
            }
            canvas.drawPath(objectPaths[i], mCommonPaint);
        }

        mCommonPaint.setColor(Color.WHITE);
        canvas.drawPath(circlePath,mCommonPaint);
        canvas.restoreToCount(save);
    }

    @Override
    public void dispatchDrawableHotspotChanged(float x, float y) {
        super.dispatchDrawableHotspotChanged(x, y);
        this.x = x;
        this.y = y;
        postInvalidate();
    }

    @Override
    protected void dispatchSetPressed(boolean pressed) {
        super.dispatchSetPressed(pressed);
        postInvalidate();
    }

    public float dp2px(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, mDM);
    }

    public static int argb(float red, float green, float blue) {
        return ((int) (1 * 255.0f + 0.5f) << 24) |
                ((int) (red * 255.0f + 0.5f) << 16) |
                ((int) (green * 255.0f + 0.5f) << 8) |
                (int) (blue * 255.0f + 0.5f);
    }
}

