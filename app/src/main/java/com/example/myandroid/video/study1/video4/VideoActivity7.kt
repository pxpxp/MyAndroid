package com.example.myandroid.video.study1.video4

import android.annotation.SuppressLint
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.os.Bundle
import android.util.Log
import android.view.TextureView
import androidx.appcompat.app.AppCompatActivity
import com.example.myandroid.R
import java.io.IOException


class VideoActivity7 : AppCompatActivity(), TextureView.SurfaceTextureListener {

    var textureView: TextureView? = null
    var camera: Camera? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video7)


        textureView = findViewById(R.id.textureView);
        textureView?.setSurfaceTextureListener(this);// 打开摄像头并将展示方向旋转90度

        // 打开摄像头并将展示方向旋转90度
        camera = Camera.open();
        camera?.setDisplayOrientation(90);

        val parameters = camera!!.getParameters()
        parameters.previewFormat = ImageFormat.NV21
        camera!!.setParameters(parameters)

        camera!!.setPreviewCallback { bytes, camera ->
            Log.e("123456", "${bytes.size}")
        }
    }

    //------ Texture 预览 -------
    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        try {
            camera!!.setPreviewTexture(surface)
            camera!!.startPreview()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {

    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        camera?.release();
        return false;
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {

    }
}