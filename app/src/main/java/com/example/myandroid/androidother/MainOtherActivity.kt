package com.example.myandroid.androidother

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.myandroid.BaseActivity
import com.example.myandroid.R
import com.example.myandroid.databinding.ActivityMainOtherBinding
import com.example.myandroid.util.singleClickListener
import com.example.myandroid.widget.CustomViewActivity

/**
 * @author pxp
 * @description android的其他知识点
 * 1.通知
 * 2.打开第三方选择文件
 *
 */
class MainOtherActivity : BaseActivity<ActivityMainOtherBinding>() {

    companion object {
        //建议单独写在一个常量类中，防止别人在不了解的情况下随意修改
        private const val CHANNEL_ID_DAILY = "channel_id_daily"
        private const val REQUEST_CODE_DAILY = 0x1000
        private const val NOTIFY_ID_DAILY = 0x2000
    }

    @SuppressLint("RemoteViewLayout")
    override fun initView() {
        //通知
        binding.tvNotification.singleClickListener {
            notification()
        }

        //打开第三方选择文件
        binding.tvOpen.singleClickListener {
            openSelectFile()
        }
    }

    override fun initData() {

    }

    private fun notification() {
        //https://juejin.cn/post/7202127250643255352
        val remoteViews = RemoteViews(this.packageName, R.layout.layout_cus_notify)
        val funIntent = Intent(this, CustomViewActivity::class.java)//演示代码：点击后跳转到ArticleActivity
        funIntent.putExtra("key_article_nun", "第一篇")
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(
                this, REQUEST_CODE_DAILY, funIntent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    PendingIntent.FLAG_MUTABLE
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }
            )
        remoteViews.setTextViewText(R.id.tv_content, "欢迎您查看的我的文章")
        remoteViews.setOnClickPendingIntent(R.id.btn, pendingIntent) //为按钮设置点击事件

        //通过传入channelId将这条通知和上面定义的通知渠道绑定
        val notification = NotificationCompat.Builder(this, CHANNEL_ID_DAILY)
            .setSmallIcon(R.mipmap.ic_launcher)//必须设置，否则会奔溃
            .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.mipmap.ic_launcher))
            .setCustomContentView(remoteViews)    //折叠后通知显示的布局
            .setCustomHeadsUpContentView(remoteViews)//横幅样式显示的布局
            .setCustomBigContentView(remoteViews) //展开后通知显示的布局
            .setContent(remoteViews)              //兼容低版本
            .setColor(ContextCompat.getColor(this, R.color.black))//小图标的颜色
            .setAutoCancel(true)                   // 允许点击后清除通知
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setDefaults(NotificationCompat.DEFAULT_ALL)  // 默认配置,包括通知的提示音,震动效果等
            .setContentIntent(pendingIntent) //一定要设置，点击整个remoteView就可跳转
        val notificationManager= getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFY_ID_DAILY, notification.build())
    }

    private fun openSelectFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "*/*" //无类型限制
//        有类型限制是这样的:
//        intent.setType(“image/*”);//选择图片
//        intent.setType(“audio/*”); //选择音频
//        intent.setType(“video/*”); //选择视频 （mp4 3gp 是android支持的视频格式）
//        intent.setType(“video/*;image/*”);//同时选择视频和图片
        //        有类型限制是这样的:
//        intent.setType(“image/*”);//选择图片
//        intent.setType(“audio/*”); //选择音频
//        intent.setType(“video/*”); //选择视频 （mp4 3gp 是android支持的视频格式）
//        intent.setType(“video/*;image/*”);//同时选择视频和图片
//            intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(intent, 1)
    }

    @SuppressLint("WrongConstant")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("123456", "选择文件返回：$data")
        if (requestCode == 1) {
            //takePersistableUriPermission保留通过SAF获取的权限，这样结束进程再启动，仍有该文件的访问权限，在断点续传等场景可能用到
            val takeFlags: Int = data!!.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION
            val contentResolver = applicationContext.contentResolver
            /**
             *  获取永久权限
             *
             *  第三方返回代码
             *  val intent = Intent()
             *  //通过扩展名找到mimeType
             *  val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(item.path)
             *  val fileUri = CustomFileProvider.getUriForFile(
             *      this,
             *      "${BuildConfig.APPLICATION_ID}.fileProvider",
             *      File(path)
             *  )
             *  grantUriPermission(callingPackage, fileUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
             *  intent.addFlags(
             *      Intent.FLAG_GRANT_READ_URI_PERMISSION
             *              or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
             *              or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
             *              or Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
             *  )
             *  intent.data = fileUri
             *  intent.setDataAndType(fileUri, mimeType)
             *  setResult(Activity.RESULT_OK, intent)
             *  finish()
             */
            contentResolver.takePersistableUriPermission(data!!.data!!, takeFlags)
        }
    }
}