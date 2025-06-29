//package com.example.myandroid.video.keyframe.video7;
//
//import android.Manifest;
//import android.app.Activity;
//import android.app.admin.DevicePolicyManager;
//import android.content.Context;
//import android.content.pm.PackageManager;
//import android.graphics.SurfaceTexture;
//import android.hardware.Camera;
//import android.hardware.camera2.CameraCharacteristics;
//import android.os.Handler;
//import android.os.HandlerThread;
//import android.os.Looper;
//import android.util.Range;
//import android.util.Size;
//
//import androidx.core.app.ActivityCompat;
//
//import java.io.IOException;
//import java.lang.ref.WeakReference;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.List;
//
//import android.opengl.EGLContext;
//
//
///**
// * @author pxp
// * @description
// * 可以看到在实现采集时，我们是用 mCamera 来管理相关接口，通过它控制视频采集开始、结束、设置参数、设置输出目标等。重点介绍一下设置输出目标 setPreviewTexture，采集器内部自己创建一个 SurfaceTexture[1]，将其提供给采集器，这样的优点是后续处理特效、编码等其它操作比较灵活。
// *
// * 从代码上可以看到主要有这几个部分：
// *
// * 1）初始化接口 setup。
// * 初始化采集线程、渲染线程，子线程处理的好处是有效避免主线程卡顿。
// * 初始化 OpenGL 上下文，将数据输出到自定义纹理上 mSurfaceTexture。
// * 2）创建采集设备与开启预览 startRunning。
// * 检测视频采集权限 checkSelfPermission。
// * 检测摄像头是否可用，_checkCameraService。
// * 开启预览 _startRunning，首先获取前后台摄像机信息 mFrontCameraInfo、mFrontCameraId、mBackCameraInfo、mBackCameraId，通过 CameraId 打开摄像头，后续依次设置分辨率、帧率、方向、输出目标等参数。设置好后通过 startPreview 开启预览，数据则会自动同步到 mSurfaceTexture。
// * 3）数据输出回调在 KFSurfaceTextureListener 的 onFrameAvailable。
// * 通过 mSurfaceTexture 的 updateTexImage 将数据转换为自定义纹理。
// * 将纹理数据与纹理矩阵封装为 KFTextureFrame 通过回调 onFrameAvailable 输出给外层。
// * 4）实现切换摄像头的功能。
// * 在 switchCamera 中实现，一共分三步，停止之前摄像头、修改摄像头标记位、开启新的摄像头。
// * 5）停止视频采集 stopRunning。
// * 设置回调为空 setPreviewCallback。
// * 停止预览 stopPreview。
// * 释放摄像头 mCamera.release。
// * 6）清理摄像机实例 release。
// */
//public class KFVideoCaptureV1 implements KFIVideoCapture {
//
//    public static final int KFVideoCaptureV1CameraDisableError = -3000;
//    private static final String TAG = "KFVideoCaptureV1";
//
//    private KFVideoCaptureListener mListener = null; ///< 回调。
//    private KFVideoCaptureConfig mConfig = null; ///< 配置。
//    private WeakReference<Context> mContext = null;
//    private boolean mCameraIsRunning = false; ///< 是否正在采集。
//
//    private HandlerThread mCameraThread = null; ///< 采集线程。
//    private Handler mCameraHandler = null;
//
//    private KFGLContext mGLContext = null; ///< GL 特效上下文。
//    private KFSurfaceTexture mSurfaceTexture = null; ///< Surface 纹理。
//    private HandlerThread mRenderThread = null; ///< 渲染线程。
//    private Handler mRenderHandler = null;
//    private Handler mMainHandler = new Handler(Looper.getMainLooper()); ///< 主线程。
//
//    private Camera.CameraInfo mFrontCameraInfo = null; ///< 前置摄像头信息。
//    private int mFrontCameraId = -1;
//    private Camera.CameraInfo mBackCameraInfo = null; ///< 后置摄像头信息。
//    private int mBackCameraId = -1;
//    private Camera mCamera = null; ///< 当前摄像头实例（前置或者后置）。
//
//    public KFVideoCaptureV1() {
//
//    }
//
//    @Override
//    public void setup(Context context, KFVideoCaptureConfig config, KFVideoCaptureListener listener, EGLContext eglShareContext) {
//        mListener = listener;
//        mConfig = config;
//        mContext = new WeakReference<Context>(context);
//
//        ///< 采集线程。
//        mCameraThread = new HandlerThread("KFCameraThread");
//        mCameraThread.start();
//        mCameraHandler = new Handler((mCameraThread.getLooper()));
//
//        ///< 渲染线程。
//        mRenderThread = new HandlerThread("KFCameraRenderThread");
//        mRenderThread.start();
//        mRenderHandler = new Handler((mRenderThread.getLooper()));
//
//        ///< OpenGL 上下文。
//        mGLContext = new KFGLContext(eglShareContext);
//    }
//
//    @Override
//    public EGLContext getEGLContext() {
//        return mGLContext.getContext();
//    }
//
//    @Override
//    public boolean isRunning() {
//        return mCameraIsRunning;
//    }
//
//    @Override
//    public void release() {
//        mCameraHandler.post(() -> {
//            ///< 停止视频采集 清晰视频采集实例、OpenGL 上下文、线程等。
//            _stopRunning();
//            mGLContext.bind();
//            if (mSurfaceTexture != null) {
//                mSurfaceTexture.release();
//                mSurfaceTexture = null;
//            }
//            mGLContext.unbind();
//            mGLContext.release();
//            mGLContext = null;
//
//            if (mCamera != null) {
//                mCamera.release();
//                mCamera = null;
//            }
//
//            mCameraThread.quit();
//            mRenderThread.quit();
//        });
//    }
//
//    @Override
//    public void startRunning() {
//        mCameraHandler.post(() -> {
//            ///< 检测视频采集权限。
//            if (ActivityCompat.checkSelfPermission(mContext.get(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions((Activity) mContext.get(), new String[] {Manifest.permission.CAMERA}, 1);
//            }
//            ///< 检测相机是否可用。
//            if (!_checkCameraService()) {
//                _callBackError(KFVideoCaptureV1CameraDisableError, "相机不可用");
//                return;
//            }
//
//            ///< 开启视频采集。
//            _startRunning();
//        });
//    }
//
//    @Override
//    public void stopRunning() {
//        mCameraHandler.post(() -> {
//            _stopRunning();
//        });
//    }
//
//    @Override
//    public void switchCamera() {
//        mCameraHandler.post(() -> {
//            ///< 切换摄像头，先关闭相机调整方向再打开相机。
//            _stopRunning();
//            mConfig.cameraFacing = mConfig.cameraFacing == CameraCharacteristics.LENS_FACING_FRONT ? CameraCharacteristics.LENS_FACING_BACK : CameraCharacteristics.LENS_FACING_FRONT;
//            _startRunning();
//        });
//    }
//
//    private void _startRunning() {
//        ///< 获取前后台摄像机信息。
//        if (mFrontCameraInfo == null || mBackCameraInfo == null) {
//            _initCameraInfo();
//        }
//
//        try {
//            ///< 根据前后台摄像头 id 打开相机实例。
//            mCamera = Camera.open(_getCurrentCameraId());
//            if (mCamera != null) {
//                ///< 设置相机各分辨率、帧率、方向。
//                Camera.Parameters parameters = mCamera.getParameters();
//                Size previewSize = _getOptimalSize(mConfig.resolution.getWidth(), mConfig.resolution.getHeight());
//                mConfig.resolution = new Size(previewSize.getHeight(),previewSize.getWidth());
//                parameters.setPreviewSize(previewSize.getWidth(),previewSize.getHeight());
//                Range<Integer> selectFpsRange = _chooseFpsRange();
//                if (selectFpsRange.getUpper() > 0) {
//                    parameters.setPreviewFpsRange(selectFpsRange.getLower(),selectFpsRange.getUpper());
//                }
//                mCamera.setParameters(parameters);
//                mCamera.setDisplayOrientation(_getDisplayOrientation());
//                ///< 创建 Surface 纹理。
//                if (mSurfaceTexture == null) {
//                    mGLContext.bind();
//                    mSurfaceTexture = new KFSurfaceTexture(mSurfaceTextureListener);
//                    mGLContext.unbind();
//                }
//                ///< 设置 SurfaceTexture 给 Camera，这样 Camera 自动将数据渲染到 SurfaceTexture。
//                mCamera.setPreviewTexture(mSurfaceTexture.getSurfaceTexture());
//                ///< 开启预览。
//                mCamera.startPreview();
//                mCameraIsRunning = true;
//                if (mListener != null) {
//                    mMainHandler.post(()->{
//                        ///< 回调相机打开。
//                        mListener.cameraOnOpened();
//                    });
//                }
//            }
//        } catch (RuntimeException | IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void _stopRunning() {
//        if (mCamera != null) {
//            ///< 关闭相机采集。
//            mCamera.setPreviewCallback(null);
//            mCamera.stopPreview();
//            mCamera.release();
//            mCamera = null;
//            mCameraIsRunning = false;
//            if (mListener != null) {
//                mMainHandler.post(()->{
//                    ///< 回调相机关闭。
//                    mListener.cameraOnClosed();
//                });
//            }
//        }
//    }
//
//    private int _getCurrentCameraId() {
//        ///< 获取当前摄像机 id。
//        if (mConfig.cameraFacing == CameraCharacteristics.LENS_FACING_FRONT) {
//            return mFrontCameraId;
//        } else if (mConfig.cameraFacing == CameraCharacteristics.LENS_FACING_BACK) {
//            return mBackCameraId;
//        } else {
//            throw new RuntimeException("No available camera id found.");
//        }
//    }
//
//    private int _getDisplayOrientation() {
//        ///< 获取摄像机需要旋转的方向。
//        int orientation = 0;
//        if (mConfig.cameraFacing == CameraCharacteristics.LENS_FACING_FRONT) {
//            orientation = (_getCurrentCameraInfo().orientation) % 360;
//            orientation = (360 - orientation) % 360;
//        } else {
//            orientation = (_getCurrentCameraInfo().orientation + 360) % 360;
//        }
//        return orientation;
//    }
//
//    private Camera.CameraInfo _getCurrentCameraInfo() {
//        ///< 获取当前摄像机描述信息。
//        if (mConfig.cameraFacing == CameraCharacteristics.LENS_FACING_FRONT) {
//            return mFrontCameraInfo;
//        } else if (mConfig.cameraFacing == CameraCharacteristics.LENS_FACING_BACK) {
//            return mBackCameraInfo;
//        } else {
//            throw new RuntimeException("No available camera id found.");
//        }
//    }
//
//    private Size _getOptimalSize(int width, int height) {
//        ///< 根据外层输入分辨率查找对应最合适的分辨率。
//        List<Camera.Size> sizeMap = mCamera.getParameters().getSupportedPreviewSizes();
//        List<Size> sizeList = new ArrayList<>();
//        for (Camera.Size option:sizeMap) {
//            if (width > height) {
//                if (option.width >= width && option.height >= height) {
//                    sizeList.add(new Size(option.width,option.height));
//                }
//            } else {
//                if (option.width >= height && option.height >= width) {
//                    sizeList.add(new Size(option.width,option.height));
//                }
//            }
//        }
//        if (sizeList.size() > 0) {
//            return Collections.min(sizeList, new Comparator<Size>() {
//                @Override
//                public int compare(Size o1, Size o2) {
//                    return Long.signum(o1.getWidth() * o1.getHeight() - o2.getWidth() * o2.getHeight());
//                }
//            });
//        }
//
//        return new Size(0,0);
//    }
//
//    private Range<Integer> _chooseFpsRange() {
//        ///< 根据外层设置帧率查找最合适的帧率。
//        List<int[]> fpsRange = mCamera.getParameters().getSupportedPreviewFpsRange();
//        for (int[] range : fpsRange) {
//            if (range.length == 2 && range[1] >= mConfig.fps*1000 && range[0] <= mConfig.fps*1000) {
//                return new Range<>(range[0],range[1]);///< 仅支持列表中一项，不能像 camera2 一样指定
//            }
//        }
//
//        return new Range<Integer>(0,0);
//    }
//
//    private void _initCameraInfo() {
//        ///< 获取前置后置摄像头描述信息与 id。
//        int numberOfCameras = Camera.getNumberOfCameras();
//        for (int cameraId = 0; cameraId < numberOfCameras; cameraId++) {
//            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
//            Camera.getCameraInfo(cameraId, cameraInfo);
//            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
//                // 后置摄像头信息。
//                mBackCameraId = cameraId;
//                mBackCameraInfo = cameraInfo;
//            } else if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
//                // 前置摄像头信息。
//                mFrontCameraId = cameraId;
//                mFrontCameraInfo = cameraInfo;
//            }
//        }
//    }
//
//    private boolean _checkCameraService() {
//        ///< 检测相机是否可用。
//        DevicePolicyManager dpm = (DevicePolicyManager)mContext.get().getSystemService(Context.DEVICE_POLICY_SERVICE);
//        if (dpm.getCameraDisabled(null)) {
//            return false;
//        }
//        return true;
//    }
//
//    private void _callBackError(int error, String errorMsg) {
//        ///< 错误回调。
//        if (mListener != null) {
//            mMainHandler.post(()->{
//                mListener.cameraOnError(error,TAG + errorMsg);
//            });
//        }
//    }
//
//    private KFSurfaceTextureListener mSurfaceTextureListener = new KFSurfaceTextureListener() {
//        @Override
//        ///< SurfaceTexture 数据回调。
//        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
//            mRenderHandler.post(()->{
//                long timestamp = System.nanoTime();
//                mGLContext.bind();
//                ///< 刷新纹理数据至 SurfaceTexture。
//                mSurfaceTexture.getSurfaceTexture().updateTexImage();
//                if (mListener != null) {
//                    ///< 拼装好纹理数据返回给外层。
//                    KFTextureFrame frame = new KFTextureFrame(mSurfaceTexture.getSurfaceTextureId(),mConfig.resolution,timestamp,true);
//                    mSurfaceTexture.getSurfaceTexture().getTransformMatrix(frame.textureMatrix);
//                    mListener.onFrameAvailable(frame);
//                }
//                mGLContext.unbind();
//            });
//        }
//    };
//}
