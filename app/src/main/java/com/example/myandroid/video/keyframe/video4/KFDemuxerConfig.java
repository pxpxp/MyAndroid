package com.example.myandroid.video.keyframe.video4;

import com.example.myandroid.video.keyframe.video3.KFMediaBase;

/**
 * @author pxp
 * @description
 * 用于定义音频解封装参数的配置。这里包括了：视频路径、解封装类型这几个参数。
 */
public class KFDemuxerConfig {
    ///< 输入路径。
    public String path;
    ///< 音视频解封装类型（仅音频、仅视频、音视频）。
    public KFMediaBase.KFMediaType demuxerType = KFMediaBase.KFMediaType.KFMediaAV;
}
