package com.example.myandroid.video.keyframe.video1;

/**
 * @author pxp
 * @description
 * 实现采集回调，包含错误回调与数据回调
 */
public interface KFAudioCaptureListener {
    void onError(int error,String errorMsg);
    void onFrameAvailable(KFFrame frame);
}
