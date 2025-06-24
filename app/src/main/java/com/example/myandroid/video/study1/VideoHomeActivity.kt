package com.example.myandroid.video.study1

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import com.example.myandroid.video.study1.video7.VideoActivity10
import com.example.myandroid.R
import com.example.myandroid.video.study1.video1.VideoActivity1
import com.example.myandroid.video.study1.video1.VideoActivity2
import com.example.myandroid.video.study1.video1.VideoActivity3
import com.example.myandroid.video.study1.video2.VideoActivity4
import com.example.myandroid.video.study1.video4.VideoActivity6
import com.example.myandroid.video.study1.video4.VideoActivity7
import com.example.myandroid.video.study1.video5.VideoActivity8
import com.example.myandroid.video.study1.video7.VideoActivity9

class VideoHomeActivity : AppCompatActivity(), View.OnClickListener {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_video)
        val button1: Button = findViewById(R.id.btn1)
        button1.setOnClickListener(this)
        val button2: Button = findViewById(R.id.btn2)
        button2.setOnClickListener(this)
        val button3: Button = findViewById(R.id.btn3)
        button3.setOnClickListener(this)
        val button4: Button = findViewById(R.id.btn4)
        button4.setOnClickListener(this)
        val button6: Button = findViewById(R.id.btn6)
        button6.setOnClickListener(this)
        val button7: Button = findViewById(R.id.btn7)
        button7.setOnClickListener(this)
        val button8: Button = findViewById(R.id.btn8)
        button8.setOnClickListener(this)
        val button9: Button = findViewById(R.id.btn9)
        button9.setOnClickListener(this)
        val button10: Button = findViewById(R.id.btn10)
        button10.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btn1 -> startActivity(getIntent(VideoActivity1::class.java))
            R.id.btn2 -> startActivity(getIntent(VideoActivity2::class.java))
            R.id.btn3 -> startActivity(getIntent(VideoActivity3::class.java))
            R.id.btn4 -> startActivity(getIntent(VideoActivity4::class.java))
            R.id.btn6 -> startActivity(getIntent(VideoActivity6::class.java))
            R.id.btn7 -> startActivity(getIntent(VideoActivity7::class.java))
            R.id.btn8 -> startActivity(getIntent(VideoActivity8::class.java))
            R.id.btn9 -> startActivity(getIntent(VideoActivity9::class.java))
            R.id.btn10 -> startActivity(getIntent(VideoActivity10::class.java))
            else -> {}
        }
    }

    private fun getIntent(clazz: Class<*>): Intent {
        return Intent(this, clazz)
    }
}

