package com.example.myandroid.video.keyframe.video1;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myandroid.R;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 音频采集
 */
public class KFVideoActivity1 extends AppCompatActivity {

    private FileOutputStream mStream = null;
    private KFAudioCapture mAudioCapture = null;
    private KFAudioCaptureConfig mAudioCaptureConfig = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kf_video1);

        Button btn1 = findViewById(R.id.btn1);
        Button btn2 = findViewById(R.id.btn2);
        btn1.setOnClickListener((v)->{
            mAudioCaptureConfig = new KFAudioCaptureConfig();
            mAudioCapture = new KFAudioCapture(mAudioCaptureConfig, mAudioCaptureListener);
            mAudioCapture.startRunning();

            if (mStream == null) {
                try {
                    mStream = new FileOutputStream(Environment.getExternalStorageDirectory().getPath() + "/test.pcm");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
        btn2.setOnClickListener((v)->{
            mAudioCapture.stopRunning();
        });
    }

    ///< 音频采集回调。
    private KFAudioCaptureListener mAudioCaptureListener = new KFAudioCaptureListener() {
        @Override
        public void onError(int error, String errorMsg) {
            Log.e("KFAudioCapture", "errorCode" + error + "msg" + errorMsg);
        }

        @Override
        public void onFrameAvailable(KFFrame frame) {
            ///< 获取到音频 Buffer 数据存储到本地 PCM。
            try {
                ByteBuffer pcmData = ((KFBufferFrame) frame).buffer;
                byte[] ppsBytes = new byte[pcmData.capacity()];
                pcmData.get(ppsBytes);
                mStream.write(ppsBytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };
}
