package com.example.myandroid.video.keyframe.video3;

/**
 * @author pxp
 * @description KFMuxerConfig 类用于定义 MP4 封装的参数的配置。这里包括了：封装文件输出地址、封装文件类型这几个参数。
 */
public class KFMuxerConfig {
    ///< 输出路径。
    public String outputPath = null;
    ///< 封装仅音频、仅视频、音视频。
    public KFMediaBase.KFMediaType muxerType = KFMediaBase.KFMediaType.KFMediaAV;

    public KFMuxerConfig(String path) {
        outputPath = path;
    }
}
