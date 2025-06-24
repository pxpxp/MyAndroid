package com.example.myandroid.video.keyframe.video5;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaCodec;
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
import com.example.myandroid.video.keyframe.video1.KFBufferFrame;
import com.example.myandroid.video.keyframe.video1.KFFrame;
import com.example.myandroid.video.keyframe.video2.KFAVTools;
import com.example.myandroid.video.keyframe.video2.KFByteBufferCodec;
import com.example.myandroid.video.keyframe.video2.KFMediaCodecInterface;
import com.example.myandroid.video.keyframe.video2.KFMediaCodecListener;
import com.example.myandroid.video.keyframe.video3.KFMediaBase;
import com.example.myandroid.video.keyframe.video4.KFDemuxerConfig;
import com.example.myandroid.video.keyframe.video4.KFDemuxerListener;
import com.example.myandroid.video.keyframe.video4.KFMP4Demuxer;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * https://mp.weixin.qq.com/s?__biz=MjM5MTkxOTQyMQ==&mid=2257485766&idx=1&sn=f6321fbfa47a674da5112a9ee6c1df7a&chksm=a5d4e39492a36a82b1e4da857886dee08bff8357c283503f26a7d2231e3bf78e485fe5b9c933&cur_album_id=2273301900659851268&scene=189#wechat_redirect
 * Android AVDemo（5）：音频解码
 */
public class KFVideoActivity5 extends AppCompatActivity {
    private KFMP4Demuxer mDemuxer; ///< 音频解封装
    private KFDemuxerConfig mDemuxerConfig; ///< 音频解封装配置
    private KFMediaCodecInterface mDecoder; ///< 音频解码
    private FileOutputStream mStream = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kf_video5);

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
                mStream = new FileOutputStream(Environment.getExternalStorageDirectory().getPath() + "/test.pcm");
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
                ///< 创建解封装器与解码器。
                if (mDemuxer == null) {
                    mDemuxer = new KFMP4Demuxer(mDemuxerConfig, mDemuxerListener);
                    mDecoder = new KFByteBufferCodec();
                    mDecoder.setup(false, mDemuxer.audioMediaFormat(), mDecoderListener, null);

                    MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                    ByteBuffer nextBuffer = mDemuxer.readAudioSampleData(bufferInfo);
                    ///< 循环读取音频帧进入解码器。
                    while (nextBuffer != null) {
                        mDecoder.processFrame(new KFBufferFrame(nextBuffer, bufferInfo));
                        nextBuffer = mDemuxer.readAudioSampleData(bufferInfo);
                    }
                    mDecoder.flush();
                    Log.i("KFDemuxer", "complete");
                }
            }
        });
        addContentView(startButton, startParams);
    }

    private KFDemuxerListener mDemuxerListener = new KFDemuxerListener() {
        @Override
        ///< 解封装出错回调。
        public void demuxerOnError(int error, String errorMsg) {
            Log.i("KFDemuxer", "error" + error + "msg" + errorMsg);
        }
    };

    private KFMediaCodecListener mDecoderListener = new KFMediaCodecListener() {
        @Override
        ///< 解码出错回调。
        public void onError(int error, String errorMsg) {

        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        ///< 解码数据回调，存储本地。
        public void dataOnAvailable(KFFrame frame) {
            KFBufferFrame bufferFrame = (KFBufferFrame) frame;
            try {
                byte[] dst = new byte[bufferFrame.bufferInfo.size];
                bufferFrame.buffer.get(dst);
                mStream.write(dst);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };
}
