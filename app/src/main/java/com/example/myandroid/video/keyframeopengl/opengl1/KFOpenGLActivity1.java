package com.example.myandroid.video.keyframeopengl.opengl1;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myandroid.R;
import com.example.myandroid.video.keyframe.video7.KFGLContext;

/**
 * https://mp.weixin.qq.com/s?__biz=MjM5MTkxOTQyMQ==&mid=2257485786&idx=1&sn=1dc16eedc3972f1c28b32b5fd58109eb&chksm=a5d4e38892a36a9e3cc0bcfd686634fbfa8a790be7ceafae38b5b4b630e4f65970ab15b27e8c&cur_album_id=2273301900659851268&scene=189#wechat_redirect
 * Android AVDemo（7）：视频采集，视频系列来了
 * <p>
 * 1）实现两个视频采集模块，分别为 Camera 与 Camera2；
 * 2）实现视频采集逻辑并将采集的视频图像渲染进行预览；
 * 3）详尽的代码注释，帮你理解代码逻辑和原理。
 * <p>
 * 实现视频采集并实时预览的逻辑。
 */
public class KFOpenGLActivity1 extends AppCompatActivity {

    private KFRenderView mRenderView; ///< 渲染视图。
    private KFGLContext mGLContext; ///< OpenGL 上下文。

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kf_opengl1);

        ///< OpenGL 上下文。
        mGLContext = new KFGLContext(null);
        ///< 渲染视图。
        mRenderView = new KFRenderView(this, mGLContext.getContext());
        WindowManager windowManager = (WindowManager) this.getSystemService(this.WINDOW_SERVICE);
        Rect outRect = new Rect();
        windowManager.getDefaultDisplay().getRectSize(outRect);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(outRect.width(), outRect.height());
        addContentView(mRenderView, params);
    }
}
