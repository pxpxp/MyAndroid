package com.example.myandroid.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.example.myandroid.R;

/**
 * 剪切图片
 */
public class ExpandImageView extends FrameLayout {
    private static int STATUS_IDLE = 1;// 空闲状态
    private static int STATUS_MOVE = 2;// 移动状态
    private static int STATUS_SCALE = 3;// 缩放状态

    private int MAGIN_WIDTH = 46;
    private Context mContext;
    private float oldx, oldy;
    private int status = STATUS_IDLE;
    private int selectedControllerCicle;
    private RectF backUpRect = new RectF();// 上
    private RectF backLeftRect = new RectF();// 左
    private RectF backRightRect = new RectF();// 右
    private RectF backDownRect = new RectF();// 下

    private Paint mBackgroundPaint;// 背景Paint
    private Paint mLinePaint;// 线条Paint

    //控制点Bitmap
    private Bitmap leftTopBit;
    private Bitmap rightTopBit;
    private Bitmap leftBottomBit;
    private Bitmap rightBottomBit;
    private Bitmap horizontalBit;
    private Bitmap verticalBit;
    private Rect bitRect = new Rect();

    //选中八个区域可滑动
    private RectF leftTopCircleRect;
    private RectF rightTopCircleRect;
    private RectF leftBottomRect;
    private RectF rightBottomRect;
    private RectF topCenterRect;
    private RectF leftCenterRect;
    private RectF rightCenterRect;
    private RectF bottomCenterRect;

    private ImageView imageView;// 原始图片控件
    private ImageView expandImageView;// 原始图片控件

    private Bitmap bitmap;// 原始bitmap

    private RectF imageRect = new RectF();// 存贮图片位置信息
    private RectF cropRect = new RectF();// 剪切矩形
    private RectF tempRect = new RectF();// 临时存贮矩形数据

    private float ratio = -1;// 剪裁缩放比率

    public ExpandImageView(Context context) {
        super(context);
        init(context);
    }

    public ExpandImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ExpandImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;

        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(Color.WHITE);

        mLinePaint = new Paint();
        mLinePaint.setColor(Color.parseColor("#FF0065F2"));
        mLinePaint.setStrokeWidth(dp2px(1.5f));

        leftTopBit = BitmapFactory.decodeResource(context.getResources(),
                R.mipmap.ic_left_top_angle);
        rightTopBit = BitmapFactory.decodeResource(context.getResources(),
                R.mipmap.ic_right_top_angle);
        leftBottomBit = BitmapFactory.decodeResource(context.getResources(),
                R.mipmap.ic_left_bottom_angle);
        rightBottomBit = BitmapFactory.decodeResource(context.getResources(),
                R.mipmap.ic_right_bottom_angle);
        horizontalBit = BitmapFactory.decodeResource(context.getResources(),
                R.mipmap.ic_heng);
        verticalBit = BitmapFactory.decodeResource(context.getResources(),
                R.mipmap.ic_shu);
        bitRect.set(0, 0, leftTopBit.getWidth(), leftTopBit.getHeight());

        MAGIN_WIDTH = dp2px(10f);
        leftTopCircleRect = new RectF(0, 0, 0, 0);
        rightTopCircleRect = new RectF(leftTopCircleRect);
        leftBottomRect = new RectF(leftTopCircleRect);
        rightBottomRect = new RectF(leftTopCircleRect);
        topCenterRect = new RectF(leftTopCircleRect);
        leftCenterRect = new RectF(leftTopCircleRect);
        rightCenterRect = new RectF(leftTopCircleRect);
        bottomCenterRect = new RectF(leftTopCircleRect);

        //原始图片
        imageView = new ImageView(getContext());
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        addView(imageView);
        //扩图图片
        expandImageView = new ImageView(getContext());
        expandImageView.setScaleType(ImageView.ScaleType.FIT_XY);
        expandImageView.setVisibility(INVISIBLE);
        addView(expandImageView);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width;
        int height;
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        width = widthSpecSize;
        height = (int) (width / 360f * 420);
        setMeasuredDimension(width, height);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0)
            return;

        //绘制上下左右背景
        backUpRect.set(0, 0, w, cropRect.top);
        backLeftRect.set(0, cropRect.top, cropRect.left, cropRect.bottom);
        backRightRect.set(cropRect.right, cropRect.top, w, cropRect.bottom);
        backDownRect.set(0, cropRect.bottom, w, h);

        canvas.drawRect(backUpRect, mBackgroundPaint);
        canvas.drawRect(backLeftRect, mBackgroundPaint);
        canvas.drawRect(backRightRect, mBackgroundPaint);
        canvas.drawRect(backDownRect, mBackgroundPaint);

        //绘制线
        canvas.drawLine(cropRect.left, cropRect.top, cropRect.right, cropRect.top, mLinePaint);
        canvas.drawLine(cropRect.left, cropRect.top, cropRect.left, cropRect.bottom, mLinePaint);
        canvas.drawLine(cropRect.right, cropRect.top, cropRect.right, cropRect.bottom, mLinePaint);
        canvas.drawLine(cropRect.left, cropRect.bottom, cropRect.right, cropRect.bottom, mLinePaint);

        // 绘制八个控制点
        int radius = MAGIN_WIDTH;
        leftTopCircleRect.set(cropRect.left - radius, cropRect.top - radius,
                cropRect.left + radius, cropRect.top + radius);
        rightTopCircleRect.set(cropRect.right - radius, cropRect.top - radius,
                cropRect.right + radius, cropRect.top + radius);
        leftBottomRect.set(cropRect.left - radius, cropRect.bottom - radius,
                cropRect.left + radius, cropRect.bottom + radius);
        rightBottomRect.set(cropRect.right - radius, cropRect.bottom - radius,
                cropRect.right + radius, cropRect.bottom + radius);

        topCenterRect.set(cropRect.left + (cropRect.right - cropRect.left) / 2 - radius, cropRect.top - radius,
                cropRect.left + (cropRect.right - cropRect.left) / 2 + radius, cropRect.top + radius);
        leftCenterRect.set(cropRect.left - radius, cropRect.top + (cropRect.bottom - cropRect.top) / 2 - radius,
                cropRect.left + radius, cropRect.top + (cropRect.bottom - cropRect.top) / 2 + radius);
        rightCenterRect.set(cropRect.right - radius, cropRect.top + (cropRect.bottom - cropRect.top) / 2 - radius,
                cropRect.right + radius, cropRect.top + (cropRect.bottom - cropRect.top) / 2 + radius);
        bottomCenterRect.set(cropRect.left + (cropRect.right - cropRect.left) / 2 - radius, cropRect.bottom - radius,
                cropRect.left + (cropRect.right - cropRect.left) / 2 + radius, cropRect.bottom + radius);

        canvas.drawBitmap(leftTopBit, bitRect, leftTopCircleRect, null);
        canvas.drawBitmap(rightTopBit, bitRect, rightTopCircleRect, null);
        canvas.drawBitmap(leftBottomBit, bitRect, leftBottomRect, null);
        canvas.drawBitmap(rightBottomBit, bitRect, rightBottomRect, null);

        canvas.drawBitmap(horizontalBit, bitRect, topCenterRect, null);
        canvas.drawBitmap(verticalBit, bitRect, leftCenterRect, null);
        canvas.drawBitmap(verticalBit, bitRect, rightCenterRect, null);
        canvas.drawBitmap(horizontalBit, bitRect, bottomCenterRect, null);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        imageView.layout((int) imageRect.left, (int) imageRect.top, (int) imageRect.right, (int) imageRect.bottom);
        expandImageView.layout((int) cropRect.left, (int) cropRect.top, (int) cropRect.right, (int) cropRect.bottom);
    }

    /**
     * 触摸事件处理
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean ret = super.onTouchEvent(event);// 是否向下传递事件标志 true为消耗
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                expandImageView.setVisibility(INVISIBLE);
                int selectCircle = isSeletedControllerCircle(x, y);
                if (selectCircle > 0) {// 选择控制点
                    ret = true;
                    selectedControllerCicle = selectCircle;// 记录选中控制点编号
                    status = STATUS_SCALE;// 进入缩放状态
                }
                //禁止移动
                /*else if (cropRect.contains(x, y)) {// 选择缩放框内部
                    ret = true;
                    status = STATUS_MOVE;// 进入移动状态
                } else {// 没有选择

                }*/// end if
                break;
            case MotionEvent.ACTION_MOVE:
                if (status == STATUS_SCALE) {// 缩放控制
                    // System.out.println("缩放控制");
                    scaleCropController(x, y);
                } else if (status == STATUS_MOVE) {// 移动控制
                    // System.out.println("移动控制");
                    translateCrop(x - oldx, y - oldy);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                status = STATUS_IDLE;// 回归空闲状态
                break;
        }// end switch

        // 记录上一次动作点
        oldx = x;
        oldy = y;

        return ret;
    }

    /**
     * 设置图片
     */
    public void setImageBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        expandImageView.setVisibility(INVISIBLE);
        imageView.setImageBitmap(bitmap);
//        setImageRatio(bitmap.getWidth(), bitmap.getHeight());
        setImageRatio(400, 400);
    }

    /**
     * 设置扩图图片
     */
    public void setExpandImageBitmap(Bitmap bitmap) {
        expandImageView.setVisibility(VISIBLE);
        expandImageView.setImageBitmap(bitmap);
        requestLayout();
    }

    /**
     * 设置扩图图片显示隐藏
     */
    public void setExpandImageVisible(boolean isVisible) {
        expandImageView.setVisibility(isVisible ? VISIBLE : INVISIBLE);
        requestLayout();
    }

    /**
     * 设置图片比例
     */
    public void setImageRatio(int width, int height) {
//        int magin = dp2px(46);
//        if (width >= height) {   //宽大
//            int imageHeight = (getWidth() - 2 * magin) * height / width;
//            int top = (getHeight() - imageHeight) / 2;
//            int bottom = top + imageHeight;
//            imageRect.set(new Rect(magin, top, getWidth() - magin, bottom));
//        } else {
//            int imageWidth = (getHeight() - 2 * magin) * width / height;
//            if ((imageWidth + 2 * magin) > getWidth()) {//宽大
//                int imageHeight = (getWidth() - 2 * magin) * height / width;
//                int top = (getHeight() - imageHeight) / 2;
//                int bottom = top + imageHeight;
//                imageRect.set(new Rect(magin, top, getWidth() - magin, bottom));
//            } else {//高大
//                int left = (getWidth() - imageWidth) / 2;
//                int right = left + imageWidth;
//                imageRect.set(new Rect(left, magin, right, getHeight() - magin));
//            }
//        }
        int magin = dp2px(46);
        if (((getHeight() - 2f * magin) * width / height + 2 * magin) >= getWidth()) {   //宽大
            int imageHeight = (getWidth() - 2 * magin) * height / width;
            int top = (getHeight() - imageHeight) / 2;
            int bottom = top + imageHeight;
            imageRect.set(new Rect(magin, top, getWidth() - magin, bottom));
        } else {//高大
            int imageWidth = (getHeight() - 2 * magin) * width / height;
            int left = (getWidth() - imageWidth) / 2;
            int right = left + imageWidth;
            imageRect.set(new Rect(left, magin, right, getHeight() - magin));
        }
        cropRect.set(imageRect);
        invalidate();
    }

    /**
     * 重置剪裁面
     *
     * @param rect
     */
    public void setCropRect(RectF rect) {
        if (rect == null)
            return;

        imageRect.set(new Rect(200, 200, 800, 800));
        cropRect.set(imageRect);
//        scaleRect(cropRect, 0.5f);
        invalidate();
    }

    public void setRatioCropRect(RectF rect, float r) {
        this.ratio = r;
        if (r < 0) {
            setCropRect(rect);
            return;
        }

        imageRect.set(rect);
        cropRect.set(rect);
        // setCropRect(rect);
        // 调整Rect

        float h, w;
        if (cropRect.width() >= cropRect.height()) {// w>=h
            h = cropRect.height() / 2;
            w = this.ratio * h;
        } else {// w<h
            w = rect.width() / 2;
            h = w / this.ratio;
        }// end if
        float scaleX = w / cropRect.width();
        float scaleY = h / cropRect.height();
        scaleRect(cropRect, scaleX, scaleY);
        invalidate();
    }

    /**
     * 移动剪切框
     *
     * @param dx
     * @param dy
     */
    private void translateCrop(float dx, float dy) {
        tempRect.set(cropRect);// 存贮原有数据，以便还原

        translateRect(cropRect, dx, dy);
        // 边界判定算法优化
        float mdLeft = imageRect.left - cropRect.left;
        if (mdLeft > 0) {
            translateRect(cropRect, mdLeft, 0);
        }
        float mdRight = imageRect.right - cropRect.right;
        if (mdRight < 0) {
            translateRect(cropRect, mdRight, 0);
        }
        float mdTop = imageRect.top - cropRect.top;
        if (mdTop > 0) {
            translateRect(cropRect, 0, mdTop);
        }
        float mdBottom = imageRect.bottom - cropRect.bottom;
        if (mdBottom < 0) {
            translateRect(cropRect, 0, mdBottom);
        }

        this.invalidate();
    }

    /**
     * 移动矩形
     *
     * @param rect
     * @param dx
     * @param dy
     */
    private static final void translateRect(RectF rect, float dx, float dy) {
        rect.left += dx;
        rect.right += dx;
        rect.top += dy;
        rect.bottom += dy;
    }

    /**
     * 操作控制点 控制缩放
     *
     * @param x
     * @param y
     */
    private void scaleCropController(float x, float y) {
        tempRect.set(cropRect);// 存贮原有数据，以便还原
        switch (selectedControllerCicle) {
            case 1:// 左上角控制点
                cropRect.left = x > MAGIN_WIDTH ? x : MAGIN_WIDTH;
                cropRect.top = y > MAGIN_WIDTH ? y : MAGIN_WIDTH;
                break;
            case 2:// 右上角控制点
                cropRect.right = x > (getWidth() - MAGIN_WIDTH) ? getWidth() - MAGIN_WIDTH : x;
                cropRect.top = y < MAGIN_WIDTH ? MAGIN_WIDTH : y;
                break;
            case 3:// 左下角控制点
                cropRect.left = x > MAGIN_WIDTH ? x : MAGIN_WIDTH;
                cropRect.bottom = y > (getHeight() - MAGIN_WIDTH) ? getHeight() - MAGIN_WIDTH : y;
                break;
            case 4:// 右下角控制点
                cropRect.right = x > (getWidth() - MAGIN_WIDTH) ? getWidth() - MAGIN_WIDTH : x;
                cropRect.bottom = y > (getHeight() - MAGIN_WIDTH) ? getHeight() - MAGIN_WIDTH : y;
                break;
            case 5:// 上方中间控制点
                cropRect.top = y < MAGIN_WIDTH ? MAGIN_WIDTH : y;
                break;
            case 6:// 左边中间控制点
                cropRect.left = x > MAGIN_WIDTH ? x : MAGIN_WIDTH;
                break;
            case 7:// 右边中间控制点
                cropRect.right = x > (getWidth() - MAGIN_WIDTH) ? getWidth() - MAGIN_WIDTH : x;
                break;
            case 8:// 下方中间控制点
                cropRect.bottom = y > (getHeight() - MAGIN_WIDTH) ? getHeight() - MAGIN_WIDTH : y;
                break;
        }// end switch

        if (ratio < 0) {// 任意缩放比
            // 边界条件检测
            validateCropRect();
            invalidate();
        } else {
            // 更新剪切矩形长宽
            // 确定不变点
            switch (selectedControllerCicle) {
                case 1:// 左上角控制点
                case 2:// 右上角控制点
                    cropRect.top = cropRect.bottom
                            - (cropRect.right - cropRect.left) / this.ratio;
                    break;
                case 3:// 左下角控制点
                case 4:// 右下角控制点
                    cropRect.bottom = (cropRect.right - cropRect.left) / this.ratio
                            + cropRect.top;
                    break;
            }// end switch

            // validateCropRect();
            if (cropRect.left < imageRect.left
                    || cropRect.right > imageRect.right
                    || cropRect.top < imageRect.top
                    || cropRect.bottom > imageRect.bottom
                    || cropRect.width() < MAGIN_WIDTH
                    || cropRect.height() < MAGIN_WIDTH) {
                cropRect.set(tempRect);
            }
            invalidate();
        }// end if
    }

    /**
     * 边界条件检测
     */
    private void validateCropRect() {
        if (cropRect.width() < MAGIN_WIDTH) {
            cropRect.left = tempRect.left;
            cropRect.right = tempRect.right;
        }
        if (cropRect.height() < MAGIN_WIDTH) {
            cropRect.top = tempRect.top;
            cropRect.bottom = tempRect.bottom;
        }

        // 图内
//        if (cropRect.left < imageRect.left) {
//            cropRect.left = imageRect.left;
//        }
//        if (cropRect.right > imageRect.right) {
//            cropRect.right = imageRect.right;
//        }
//        if (cropRect.top < imageRect.top) {
//            cropRect.top = imageRect.top;
//        }
//        if (cropRect.bottom > imageRect.bottom) {
//            cropRect.bottom = imageRect.bottom;
//        }

        //图外
        if (cropRect.left > imageRect.left) {
            cropRect.left = imageRect.left;
        }
        if (cropRect.right < imageRect.right) {
            cropRect.right = imageRect.right;
        }
        if (cropRect.top > imageRect.top) {
            cropRect.top = imageRect.top;
        }
        if (cropRect.bottom < imageRect.bottom) {
            cropRect.bottom = imageRect.bottom;
        }
    }

    /**
     * 是否选中控制点
     * <p>
     * -1为没有
     *
     * @param x
     * @param y
     * @return
     */
    private int isSeletedControllerCircle(float x, float y) {
        if (leftTopCircleRect.contains(x, y))// 选中左上角
            return 1;
        if (rightTopCircleRect.contains(x, y))// 选中右上角
            return 2;
        if (leftBottomRect.contains(x, y))// 选中左下角
            return 3;
        if (rightBottomRect.contains(x, y))// 选中右下角
            return 4;
        if (topCenterRect.contains(x, y))// 选中上方中间
            return 5;
        if (leftCenterRect.contains(x, y))// 选中左边中间
            return 6;
        if (rightCenterRect.contains(x, y))// 选中右边中间
            return 7;
        if (bottomCenterRect.contains(x, y))// 选中下方中间
            return 8;
        return -1;
    }

    /**
     * 返回上下左右扩展像素点矩形
     *
     * @return
     */
    public RectF getExpandRect() {
        if (bitmap == null) {
            return null;
        }
        float perPx = bitmap.getWidth() / (imageRect.right - imageRect.left);
//        return new RectF(imageRect.left - cropRect.left,
//                imageRect.top - cropRect.top,
//                cropRect.right - imageRect.right,
//                cropRect.bottom - imageRect.bottom);
        return new RectF((imageRect.left - cropRect.left) * perPx,
                (imageRect.top - cropRect.top) * perPx,
                (cropRect.right - imageRect.right) * perPx,
                (cropRect.bottom - imageRect.bottom) * perPx);
    }

    /**
     * 返回剪切矩形
     *
     * @return
     */
    public RectF getCropRect() {
        return new RectF(this.cropRect);
    }

    /**
     * 缩放指定矩形
     *
     * @param rect
     */
    private static void scaleRect(RectF rect, float scaleX, float scaleY) {
        float w = rect.width();
        float h = rect.height();

        float newW = scaleX * w;
        float newH = scaleY * h;

        float dx = (newW - w) / 2;
        float dy = (newH - h) / 2;

        rect.left -= dx;
        rect.top -= dy;
        rect.right += dx;
        rect.bottom += dy;
    }

    /**
     * 缩放指定矩形
     *
     * @param rect
     * @param scale
     */
    private static void scaleRect(RectF rect, float scale) {
        scaleRect(rect, scale, scale);
    }

    public float getRatio() {
        return ratio;
    }

    public void setRatio(float ratio) {
        this.ratio = ratio;
    }

    private int dp2px(float dipValue) {
        float scale = getContext().getApplicationContext().getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }
}

