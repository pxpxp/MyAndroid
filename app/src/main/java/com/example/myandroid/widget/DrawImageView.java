package com.example.myandroid.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.example.myandroid.R;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class DrawImageView extends View {

    private Bitmap originalBitmap;
    private Bitmap resultBitmap;
    private Canvas resultCanvas;
    private List<Path> paths = new ArrayList<>();
    private Paint paint;

    public DrawImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // 加载原始图片
        originalBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.mm01);

        // 创建与原始图片相同大小的新位图用于存储结果
        resultBitmap = Bitmap.createBitmap(originalBitmap.getWidth(), originalBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        resultCanvas = new Canvas(resultBitmap);

        // 先将原始图片绘制到结果位图上
        resultCanvas.drawBitmap(originalBitmap, 0, 0, null);

        // 初始化画笔
        paint = new Paint();
        paint.setColor(Color.TRANSPARENT);
        paint.setStrokeWidth(5);
        paint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 先将原始图片绘制到结果位图上
//        resultCanvas.drawBitmap(originalBitmap, 0, 0, null);

        // 绘制用户绘制的透明路径
        for (Path path : paths) {
            resultCanvas.drawPath(path, paint);
        }

        // 绘制结果位图
        canvas.drawBitmap(resultBitmap, 0, 0, null);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Path path = new Path();
                path.moveTo(x, y);
                paths.add(path);
                break;
            case MotionEvent.ACTION_MOVE:
                Log.e("123456", "x:" + x + "---y:" + y);
                Path currentPath = paths.get(paths.size() - 1);
                currentPath.lineTo(x, y);
                resultBitmap.setPixel((int) x, (int) y, Color.TRANSPARENT);

                invalidate();
                break;
        }
        return true;
    }

    // 保存生成的新图片
    public void saveImage() {
        try {
            FileOutputStream outputStream = new FileOutputStream(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/aaa.png"));
            resultBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
