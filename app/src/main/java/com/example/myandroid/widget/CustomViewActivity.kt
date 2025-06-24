package com.example.myandroid.widget

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.text.Html
import com.example.myandroid.BaseActivity
import com.example.myandroid.R
import com.example.myandroid.databinding.ActivityCustomViewBinding
import com.example.myandroid.util.singleClickListener
import com.example.myandroid.widget.htmltext.HtmlTagHandler
import com.example.myandroid.widget.htmltext.tags.SectionTag
import com.example.myandroid.widget.timekids.LedDisplayView.IDrawer


/**
 * @author pxp
 * @description 自定义View合集
 */
class CustomViewActivity : BaseActivity<ActivityCustomViewBinding>() {

    override fun initView() {
        //单独测试某个自定义View的Activity
        binding.btn.singleClickListener {
            startActivity(Intent(this, CustomViewTestActivity::class.java))
        }
        //实现LED展示效果
        ledDisplayView()
        //实现html css富文本解析引擎
        htmlText()
        //图片描边效果
        binding.viewHighLight.singleClickListener {
            binding.viewHighLight.shake()
        }
    }

    override fun initData() {
    }

    private fun ledDisplayView() {
        val bitmapDrawable1 = BitmapDrawable(resources, decodeBitmap(R.drawable.kt01))
        val bitmapDrawable2 = BitmapDrawable(resources, decodeBitmap(R.drawable.mm02))
        binding.ledDisplayView.addDrawer(object : IDrawer {
            var matrix: Matrix = Matrix()
            override fun draw(canvas: Canvas, width: Int, height: Int, paint: Paint?) {
                canvas.translate(width / 2f, height / 2f)
                matrix.preTranslate(-width / 2f, -height / 4f)
                val bitmap1 = bitmapDrawable1.bitmap
                canvas.drawBitmap(bitmap1, matrix, paint)
                matrix.postTranslate(width / 2f, height / 4f)
                val bitmap2 = bitmapDrawable2.bitmap
                canvas.drawBitmap(bitmap2, matrix, paint)
            }
        })
        binding.ledDisplayView.addDrawer(IDrawer { canvas, width, height, paint ->
            paint.color = Color.CYAN
            val textSize = paint.textSize
            paint.textSize = binding.ledDisplayView.sp2px(50f)
            canvas.drawText("你好，L E D", 100f, 200f, paint)
            canvas.drawText("85%", 100f, 350f, paint)
            paint.color = Color.YELLOW
            canvas.drawCircle(width * 3f / 4, height / 4f, 100f, paint)
            paint.textSize = textSize
        })
    }

    private fun decodeBitmap(resId: Int): Bitmap? {
        val options = BitmapFactory.Options()
        options.inMutable = true
        return BitmapFactory.decodeResource(resources, resId, options)
    }

    private fun htmlText() {
        val htmlTagHandler = HtmlTagHandler()
        htmlTagHandler.registerTag("section", SectionTag(this))
        htmlTagHandler.registerTag("custom", SectionTag(this))
        htmlTagHandler.registerTag("span", SectionTag(this))

        val source =
            "<html>今天<section style='color:#FFE31335;font-size:16sp;background-color:white;'>星期三</section>，<section style='color:#fff;font-size:14sp;background-color:red;'>但是我还要加班</section><html>"
        val spanned = Html.fromHtml(source, htmlTagHandler, htmlTagHandler)
        binding.tvHtml.text = spanned

//        val source =
//            "<html>今天<span style='color:#FFE31335;font-size:16sp;background-color:white;'>星期三</span>，<custom style='color:#fff;font-size:14sp;background-color:red;'>但是我还要加班</custom><html>"
//        val spanned = Html.fromHtml(source, htmlTagHandler, htmlTagHandler)
//        binding.tvHtml.text = spanned
    }
}