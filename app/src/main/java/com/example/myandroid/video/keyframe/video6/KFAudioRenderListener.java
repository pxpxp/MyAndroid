package com.example.myandroid.video.keyframe.video6;

/**
 * @author pxp
 * @description
 * 音频渲染模块 KFAudioRender，在这里输入解码后的数据进行渲染播放。
 */
public interface KFAudioRenderListener {
    ///< 出错回调。
    void onError(int error,String errorMsg);
    ///< 获取PCM数据。
    byte[] audioPCMData(int size);
}
