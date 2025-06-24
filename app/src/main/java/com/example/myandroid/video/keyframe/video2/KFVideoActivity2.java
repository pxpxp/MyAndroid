package com.example.myandroid.video.keyframe.video2;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaFormat;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.myandroid.R;
import com.example.myandroid.video.keyframe.video1.KFAudioCapture;
import com.example.myandroid.video.keyframe.video1.KFAudioCaptureConfig;
import com.example.myandroid.video.keyframe.video1.KFAudioCaptureListener;
import com.example.myandroid.video.keyframe.video1.KFBufferFrame;
import com.example.myandroid.video.keyframe.video1.KFFrame;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * https://mp.weixin.qq.com/s?__biz=MjM5MTkxOTQyMQ==&mid=2257485614&idx=1&sn=636683b05eacc4f4728fb2849a445ded&chksm=a5d4e37c92a36a6a8a4d3d1991cebc5fde3775086b4da22d78924d7f965e78fccbf6605c0be5&scene=178&cur_album_id=2273301900659851268#rd
 * Android AVDemo（2）：音频编码，采集 PCM 数据编码为 AAC丨音视频工程示例
 */
public class KFVideoActivity2 extends AppCompatActivity {

    private FileOutputStream mStream = null;
    private KFAudioCapture mAudioCapture = null; ///< 音频采集模块
    private KFAudioCaptureConfig mAudioCaptureConfig = null; ///< 音频采集配置
    private KFMediaCodecInterface mEncoder = null; ///< 音频编码
    private MediaFormat mAudioEncoderFormat = null; ///< 音频编码格式描述

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kf_video2);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        }

        mAudioCaptureConfig = new KFAudioCaptureConfig();
        mAudioCapture = new KFAudioCapture(mAudioCaptureConfig, mAudioCaptureListener);
        mAudioCapture.startRunning();

        if (mStream == null) {
            try {
                File file = new File(Environment.getExternalStorageDirectory().getPath() + "/test.aac");
                if (!file.exists()) {
                    file.createNewFile();
                }
                mStream = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        FrameLayout.LayoutParams startParams = new FrameLayout.LayoutParams(200, 120);
        startParams.gravity = Gravity.CENTER_HORIZONTAL;
        Button startButton = new Button(this);
        startButton.setTextColor(Color.BLUE);
        startButton.setText("开始");
        startButton.setVisibility(View.VISIBLE);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mEncoder == null) {
                    mEncoder = new KFAudioByteBufferEncoder();
                    MediaFormat mediaFormat = KFAVTools.createAudioFormat(mAudioCaptureConfig.sampleRate, mAudioCaptureConfig.channel, 96 * 1000);
                    mEncoder.setup(true, mediaFormat, mAudioEncoderListener, null);
                    ((Button) view).setText("停止");
                } else {
                    mEncoder.release();
                    mEncoder = null;
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
            ///< 音频回调数据
            if (mAudioEncoderFormat == null && mEncoder != null) {
                mAudioEncoderFormat = mEncoder.getOutputMediaFormat();
            }
            KFBufferFrame bufferFrame = (KFBufferFrame) frame;
            try {
                ///< 添加ADTS数据
//                ByteBuffer adtsBuffer = KFAVTools.getADTS(bufferFrame.bufferInfo.size,mAudioEncoderFormat.getInteger(MediaFormat.KEY_PROFILE),mAudioEncoderFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE),mAudioEncoderFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT));
                ByteBuffer adtsBuffer = KFAVTools.getADTS(bufferFrame.bufferInfo.size, 2, mAudioEncoderFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE), mAudioEncoderFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT));
                byte[] adtsBytes = new byte[adtsBuffer.capacity()];
                adtsBuffer.get(adtsBytes);
                mStream.write(adtsBytes);

                byte[] dst = new byte[bufferFrame.bufferInfo.size];
                bufferFrame.buffer.get(dst);
                mStream.write(dst);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };
}
