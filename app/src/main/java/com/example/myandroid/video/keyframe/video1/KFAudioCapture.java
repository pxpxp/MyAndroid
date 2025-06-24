package com.example.myandroid.video.keyframe.video1;

import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author pxp
 * @description
 * 实现音频采集
 */
public class KFAudioCapture {
    public static int KFAudioCaptureErrorCreate = -2600;
    public static int KFAudioCaptureErrorStart = -2601;
    public static int KFAudioCaptureErrorStop = -2602;

    private static final String TAG = "KFAudioCapture";
    private KFAudioCaptureConfig mConfig = null; ///< 音频配置
    private KFAudioCaptureListener mListener = null; ///< 音频回调
    private HandlerThread mRecordThread = null; ///< 音频采集线程
    private Handler mRecordHandle = null;

    private HandlerThread mReadThread = null; ///< 音频读数据线程
    private Handler mReadHandle = null;
    private int mMinBufferSize = 0;

    private AudioRecord mAudioRecord = null; ///< 音频采集实例
    private boolean mRecording = false;
    private Handler mMainHandler = new Handler(Looper.getMainLooper()); ///< 主线程用作错误回调

    public KFAudioCapture(KFAudioCaptureConfig config, KFAudioCaptureListener listener) {
        mConfig = config;
        mListener = listener;

        mRecordThread = new HandlerThread("KFAudioCaptureThread");
        mRecordThread.start();
        mRecordHandle = new Handler((mRecordThread.getLooper()));

        mReadThread = new HandlerThread("KFAudioCaptureReadThread");
        mReadThread.start();
        mReadHandle = new Handler((mReadThread.getLooper()));

        mRecordHandle.post(() -> {
            ///< 初始化音频采集实例。
            _setupAudioRecord();
        });
    }

    public void startRunning() {
        ///< 开启音频采集。
        mRecordHandle.post(() -> {
            if (mAudioRecord != null && !mRecording) {
                try {
                    mAudioRecord.startRecording();
                    mRecording = true;
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                    _callBackError(KFAudioCaptureErrorStart, e.getMessage());
                }

                ///< 音频采集采用拉数据模式，通过读数据线程开启循环无限拉取 PCM 数据，拉到数据后进行回调。
                mReadHandle.post(() -> {
                    while (mRecording) {
                        final byte[] pcmData = new byte[mMinBufferSize];
                        int readSize = mAudioRecord.read(pcmData, 0, mMinBufferSize);
                        if (readSize > 0) {
                            ///< 处理音频数据 data。
                            ByteBuffer buffer = ByteBuffer.allocateDirect(readSize).put(pcmData).order(ByteOrder.nativeOrder());
                            buffer.position(0);
                            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                            bufferInfo.presentationTimeUs = System.nanoTime() / 1000;
                            bufferInfo.size = readSize;
                            KFBufferFrame bufferFrame = new KFBufferFrame(buffer, bufferInfo);
                            if (mListener != null) {
                                mListener.onFrameAvailable(bufferFrame);
                            }
                        }
                    }
                });
            }
        });
    }

    public void stopRunning() {
        ///< 关闭音频采集。
        mRecordHandle.post(() -> {
            if (mAudioRecord != null && mRecording) {
                try {
                    mAudioRecord.stop();
                    mRecording = false;
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                    _callBackError(KFAudioCaptureErrorStart, e.getMessage());
                }
            }
        });
    }

    public void release() {
        ///< 外层主动触发释放，释放采集实例、线程。
        mRecordHandle.post(() -> {
            if (mAudioRecord != null) {
                if (mRecording) {
                    try {
                        mAudioRecord.stop();
                        mRecording = false;
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    }
                }

                try {
                    mAudioRecord.release();
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
                mAudioRecord = null;
            }

            mRecordThread.quit();
            mReadThread.quit();
        });
    }

    @SuppressLint("MissingPermission")
    private void _setupAudioRecord() {
        if (mAudioRecord == null) {
            ///< 根据指定采样率、声道、位深获取每次回调数据大小。
//            getMinBufferSize用于获取成功创建AudioRecord对象所需的最小缓冲区大小,
//                    此大小不能保证在负载下能顺利录制，应根据预期的频率选择更高的值，
//            在该频率下，将对AudioRecord实例进行轮询以获取新数据
//            参数介绍：(具体看官网api介绍)
//            sampleRateInHz：采样率，以赫兹为单位
//            channelConfig：音频通道的配置
//            audioFormat：音频数据的格式
            mMinBufferSize = AudioRecord.getMinBufferSize(mConfig.sampleRate, mConfig.channel, AudioFormat.ENCODING_PCM_16BIT);
            try {
                ///< 根据采样率、声道、位深每次回调数据大小生成采集实例。
//                audioSource：音频来源
//                sampleRateInHz：采样率，以赫兹为单位。目前，只有44100Hz是保证在所有设备上都可以使用的速率(最适合人耳的)，但是其他速率（例如22050、16000和11025）可能在某些设备上可以使用
//                channelConfig：音频通道的配置
//                audioFormat：音频数据的格式
//                bufferSizeInBytes：在录制期间写入音频数据的缓冲区的总大小（以字节为单位）
                mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, mConfig.sampleRate, mConfig.channel, AudioFormat.ENCODING_PCM_16BIT, mMinBufferSize);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                _callBackError(KFAudioCaptureErrorCreate, e.getMessage());
            }
        }
    }

    private void _callBackError(int error, String errorMsg) {
        ///< 错误回调。
        if (mListener != null) {
            mMainHandler.post(() -> {
                mListener.onError(error, TAG + errorMsg);
            });
        }
    }
}
