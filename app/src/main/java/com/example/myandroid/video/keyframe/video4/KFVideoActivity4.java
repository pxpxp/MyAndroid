package com.example.myandroid.video.keyframe.video4;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaCodec;
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
import com.example.myandroid.video.keyframe.video3.KFMP4Muxer;
import com.example.myandroid.video.keyframe.video3.KFMediaBase;
import com.example.myandroid.video.keyframe.video3.KFMuxerConfig;
import com.example.myandroid.video.keyframe.video3.KFMuxerListener;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * https://mp.weixin.qq.com/s?__biz=MjM5MTkxOTQyMQ==&mid=2257485659&idx=1&sn=1acc6ec2b1240f4b2e389668a14e34f7&chksm=a5d4e30992a36a1f51f4043a3badb5693d8df54c0a9694ed22663f9089795c58b9bb5b72625b&cur_album_id=2273301900659851268&scene=189#wechat_redirect
 * Android AVDemo（4）：音频解封装，从 MP4 中解封装出 AAC丨音视频工程示例
 */
public class KFVideoActivity4 extends AppCompatActivity {
    private KFMP4Demuxer mDemuxer; ///< 解封装实例
    private KFDemuxerConfig mDemuxerConfig; ///< 解封装配置
    private FileOutputStream mStream = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kf_video4);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        }

        mDemuxerConfig = new KFDemuxerConfig();
        mDemuxerConfig.path = Environment.getExternalStorageDirectory().getPath() + "/2.mp4";
        mDemuxerConfig.demuxerType = KFMediaBase.KFMediaType.KFMediaAudio;
        if (mStream == null) {
            try {
                mStream = new FileOutputStream(Environment.getExternalStorageDirectory().getPath() + "/test.aac");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
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
                ///< 创建解封装实例。
                if (mDemuxer == null) {
                    mDemuxer = new KFMP4Demuxer(mDemuxerConfig, mDemuxerListener);

                    ///< 读取音频数据。
                    MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                    ByteBuffer nextBuffer = mDemuxer.readAudioSampleData(bufferInfo);
                    while (nextBuffer != null) {
                        try {
                            ///< 添加 ADTS。
                            ByteBuffer adtsBuffer = KFAVTools.getADTS(bufferInfo.size, mDemuxer.audioProfile(), mDemuxer.samplerate(), mDemuxer.channel());
                            byte[] adtsBytes = new byte[adtsBuffer.capacity()];
                            adtsBuffer.get(adtsBytes);
                            mStream.write(adtsBytes);

                            byte[] dst = new byte[bufferInfo.size];
                            nextBuffer.get(dst);
                            mStream.write(dst);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        nextBuffer = mDemuxer.readAudioSampleData(bufferInfo);
                    }
                    Log.i("KFDemuxer", "complete");
                }
            }
        });
        addContentView(startButton, startParams);
    }

    private KFDemuxerListener mDemuxerListener = new KFDemuxerListener() {
        ///< 解封装错误回调。
        @Override
        public void demuxerOnError(int error, String errorMsg) {
            Log.i("KFDemuxer", "error" + error + "msg" + errorMsg);
        }
    };
}
