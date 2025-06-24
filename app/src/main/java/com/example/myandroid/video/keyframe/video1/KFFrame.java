package com.example.myandroid.video.keyframe.video1;

/**
 * @author pxp
 * @description
 * 音频数据对象，数据包含 Buffer 数据与 Texture 数据，音频仅涉及 Buffer 数据。
 */
public class KFFrame {
    public enum KFFrameType {
        KFFrameBuffer,
        KFFrameTexture;
    }

    public KFFrameType frameType = KFFrameType.KFFrameBuffer;
    public KFFrame(KFFrameType type) {
        frameType = type;
    }
}
