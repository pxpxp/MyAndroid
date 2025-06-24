package com.example.myandroid.video.study1.video4

import android.annotation.SuppressLint
import android.hardware.Camera
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.appcompat.app.AppCompatActivity
import com.example.myandroid.R
import java.io.IOException


class VideoActivity6 : AppCompatActivity(), SurfaceHolder.Callback {

    var surfaceView: SurfaceView? = null
    var camera: Camera? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video6)


        surfaceView = findViewById(R.id.surfaceView);
        surfaceView?.getHolder()?.addCallback(this);

        // 打开摄像头并将展示方向旋转90度
        camera = Camera.open();
        camera?.setDisplayOrientation(90);
    }

    //------ Surface 预览 -------
    override fun surfaceCreated(holder: SurfaceHolder) {
        try {
            camera!!.setPreviewDisplay(holder)
            camera!!.startPreview()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        camera!!.release()
    }
}