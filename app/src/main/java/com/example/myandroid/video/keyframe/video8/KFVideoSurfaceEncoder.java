//package com.example.myandroid.video.keyframe.video8;
//
//import android.media.MediaCodec;
//import android.media.MediaFormat;
//import android.opengl.EGLContext;
//import android.opengl.GLES20;
//import android.os.Build;
//import android.os.Handler;
//import android.os.HandlerThread;
//import android.os.Looper;
//import android.util.Log;
//import android.view.Surface;
//
//import androidx.annotation.RequiresApi;
//
//import com.example.myandroid.video.keyframe.video1.KFBufferFrame;
//import com.example.myandroid.video.keyframe.video1.KFFrame;
//import com.example.myandroid.video.keyframe.video2.KFMediaCodecInterface;
//import com.example.myandroid.video.keyframe.video2.KFMediaCodecListener;
//import com.example.myandroid.video.keyframe.video7.KFGLContext;
//import com.example.myandroid.video.keyframe.video7.KFTextureFrame;
//
//import java.io.IOException;
//import java.nio.ByteBuffer;
//
//
//
///**
// * @author pxp
// * @description
// */
//public class KFVideoSurfaceEncoder implements KFMediaCodecInterface {
//    private static final String TAG = "KFVideoSurfaceEncoder";
//    private KFMediaCodecListener mListener = null; ///< 回调。
//    private KFGLContext mEGLContext = null; ///< GL 上下文。
//    private KFGLFilter mFilter = null; ///< 渲染到 Surface 特效。
//    private MediaCodec mEncoder = null; ///< 编码器。
//    private Surface mSurface = null; ///< 渲染 Surface 缓存。
//
//    private HandlerThread mEncoderThread = null; ///< 编码线程。
//    private Handler mEncoderHandler = null;
//    private Handler mMainHandler = new Handler(Looper.getMainLooper()); ///< 主线程。
//    private MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
//    private long mLastInputPts = 0;
//    private MediaFormat mOutputFormat = null; ///< 输出格式描述。
//    private MediaFormat mInputFormat = null; ///< 输入格式描述。
//
//    public KFVideoSurfaceEncoder() {
//
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    @Override
//    public void setup(boolean isEncoder,MediaFormat mediaFormat, KFMediaCodecListener listener, EGLContext eglShareContext) {
//        mInputFormat = mediaFormat;
//        mListener = listener;
//
//        mEncoderThread = new HandlerThread("KFSurfaceEncoderThread");
//        mEncoderThread.start();
//        mEncoderHandler = new Handler((mEncoderThread.getLooper()));
//
//        mEncoderHandler.post(()->{
//            if (mInputFormat == null) {
//                _callBackError(KFMediaCodecInterfaceErrorParams,"mInputFormat == null");
//                return;
//            }
//
//            ///< 初始化编码器。
//            boolean setupSuccess = _setupEnocder();
//            if (setupSuccess) {
//                mEGLContext = new KFGLContext(eglShareContext,mSurface);
//                mEGLContext.bind();
//                ///< 初始化特效，用于纹理渲染到编码器 Surface 上。
//                _setupFilter();
//                mEGLContext.unbind();
//            }
//        });
//    }
//
//    @Override
//    public MediaFormat getOutputMediaFormat() {
//        return mOutputFormat;
//    }
//
//    @Override
//    public MediaFormat getInputMediaFormat() {
//        return mInputFormat;
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    @Override
//    public void release() {
//        mEncoderHandler.post(()->{
//            ///< 释放编码器。
//            if (mEncoder != null) {
//                try {
//                    mEncoder.stop();
//                    mEncoder.release();
//                } catch (Exception e) {
//                    Log.e(TAG, "release: " + e.toString());
//                }
//                mEncoder = null;
//            }
//
//            ///< 释放 GL 特效上下文。
//            if (mEGLContext != null) {
//                mEGLContext.bind();
//                if (mFilter != null) {
//                    mFilter.release();
//                    mFilter = null;
//                }
//                mEGLContext.unbind();
//
//                mEGLContext.release();
//                mEGLContext = null;
//            }
//
//            ///< 释放 Surface 缓存。
//            if (mSurface != null) {
//                mSurface.release();
//                mSurface = null;
//            }
//
//            mEncoderThread.quit();
//        });
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    @Override
//    public int processFrame(KFFrame inputFrame) {
//        if (inputFrame == null || mEncoderHandler == null) {
//            return KFMediaCodeProcessParams;
//        }
//        KFTextureFrame frame = (KFTextureFrame)inputFrame;
//
//        mEncoderHandler.post(()-> {
//            if (mEncoder != null && mEGLContext != null) {
//                if (frame.isEnd) {
//                    ///< 最后一帧标记。
//                    mEncoder.signalEndOfInputStream();
//                } else {
//                    ///< 最近一帧时间戳。
//                    mLastInputPts = frame.usTime();
//                    mEGLContext.bind();
//                    ///< 渲染纹理到编码器 Surface 设置视口。
//                    GLES20.glViewport(0, 0, frame.textureSize.getWidth(), frame.textureSize.getHeight());
//                    mFilter.render(frame);
//                    ///< 设置时间戳。
//                    mEGLContext.setPresentationTime(frame.usTime() * 1000);
//                    mEGLContext.swapBuffers();
//                    mEGLContext.unbind();
//
//                    ///< 获取编码后的数据，尽量拿出最多的数据出来，回调给外层。
//                    long outputDts = -1;
//                    while (outputDts < mLastInputPts){
//                        int bufferIndex = 0;
//                        try {
//                            bufferIndex = mEncoder.dequeueOutputBuffer(mBufferInfo, 10 * 1000);
//                        } catch (Exception e) {
//                            Log.e(TAG, "Unexpected MediaCodec exception in dequeueOutputBufferIndex, " + e);
//                            _callBackError(KFMediaCodecInterfaceErrorDequeueOutputBuffer,e.getMessage());
//                            return;
//                        }
//
//                        if (bufferIndex >= 0) {
//                            ByteBuffer byteBuffer = mEncoder.getOutputBuffer(bufferIndex);
//                            if (byteBuffer != null) {
//                                outputDts = mBufferInfo.presentationTimeUs;
//                                if (mListener != null) {
//                                    KFBufferFrame encodeFrame = new KFBufferFrame();
//                                    encodeFrame.buffer = byteBuffer;
//                                    encodeFrame.bufferInfo = mBufferInfo;
//                                    mListener.dataOnAvailable(encodeFrame);
//                                }
//                            } else {
//                                break;
//                            }
//
//                            try {
//                                mEncoder.releaseOutputBuffer(bufferIndex, false);
//                            } catch (Exception e) {
//                                Log.e(TAG, e.toString());
//                                return;
//                            }
//                        } else {
//                            if (bufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
//                                mOutputFormat = mEncoder.getOutputFormat();
//                            }
//                            break;
//                        }
//                    }
//                }
//            }
//        });
//
//        return KFMediaCodeProcessSuccess;
//    }
//
//    @Override
//    public void flush() {
//        mEncoderHandler.post(()-> {
//            ///< 刷新缓冲区。
//            if (mEncoder != null) {
//                try {
//                    mEncoder.flush();
//                } catch (Exception e) {
//                    Log.e(TAG, "flush error!" + e);
//                }
//            }
//        });
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    private boolean _setupEnocder() {
//        ///< 初始化编码器。
//        try {
//            String mimeType = mInputFormat.getString(MediaFormat.KEY_MIME);
//            mEncoder = MediaCodec.createEncoderByType(mimeType);
//            mEncoder.configure(mInputFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
//        } catch (IOException e) {
//            Log.e(TAG, "createEncoderByType" + e);
//            _callBackError(KFMediaCodecInterfaceErrorCreate,e.getMessage());
//            return false;
//        }
//
//        ///< 创建 Surface。
//        mSurface = mEncoder.createInputSurface();
//
//        ///< 开启编码器。
//        try {
//            mEncoder.start();
//        } catch (Exception e) {
//            Log.e(TAG, "start" +  e );
//            _callBackError(KFMediaCodecInterfaceErrorStart,e.getMessage());
//            return false;
//        }
//
//        return true;
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    private void _setupFilter() {
//        ///< 创建渲染模块，渲染到编码器 Surface。
//        if (mFilter == null) {
//            mFilter = new KFGLFilter(true, KFGLBase.defaultVertexShader,KFGLBase.defaultFragmentShader);
//        }
//    }
//
//    private void _callBackError(int error, String errorMsg){
//        ///< 出错回调。
//        if (mListener != null) {
//            mMainHandler.post(()->{
//                mListener.onError(error,TAG + errorMsg);
//            });
//        }
//    }
//}
