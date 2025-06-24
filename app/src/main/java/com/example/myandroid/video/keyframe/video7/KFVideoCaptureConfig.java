package com.example.myandroid.video.keyframe.video7;

import android.hardware.camera2.CameraCharacteristics;
import android.util.Size;

/**
 * @author pxp
 * @description 类用于定义视频采集参数的配置。
 */
public class KFVideoCaptureConfig {
    ///< 摄像头方向。
    public Integer cameraFacing = CameraCharacteristics.LENS_FACING_FRONT;
    ///< 分辨率。
    public Size resolution = new Size(1080, 1920);
    ///< 帧率。
    public Integer fps = 30;
}
