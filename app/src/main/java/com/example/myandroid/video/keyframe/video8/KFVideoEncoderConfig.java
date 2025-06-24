package com.example.myandroid.video.keyframe.video8;

import android.media.MediaCodecInfo;
import android.util.Size;

/**
 * @author pxp
 * @description
 */
public class KFVideoEncoderConfig {
    public Size size = new Size(720, 1280);
    public int bitrate = 4 * 1024 * 1024;
    public int fps = 30;
    public int gop = 30 * 4;
    public boolean isHEVC = false;
    public int profile = MediaCodecInfo.CodecProfileLevel.AVCProfileBaseline;
    public int profileLevel = MediaCodecInfo.CodecProfileLevel.AVCLevel1;

    public KFVideoEncoderConfig() {

    }
}
