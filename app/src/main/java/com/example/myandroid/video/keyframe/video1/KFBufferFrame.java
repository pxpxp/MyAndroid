package com.example.myandroid.video.keyframe.video1;

import static com.example.myandroid.video.keyframe.video1.KFFrame.KFFrameType.KFFrameBuffer;

import android.media.MediaCodec;

import java.nio.ByteBuffer;

/**
 * @author pxp
 * @description
 * 音频 Buffer 数据 KFBufferFrame，继承自 KFFrame，包含 ByteBuffer 数据与 BufferInfo 数据信息。BufferInfo 为了提供时间戳 presentationTimeUs 与 size。
 */
public class KFBufferFrame extends KFFrame {
    public ByteBuffer buffer;
    public MediaCodec.BufferInfo bufferInfo;

    public KFBufferFrame() {
        super(KFFrameBuffer);
    }

    public KFBufferFrame(ByteBuffer inputBuffer, MediaCodec.BufferInfo inputBufferInfo) {
        super(KFFrameBuffer);
        buffer = inputBuffer;
        bufferInfo = inputBufferInfo;
    }

    public KFFrameType frameType() {
        return KFFrameBuffer;
    }
}
