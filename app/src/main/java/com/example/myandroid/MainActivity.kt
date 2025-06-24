package com.example.myandroid

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.os.Environment
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.example.myandroid.databinding.ActivityMainBinding
import com.example.myandroid.util.singleClickListener
import com.example.myandroid.video.keyframe.KFVideoHomeActivity
import com.example.myandroid.video.keyframeopengl.KFOpenGLHomeActivity
import com.example.myandroid.video.study1.VideoHomeActivity
import com.example.myandroid.widget.CustomViewActivity
import java.io.File
import java.io.FileOutputStream


/**
 * @author pxp
 * @description
 * 自定义View
 * KFVideo
 * KFOpenGL
 */
class MainActivity : BaseActivity<ActivityMainBinding>() {

    lateinit var  alterBitmap:Bitmap

    override fun initView() {
        binding.tvOpengl.singleClickListener {
            startActivity(Intent(this, KFOpenGLHomeActivity::class.java))
        }
        binding.tvVideo1.singleClickListener {
            startActivity(Intent(this, VideoHomeActivity::class.java))
        }
        binding.tvVideo2.singleClickListener {
            startActivity(Intent(this, KFVideoHomeActivity::class.java))
        }
        binding.tvView.singleClickListener {
            startActivity(Intent(this, CustomViewActivity::class.java))
        }
        binding.tvTest.singleClickListener {
            test()
        }
    }

    private fun test() {
//        val bitmap = BitmapFactory.decodeResource(resources, R.mipmap.mm01)
//
//        val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
//        val canvas = Canvas(mutableBitmap)
//
//        val paint = Paint()
//        paint.setColor(Color.WHITE)
//        paint.setTextSize(80f)
//        paint.setAntiAlias(true)
//
//        val x = binding.ivTest.width / 2f - (paint.measureText("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa") / 2)
//        val y = binding.ivTest.height / 2f
//
//        canvas.drawText("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", x, y, paint)
//
//        binding.ivTest.setImageBitmap(mutableBitmap)

        //改变图片大小
        //改变图片大小
        val opts = BitmapFactory.Options()
        opts.inSampleSize = 1
        // 获取要操作的原图
        // 获取要操作的原图
        val srcBitmap = BitmapFactory.decodeResource(resources, R.drawable.mm01)
        // 创建一个副本，相当于和一个原图一样的白纸
        // 创建一个副本，相当于和一个原图一样的白纸
        alterBitmap = Bitmap.createBitmap(srcBitmap.width, srcBitmap.height, srcBitmap.config)
        // 创建画笔
        // 创建画笔
        val paint = Paint()
        // 创建画布 把白纸铺到画布
        // 创建画布 把白纸铺到画布
        val canvas = Canvas(alterBitmap)
        // 开始作画
        // 开始作画
        canvas.drawBitmap(srcBitmap, Matrix(), paint)

        binding.ivTest.setImageBitmap(alterBitmap)
        // 给iv设置一个触摸事件
        // 给iv设置一个触摸事件
        binding.ivTest.setOnTouchListener(View.OnTouchListener { v, event -> // 具体判断一下触摸事件
            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    var i = -20
                    while (i < 20) {
                        var j = -20
                        while (j < 20) {
                            if (Math.sqrt((i * i + j * j).toDouble()) < 20) {
                                val x = event.x.toInt()
                                val y = event.y.toInt()
                                if (x + i < 0 || x + i >= alterBitmap.width) {
                                    ++j
                                    // 0到width-1的闭区间
                                    continue
                                }
                                if (y + j < 0 || y + j >= alterBitmap.height) {
                                    ++j
                                    continue
                                }
                                Log.d("123456", "getX: $x getY:$y")
                                alterBitmap.setPixel(x + i, y + j, Color.TRANSPARENT)
                            }
                            ++j
                        }
                        ++i
                    }
                    binding.ivTest.setImageBitmap(alterBitmap)
                }

                else -> {}
            }
            true
        })
    }

    override fun initData() {

    }
}