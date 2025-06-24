package com.example.myandroid.video.keyframe.video2;

import com.example.myandroid.video.keyframe.video1.KFFrame;

/**
 * @author pxp
 * @description
 */
public interface KFMediaCodecListener {
    void onError(int error,String errorMsg);
    void dataOnAvailable(KFFrame frame);
}
