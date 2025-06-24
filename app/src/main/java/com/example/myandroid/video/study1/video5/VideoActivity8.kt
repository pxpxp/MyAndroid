package com.example.myandroid.video.study1.video5

import android.annotation.SuppressLint
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.myandroid.R
import java.io.IOException
import java.nio.ByteBuffer


class VideoActivity8 : AppCompatActivity() {

    private val SDCARD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
    private val mMediaExtractor = MediaExtractor()
    private var mMediaMuxer: MediaMuxer? = null


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video8)

        Thread {
            try {
                process()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    @Throws(IOException::class)
    private fun process(): Boolean {
        Log.e("123456", "Environment.getExternalStorageState():" + SDCARD_PATH )
        //setDataSource(String path)：即可以设置本地文件又可以设置网络文件
        mMediaExtractor.setDataSource(SDCARD_PATH + "/123.mp4")
        var mVideoTrackIndex = -1
        var framerate = 0
        //getTrackCount()得到源文件通道数
        for (i in 0 until mMediaExtractor.getTrackCount()) {
            //getTrackFormat(int index)：获取指定（index）的通道格式
            val format: MediaFormat = mMediaExtractor.getTrackFormat(i)
            //MediaFormat.KEY_MIME：MediaFormat的mime类型的键
            val mime = format.getString(MediaFormat.KEY_MIME)
            if (!mime!!.startsWith("video/")) {
                continue
            }
            //MediaFormat.KEY_FRAME_RATE：描述视频格式的帧速率(以帧/秒为单位)的键。
            framerate = format.getInteger(MediaFormat.KEY_FRAME_RATE)
            mMediaExtractor.selectTrack(i)
            //MediaMuxer(String path, int format)：path:输出文件的名称  format:输出文件的格式；当前只支持MP4格式；
            mMediaMuxer =
                MediaMuxer(SDCARD_PATH + "/ouput.mp4", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            //addTrack(MediaFormat format)：添加通道；我们更多的是使用MediaCodec.getOutpurForma()或Extractor.getTrackFormat(int index)来获取MediaFormat;也可以自己创建；
            mVideoTrackIndex = mMediaMuxer!!.addTrack(format)
            //start()：开始合成文件
            mMediaMuxer!!.start()
        }
        if (mMediaMuxer == null) {
            return false
        }
        val info = MediaCodec.BufferInfo()
        info.presentationTimeUs = 0
        val buffer = ByteBuffer.allocate(500 * 1024)
        var sampleSize = 0
        //readSampleData(ByteBuffer byteBuf, int offset)：把指定通道中的数据按偏移量读取到ByteBuffer中；
        while (mMediaExtractor.readSampleData(buffer, 0).also { sampleSize = it } > 0) {
            info.offset = 0
            info.size = sampleSize
            info.flags = MediaCodec.BUFFER_FLAG_SYNC_FRAME
            info.presentationTimeUs += (1000 * 1000 / framerate).toLong()
            //writeSampleData(int trackIndex, ByteBuffer byteBuf, MediaCodec.BufferInfo bufferInfo)：把ByteBuffer中的数据写入到在构造器设置的文件中；
            mMediaMuxer!!.writeSampleData(mVideoTrackIndex, buffer, info)
            //advance()：读取下一帧数据
            mMediaExtractor.advance()
        }
        //release(): 读取结束后释放资源
        mMediaExtractor.release()
        //stop()：停止合成文件
        mMediaMuxer!!.stop()
        //release()：释放资源
        mMediaMuxer!!.release()
        return true
    }
}