package com.example.myandroid.widget

import android.graphics.BitmapFactory
import android.util.Log
import com.example.myandroid.BaseActivity
import com.example.myandroid.R
import com.example.myandroid.databinding.ActivityCustomViewTestBinding
import com.example.myandroid.util.singleClickListener
import java.util.Random

/**
 * @author pxp
 * @description 用于测试某个自定义View
 */
class CustomViewTestActivity : BaseActivity<ActivityCustomViewTestBinding>() {
    override fun initView() {
        binding.v.post {
//            binding.v.setImageRatio(150, 300)
            binding.v.setImageBitmap(
                BitmapFactory.decodeResource(
                    getResources(),
                    R.drawable.mm03
                )
            )
        }
        binding.btn.setOnClickListener {
            binding.v.setExpandImageBitmap(BitmapFactory.decodeResource(
                getResources(),
                R.drawable.mm02
            ))
        }
        binding.btn2.setOnClickListener {
            val random = Random()
            binding.v.setExpandImageVisible(random.nextBoolean())
            Log.v("123456", binding.v.getExpandRect().toString())
        }
    }

    override fun initData() {

    }
}