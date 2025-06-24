package com.example.myandroid.video.study1.video7

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.hardware.Camera
import android.media.MediaCodecList
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myandroid.R
import java.io.IOException

/**
 * 仅收集视频帧，并转成H264格式
 */
class VideoActivity9 : AppCompatActivity(), SurfaceHolder.Callback, Camera.PreviewCallback {

    var camera: Camera? = null
    var surfaceView: SurfaceView? = null
    var surfaceHolder: SurfaceHolder? = null

    var width = 1280
    var height = 720
    var framerate = 30
    var encoder: H264Encoder? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video9)

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "申请权限", Toast.LENGTH_SHORT).show()
            // 申请 相机 麦克风权限
            ActivityCompat.requestPermissions(
                this, arrayOf<String>(
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ), 100
            )
        }

        surfaceView = findViewById<View>(R.id.surfaceView) as SurfaceView
        surfaceHolder = surfaceView!!.holder
        surfaceHolder!!.addCallback(this)

        if (supportH264Codec()) {
            Log.e("123456", "support H264 hard codec")
        } else {
            Log.e("123456", "not support H264 hard codec")
        }
    }


    private fun supportH264Codec(): Boolean {
        // 遍历支持的编码格式信息
        if (Build.VERSION.SDK_INT >= 18) {
            for (j in MediaCodecList.getCodecCount() - 1 downTo 0) {
                val codecInfo = MediaCodecList.getCodecInfoAt(j)
                val types = codecInfo.supportedTypes
                for (i in types.indices) {
                    if (types[i].equals("video/avc", ignoreCase = true)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        Log.w("MainActivity", "enter surfaceCreated method")
        // 目前设定的是，当surface创建后，就打开摄像头开始预览
        camera = Camera.open()
        camera!!.setDisplayOrientation(90)
        val parameters = camera!!.getParameters()
        parameters.previewFormat = ImageFormat.NV21
        parameters.setPreviewSize(1280, 720)
        try {
            camera!!.setPreviewDisplay(surfaceHolder)
            camera!!.setPreviewCallback(this)
            camera!!.startPreview()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        encoder = H264Encoder(width, height, framerate)
        encoder!!.startEncoder()
    }

    override fun surfaceChanged(holder: SurfaceHolder, i: Int, i1: Int, i2: Int) {
        Log.w("123546", "enter surfaceChanged method")
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        Log.w("123456", "enter surfaceDestroyed method")

        // 停止预览并释放资源
        if (camera != null) {
            camera!!.setPreviewCallback(null)
            camera!!.stopPreview()
            camera = null
        }
        if (encoder != null) {
            encoder!!.stopEncoder()
        }
    }

    override fun onPreviewFrame(bytes: ByteArray?, camera: Camera?) {
        if (encoder != null) {
            encoder!!.putData(bytes)
        }
    }
}