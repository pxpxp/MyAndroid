package com.example.myandroid.video.study1.video2

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.myandroid.R
import java.io.FileOutputStream


class VideoActivity4 : AppCompatActivity() {

    companion object {
        //pcm位数
        private val pcmEncodeBit = AudioFormat.ENCODING_PCM_16BIT

        //双声道
        private val channels = AudioFormat.CHANNEL_IN_STEREO

        //采样率
        private val sampleRate = 44100

        //数据来源
        private val audioSource = MediaRecorder.AudioSource.MIC
    }

    private var audioRecord: AudioRecord? = null // 声明 AudioRecord 对象
    private var recordBufSize = 0 // 声明recoordBufffer的大小字段
    private var isRecording = false
    var os: FileOutputStream? = null


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video4)

        createAudioRecord()

        findViewById<Button>(R.id.start).setOnClickListener {
            audioRecord?.startRecording()
            isRecording = true

            Thread(recordRunnable).start()

        }

        findViewById<Button>(R.id.end).setOnClickListener {
            if (null != audioRecord) {
                audioRecord?.stop()
                audioRecord?.release()
                audioRecord = null
//                recordingThread = null
            }
        }
    }

    private val recordRunnable = Runnable {
        os = FileOutputStream("")
        val data = ByteArray(4096)

        if (null != os) {
            while (isRecording) {
                var read = audioRecord?.read(data, 0, recordBufSize);
                // 如果读取音频数据没有出现错误，就将数据写入到文件
                if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                    os!!.write(data)
                }
            }

            os!!.close()
        }
    }


    //创建录音对象
    fun createAudioRecord() {
//        recordBufSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, EncodingBitRate);  //audioRecord能接受的最小的buffer大小
        recordBufSize = AudioRecord.getMinBufferSize(
            sampleRate,
            channels,
            pcmEncodeBit
        )  //audioRecord能接受的最小的buffer大小
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        audioRecord = AudioRecord(audioSource, sampleRate, channels, pcmEncodeBit, recordBufSize)
    }
}