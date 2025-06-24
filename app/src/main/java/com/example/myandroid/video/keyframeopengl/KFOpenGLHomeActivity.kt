package com.example.myandroid.video.keyframeopengl

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import com.example.myandroid.R
import com.example.myandroid.video.keyframe.video1.KFVideoActivity1
import com.example.myandroid.video.keyframe.video2.KFVideoActivity2
import com.example.myandroid.video.keyframeopengl.opengl1.KFOpenGLActivity1
import com.example.myandroid.video.study1.video1.VideoActivity1

class KFOpenGLHomeActivity : AppCompatActivity(), View.OnClickListener {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kf_opengl_home)
        val button1: Button = findViewById(R.id.btn1)
        button1.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btn1 -> startActivity(getIntent(KFOpenGLActivity1::class.java))
            else -> {}
        }
    }

    private fun getIntent(clazz: Class<*>): Intent {
        return Intent(this, clazz)
    }
}

