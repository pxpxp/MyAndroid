package com.example.myandroid.video.study1.video1;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myandroid.R;

import java.io.InputStream;

public class VideoActivity2 extends AppCompatActivity {

    private SurfaceView mSv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video2);

        mSv = findViewById(R.id.sv);

        initView();
    }

    private void initView() {
        mSv.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (holder == null) {
                    return;
                }
                Paint paint = new Paint();
                paint.setAntiAlias(true);
                paint.setStyle(Paint.Style.STROKE);
                InputStream is = getClass().getResourceAsStream("/assets/meinv1.jpg");
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                Canvas canvas = holder.lockCanvas();// 先锁定当前surfaceView的画布
                canvas.drawBitmap(bitmap, 0, 0, paint);//执行绘制操作
                holder.unlockCanvasAndPost(canvas);// 解除锁定并显示在界面上
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
    }
}
