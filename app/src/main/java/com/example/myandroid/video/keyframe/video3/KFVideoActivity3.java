package com.example.myandroid.video.keyframe.video3;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.myandroid.R;
import com.example.myandroid.video.keyframe.video1.KFAudioCapture;
import com.example.myandroid.video.keyframe.video1.KFAudioCaptureConfig;
import com.example.myandroid.video.keyframe.video1.KFAudioCaptureListener;
import com.example.myandroid.video.keyframe.video1.KFBufferFrame;
import com.example.myandroid.video.keyframe.video1.KFFrame;
import com.example.myandroid.video.keyframe.video2.KFAVTools;
import com.example.myandroid.video.keyframe.video2.KFAudioByteBufferEncoder;
import com.example.myandroid.video.keyframe.video2.KFMediaCodecInterface;
import com.example.myandroid.video.keyframe.video2.KFMediaCodecListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * https://mp.weixin.qq.com/s?__biz=MjM5MTkxOTQyMQ==&mid=2257485647&idx=1&sn=abbe7b2c79310c85209aaa2187b6ba93&chksm=a5d4e31d92a36a0b69c7a99b3cf0ff808a4da60f3cb09d1cf6812f048c0022de64541613f398&cur_album_id=2273301900659851268&scene=189#wechat_redirect
 * * Android AVDemo（3）：音频封装，采集编码并封装为 M4A丨音视频工程示例
 */
public class KFVideoActivity3 extends AppCompatActivity {
    private KFAudioCapture mAudioCapture = null; ///< 音频采集
    private KFAudioCaptureConfig mAudioCaptureConfig = null; ///< 音频采集配置
    private KFMediaCodecInterface mEncoder = null; ///< 音频编码
    private MediaFormat mAudioEncoderFormat = null; ///< 音频编码格式描述
    private KFMP4Muxer mMuxer; ///< 封装起器
    private KFMuxerConfig mMuxerConfig; ///< 封装器配置

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kf_video3);

        ///< 申请存储、音频采集权限。
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) this, new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        ///< 创建采集实例。
        mAudioCaptureConfig = new KFAudioCaptureConfig();
        mAudioCapture = new KFAudioCapture(mAudioCaptureConfig, mAudioCaptureListener);
        mAudioCapture.startRunning();

        mMuxerConfig = new KFMuxerConfig(Environment.getExternalStorageDirectory().getPath() + "/test.m4a");
        mMuxerConfig.muxerType = KFMediaBase.KFMediaType.KFMediaAudio;

        FrameLayout.LayoutParams startParams = new FrameLayout.LayoutParams(200, 120);
        startParams.gravity = Gravity.CENTER_HORIZONTAL;
        Button startButton = new Button(this);
        startButton.setTextColor(Color.BLUE);
        startButton.setText("开始");
        startButton.setVisibility(View.VISIBLE);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ///< 创建音频编码实例。
                if (mEncoder == null) {
                    mEncoder = new KFAudioByteBufferEncoder();
                    MediaFormat mediaFormat = KFAVTools.createAudioFormat(mAudioCaptureConfig.sampleRate, mAudioCaptureConfig.channel, 96 * 1000);
                    mEncoder.setup(true, mediaFormat, mAudioEncoderListener, null);
                    ((Button) view).setText("停止");
                    mMuxer = new KFMP4Muxer(mMuxerConfig, mMuxerListener);
                } else {
                    mEncoder.release();
                    mEncoder = null;
                    mMuxer.stop();
                    mMuxer.release();
                    mMuxer = null;
                    ((Button) view).setText("开始");
                }
            }
        });
        addContentView(startButton, startParams);
    }

    private KFAudioCaptureListener mAudioCaptureListener = new KFAudioCaptureListener() {
        @Override
        public void onError(int error, String errorMsg) {
            Log.e("KFAudioCapture", "errorCode" + error + "msg" + errorMsg);
        }

        @Override
        public void onFrameAvailable(KFFrame frame) {
            ///< 采集回调输入编码。
            if (mEncoder != null) {
                mEncoder.processFrame(frame);
            }
        }
    };

    private KFMediaCodecListener mAudioEncoderListener = new KFMediaCodecListener() {
        @Override
        public void onError(int error, String errorMsg) {
            Log.i("KFMediaCodecListener", "error" + error + "msg" + errorMsg);
        }

        @Override
        public void dataOnAvailable(KFFrame frame) {
            ///< 编码回调写入封装器。
            if (mAudioEncoderFormat == null && mEncoder != null) {
                mAudioEncoderFormat = mEncoder.getOutputMediaFormat();
                mMuxer.setAudioMediaFormat(mEncoder.getOutputMediaFormat());
                mMuxer.start();
            }

            if (mMuxer != null) {
                mMuxer.writeSampleData(false, ((KFBufferFrame) frame).buffer, ((KFBufferFrame) frame).bufferInfo);
            }
        }
    };

    private KFMuxerListener mMuxerListener = new KFMuxerListener() {
        @Override
        public void muxerOnError(int error, String errorMsg) {
            ///< 音频封装错误回调。
            Log.i("KFMuxerListener", "error" + error + "msg" + errorMsg);
        }
    };
}
