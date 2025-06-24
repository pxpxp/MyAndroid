package com.example.myandroid.video.keyframe.video7;

import android.content.Context;

import android.opengl.EGLContext;

/**
 * @author pxp
 * @description 视频采集接口。主要包含 初始化、开始采集、停止采集、切换摄像头等接口。
 */
public interface KFIVideoCapture {
    ///< 视频采集初始化。
    public void setup(Context context, KFVideoCaptureConfig config, KFVideoCaptureListener listener, EGLContext eglShareContext);

    ///< 释放采集实例。
    public void release();

    ///< 开始采集。
    public void startRunning();

    ///< 关闭采集。
    public void stopRunning();

    ///< 是否正在采集。
    public boolean isRunning();

    ///< 获取 OpenGL 上下文。
    public EGLContext getEGLContext();

    ///< 切换摄像头。
    public void switchCamera();
}
