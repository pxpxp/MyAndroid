package com.example.myandroid.video.keyframe.video7;

import com.example.myandroid.video.keyframe.video1.KFFrame;

/**
 * @author pxp
 * @description 提供了相机打开回调、相机关闭回调、以及相机出错回调的接口。
 * 外层可以根据 相机打开回调 优先将 CPU 等资源分配给相机，打开成功后执行 UI 等其它布局，提升用户体验。
 */
public interface KFVideoCaptureListener {
    ///< 摄像机打开。
    void cameraOnOpened();

    ///< 摄像机关闭。
    void cameraOnClosed();

    ///< 摄像机出错。
    void cameraOnError(int error, String errorMsg);

    ///< 数据回调给外层。
    void onFrameAvailable(KFFrame frame);
}
