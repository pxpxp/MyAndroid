package com.example.myandroid.video.study1.video7

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.hardware.Camera
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myandroid.R
import java.io.IOException


/**
 * 音视频混合界面
 */
class VideoActivity10 : AppCompatActivity(), SurfaceHolder.Callback, Camera.PreviewCallback {

    var surfaceView: SurfaceView? = null
    var startStopButton: Button? = null

    var camera: Camera? = null
    var surfaceHolder: SurfaceHolder? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video10)

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
                this, arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ), 100
            )
        }

        surfaceView = findViewById<SurfaceView>(R.id.surfaceView)
        startStopButton = findViewById<Button>(R.id.btn)

        startStopButton!!.setOnClickListener(View.OnClickListener { view ->
            if (view.tag != null && view.tag.toString().equals("stop")) {
                view.tag = "start"
                (view as TextView).text = "开始"
                MediaMuxerThread.stopMuxer()
                stopCamera()
                finish()
            } else {
                startCamera()
                view.tag = "stop"
                (view as TextView).text = "停止"
                MediaMuxerThread.startMuxer()
            }
        })

        surfaceHolder = surfaceView!!.holder
        surfaceHolder!!.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        Log.w("MainActivity", "enter surfaceCreated method")
        this.surfaceHolder = surfaceHolder
    }

    override fun surfaceChanged(holder: SurfaceHolder, i: Int, i1: Int, i2: Int) {
        Log.w("MainActivity", "enter surfaceChanged method")
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        Log.w("MainActivity", "enter surfaceDestroyed method")
        MediaMuxerThread.stopMuxer()
        stopCamera()
    }

    override fun onPreviewFrame(bytes: ByteArray?, camera: Camera?) {
        MediaMuxerThread.addVideoFrameData(bytes)
    }

    //----------------------- 摄像头操作相关 --------------------------------------
    /**
     * 打开摄像头
     */
    private fun startCamera() {
        camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT)
        camera!!.setDisplayOrientation(90)
        val parameters = camera!!.getParameters()
        parameters.previewFormat = ImageFormat.NV21

        // 这个宽高的设置必须和后面编解码的设置一样，否则不能正常处理
        parameters.setPreviewSize(1920, 1080)
        try {
            camera!!.setParameters(parameters)
            camera!!.setPreviewDisplay(surfaceHolder)
            camera!!.setPreviewCallback(this)
            camera!!.startPreview()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * 关闭摄像头
     */
    private fun stopCamera() {
        // 停止预览并释放资源
        if (camera != null) {
            camera!!.setPreviewCallback(null)
            camera!!.stopPreview()
            camera = null
        }
    }
}