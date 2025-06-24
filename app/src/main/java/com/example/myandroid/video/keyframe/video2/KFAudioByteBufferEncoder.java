package com.example.myandroid.video.keyframe.video2;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.myandroid.video.keyframe.video1.KFBufferFrame;
import com.example.myandroid.video.keyframe.video1.KFFrame;

import java.nio.ByteBuffer;

/**
 * @author pxp
 * @description
 * 主要就干了一件事：拆分合适大小（2048 字节）的数据送给编码器。因为 AAC 数据编码每 packet 大小为 1024 * 2（位深 16 Bit）。
 */
public class KFAudioByteBufferEncoder extends KFByteBufferCodec {
    private int mChannel = 0; ///< 音频声道数
    private int mSampleRate = 0; ///< 音频采样率
    private long mCurrentTimestamp = -1; ///< 标记当前时间戳 (因为数据重新分割，所以时间戳需要手动计算)
    private byte[] mByteArray = new byte[500 * 1024]; ///< 输入音频数据数组
    private int mByteArraySize = 0; ///< 输入音频数据 Size

    @Override
    public int processFrame(KFFrame inputFrame) {
        ///< 获取音频声道数与采样率。
        if (mChannel == 0) {
            MediaFormat inputMediaFormat = getInputMediaFormat();
            if (inputMediaFormat != null) {
                mChannel = inputMediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
                mSampleRate = inputMediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
            }
        }

        if (mChannel == 0  || mSampleRate == 0 || inputFrame == null) {
            return KFMediaCodeProcessParams;
        }

        KFBufferFrame bufferFrame = (KFBufferFrame)inputFrame;
        if (bufferFrame.bufferInfo == null || bufferFrame.bufferInfo.size == 0) {
            return KFMediaCodeProcessParams;
        }

        ///< 控制音频输入给编码器单次字节数 2048 字节。
        int sendSize = 2048;
        ///< 外层输入如果为 2048 则直接跳过执行。
        if (mByteArraySize == 0 && sendSize == bufferFrame.bufferInfo.size) {
            return super.processFrame(inputFrame);
        } else {
            long currentTimestamp = 0;
            if (mCurrentTimestamp == -1) {
                mCurrentTimestamp = bufferFrame.bufferInfo.presentationTimeUs;
            }

            ///< 将缓存中数据执行送入编码器操作。
            int sendCacheStatus = sendBufferEncoder(sendSize);
            if (sendCacheStatus < 0) {
                return sendCacheStatus;
            }

            ///< 将输入数据送入缓冲区重复执行此操作。
            byte[] inputBytes = new byte[bufferFrame.bufferInfo.size];
            bufferFrame.buffer.get(inputBytes);

            System.arraycopy(inputBytes,0,mByteArray,mByteArraySize,bufferFrame.bufferInfo.size);
            mByteArraySize += bufferFrame.bufferInfo.size;

            return sendBufferEncoder(sendSize);
        }
    }

    @Override
    public void release() {
        mCurrentTimestamp = -1;
        mByteArraySize = 0;
        super.release();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void flush() {
        mCurrentTimestamp = -1;
        mByteArraySize = 0;
        super.flush();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private int sendBufferEncoder(int sendSize) {
        ///< 将当前 Buffer 中数据按每次 2048 送给编码器。
        while (mByteArraySize >= sendSize) {
            MediaCodec.BufferInfo newBufferInfo = new MediaCodec.BufferInfo();
            newBufferInfo.size = sendSize;
            newBufferInfo.presentationTimeUs = mCurrentTimestamp;

            ByteBuffer newBuffer = ByteBuffer.allocateDirect(sendSize);
            newBuffer.put(mByteArray,0,sendSize).position(0);

            KFBufferFrame newFrame = new KFBufferFrame();
            newFrame.buffer = newBuffer;
            newFrame.bufferInfo = newBufferInfo;
            int status = super.processFrame(newFrame);
            if (status < 0) {
                return status;
            } else {
                mByteArraySize -= sendSize;
                if (mByteArraySize > 0) {
                    System.arraycopy(mByteArray, sendSize, mByteArray, 0, mByteArraySize);
                }
            }
            mCurrentTimestamp += sendSize * 1000000 / (16 / 8 * mSampleRate * mChannel);
        }
        return KFMediaCodeProcessSuccess;
    }
}