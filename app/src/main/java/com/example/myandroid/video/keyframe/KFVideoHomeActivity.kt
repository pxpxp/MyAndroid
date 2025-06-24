package com.example.myandroid.video.keyframe

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
import com.example.myandroid.video.keyframe.video3.KFVideoActivity3
import com.example.myandroid.video.keyframe.video4.KFVideoActivity4
import com.example.myandroid.video.keyframe.video5.KFVideoActivity5

class KFVideoHomeActivity : AppCompatActivity(), View.OnClickListener {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kf_video_home)
        val button1: Button = findViewById(R.id.btn1)
        val button2: Button = findViewById(R.id.btn2)
        val button3: Button = findViewById(R.id.btn3)
        val button4: Button = findViewById(R.id.btn4)
        val button5: Button = findViewById(R.id.btn5)
        button1.setOnClickListener(this)
        button2.setOnClickListener(this)
        button3.setOnClickListener(this)
        button4.setOnClickListener(this)
        button5.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btn1 -> startActivity(getIntent(KFVideoActivity1::class.java))
            R.id.btn2 -> startActivity(getIntent(KFVideoActivity2::class.java))
            R.id.btn3 -> startActivity(getIntent(KFVideoActivity3::class.java))
            R.id.btn5 -> startActivity(getIntent(KFVideoActivity5::class.java))
            else -> {}
        }
    }

    private fun getIntent(clazz: Class<*>): Intent {
        return Intent(this, clazz)
    }
}

