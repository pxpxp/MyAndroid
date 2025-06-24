package com.example.myandroid.video.keyframe.video6;

import static android.media.AudioTrack.STATE_INITIALIZED;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.RequiresApi;

/**
 * @author pxp
 * @description
 * 音频渲染数据输入回调接口，系统的音频渲染单元每次会主动通过回调的方式要数据，
 * 我们这里封装的 KFAudioRender 则是用数据输入回调接口来从外部获取一组待渲染的音频数据送给系统的音频渲染单元。
 *
 * 1）创建音频渲染实例。
 * 在 _setupAudioTrack 方法中实现，根据采样率、声道、单次输入数据大小 等几个参数生成。
 * 2）处理音频渲染实例的数据回调，并在回调中通过 KFAudioRender 的对外数据输入回调接口向更外层要待渲染的数据。
 * 通过 audioPCMData 回调接口向更外层要数据。
 * 3）实现开始渲染和停止渲染逻辑。
 * 分别在 play 和 stop 方法中实现。注意，这里是开始和停止操作都是放在串行队列中通过 mHandler.post 异步处理的，这里主要是为了防止主线程卡顿。
 * 开启播放后会循环向外层获取 PCM 数据，通过 write 方法写入 mAudioTrack。
 * 4）清理音频渲染实例。
 * 在 release 方法中实现。
 */
public class KFAudioRender {
    private static final String TAG = "KFAudioRender";
    public static final int KFAudioRenderErrorCreate = -2700;
    public static final int KFAudioRenderErrorPlay = -2701;
    public static final int KFAudioRenderErrorStop = -2702;
    public static final int KFAudioRenderErrorPause = -2703;

    private static final int KFAudioRenderMaxCacheSize = 500*1024; ///< 音频 PCM 缓存最大值。
    private KFAudioRenderListener mListener = null; ///< 回调。
    private Handler mMainHandler = new Handler(Looper.getMainLooper()); ///< 主线程。
    private HandlerThread mThread = null; ///< 音频管控线程。
    private Handler mHandler = null;
    private HandlerThread mRenderThread = null; ///< 音频渲染线程。
    private Handler mRenderHandler = null;
    private AudioTrack mAudioTrack = null; ///< 音频播放实例。
    private int mMinBufferSize = 0;
    private byte mCache[] = new byte[KFAudioRenderMaxCacheSize]; ///< 音频 PCM 缓存。
    private int mCacheSize = 0;

    public KFAudioRender(KFAudioRenderListener listener, int sampleRate, int channel) {
        mListener = listener;
        ///< 创建音频管控线程。
        mThread = new HandlerThread("KFAudioRenderThread");
        mThread.start();
        mHandler = new Handler((mThread.getLooper()));
        ///< 创建音频渲染线程。
        mRenderThread = new HandlerThread("KFAudioGetDataThread");
        mRenderThread.start();
        mRenderHandler = new Handler((mRenderThread.getLooper()));

        mHandler.post(()->{
            ///< 初始化音频播放实例。
            _setupAudioTrack(sampleRate,channel);
        });
    }

    public void release() {
        mHandler.post(()-> {
            ///< 停止与释放音频播放实例。
            if (mAudioTrack != null) {
                try {
                    mAudioTrack.stop();
                    mAudioTrack.release();
                } catch (Exception e) {
                    Log.e(TAG, "release: " + e.toString());
                }
                mAudioTrack = null;
            }

            mThread.quit();
            mRenderThread.quit();
        });
    }

    public void play() {
        mHandler.post(()-> {
            ///< 音频实例播放。
            try {
                mAudioTrack.play();
            } catch (Exception e){
                _callBackError(KFAudioRenderErrorPlay,e.getMessage());
                return;
            }

            mRenderHandler.post(()->{
                ///< 循环写入 PCM 数据，写入系统缓冲区，当读取到最大值或者状态机不等于 STATE_INITIALIZED 则退出循环。
                while (mAudioTrack.getState() == STATE_INITIALIZED){
                    if (mListener != null && mCacheSize < KFAudioRenderMaxCacheSize) {
                        byte[] bytes = mListener.audioPCMData(mMinBufferSize);
                        if (bytes != null && bytes.length > 0) {
                            System.arraycopy(bytes,0,mCache,mCacheSize,bytes.length);
                            mCacheSize += bytes.length;
                            if (mCacheSize >= mMinBufferSize) {
                                int writeSize = mAudioTrack.write(mCache,0,mMinBufferSize);
                                if (writeSize > 0) {
                                    mCacheSize -= writeSize;
                                    System.arraycopy(mCache,writeSize,mCache,0,mCacheSize);
                                }
                            }
                        } else {
                            break;
                        }
                    }
                }
            });
        });
    }

    public void stop() {
        ///< 停止音频播放。
        mHandler.post(()-> {
            try {
                mAudioTrack.stop();
            } catch (Exception e){
                _callBackError(KFAudioRenderErrorStop,e.getMessage());
            }
            mCacheSize = 0;
        });
    }

    public void pause() {
        ///< 暂停音频播放。
        mHandler.post(()-> {
            try {
                mAudioTrack.pause();
            } catch (Exception e){
                _callBackError(KFAudioRenderErrorPause,e.getMessage());
            }
        });
    }

    public void  _setupAudioTrack(int sampleRate, int channel) {
        ///< 根据采样率、声道获取每次音频播放塞入数据大小，根据采样率、声道、数据大小创建音频播放实例。
        if (mAudioTrack == null) {
            try {
                mMinBufferSize = AudioRecord.getMinBufferSize(sampleRate, channel, AudioFormat.ENCODING_PCM_16BIT);
                mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,sampleRate,channel == 2 ? AudioFormat.CHANNEL_OUT_STEREO : AudioFormat.CHANNEL_OUT_MONO,AudioFormat.ENCODING_PCM_16BIT,mMinBufferSize,AudioTrack.MODE_STREAM);
            } catch (Exception e){
                _callBackError(KFAudioRenderErrorCreate,e.getMessage());
            }
        }
    }

    private void _callBackError(int error, String errorMsg) {
        if (mListener != null) {
            mMainHandler.post(()->{
                mListener.onError(error,TAG + errorMsg);
            });
        }
    }
}
