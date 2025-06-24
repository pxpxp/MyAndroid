//package com.example.myandroid.video.keyframe.video7;
//
//import static android.hardware.camera2.CameraMetadata.LENS_FACING_FRONT;
//
//import android.Manifest;
//import android.app.Activity;
//import android.content.pm.PackageManager;
//import android.graphics.Rect;
//import android.os.Build;
//import android.os.Bundle;
//import android.util.Size;
//import android.view.WindowManager;
//import android.widget.FrameLayout;
//
//import androidx.annotation.RequiresApi;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.app.ActivityCompat;
//
//import com.example.myandroid.R;
//import com.example.myandroid.video.keyframe.video1.KFFrame;
//import com.example.myandroid.video.keyframeopengl.opengl1.KFRenderView;
//
///**
// * https://mp.weixin.qq.com/s?__biz=MjM5MTkxOTQyMQ==&mid=2257485786&idx=1&sn=1dc16eedc3972f1c28b32b5fd58109eb&chksm=a5d4e38892a36a9e3cc0bcfd686634fbfa8a790be7ceafae38b5b4b630e4f65970ab15b27e8c&cur_album_id=2273301900659851268&scene=189#wechat_redirect
// * Android AVDemo（7）：视频采集，视频系列来了
// *
// * 1）实现两个视频采集模块，分别为 Camera 与 Camera2；
// * 2）实现视频采集逻辑并将采集的视频图像渲染进行预览；
// * 3）详尽的代码注释，帮你理解代码逻辑和原理。
// *
// * 实现视频采集并实时预览的逻辑。
// */
//public class KFVideoActivity7 extends AppCompatActivity {
//
//    private KFIVideoCapture mCapture; ///< 相机采集。
//    private KFVideoCaptureConfig mCaptureConfig; ///< 相机采集配置。
//    private KFRenderView mRenderView; ///< 渲染视图。
//    private KFGLContext mGLContext; ///< OpenGL 上下文。
//
//
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        ///< 检测采集相关权限。
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
//                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
//                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions((Activity) this,
//                    new String[] {Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                    1);
//        }
//
//        ///< OpenGL 上下文。
//        mGLContext = new KFGLContext(null);
//        ///< 渲染视图。
//        mRenderView = new KFRenderView(this,mGLContext.getContext());
//        WindowManager windowManager = (WindowManager)this.getSystemService(this.WINDOW_SERVICE);
//        Rect outRect = new Rect();
//        windowManager.getDefaultDisplay().getRectSize(outRect);
//        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(outRect.width(), outRect.height());
//        addContentView(mRenderView,params);
//
//        ///< 采集配置：摄像头方向、分辨率、帧率。
//        mCaptureConfig = new KFVideoCaptureConfig();
//        mCaptureConfig.cameraFacing = LENS_FACING_FRONT;
//        mCaptureConfig.resolution = new Size(720,1280);
//        mCaptureConfig.fps = 30;
//        boolean useCamera2 = false;
////        if (useCamera2) {
////            mCapture = new KFVideoCaptureV2();
////        } else {
//            mCapture = new KFVideoCaptureV1();
////        }
//        mCapture.setup(this,mCaptureConfig,mVideoCaptureListener,mGLContext.getContext());
//        mCapture.startRunning();
//    }
//
//    private KFVideoCaptureListener mVideoCaptureListener = new KFVideoCaptureListener() {
//        @Override
//        ///< 相机打开回调。
//        public void cameraOnOpened(){}
//
//        @Override
//        ///< 相机关闭回调。
//        public void cameraOnClosed() {
//        }
//
//        @Override
//        ///< 相机出错回调。
//        public void cameraOnError(int error,String errorMsg) {
//
//        }
//
//        @Override
//        ///< 相机数据回调。
//        public void onFrameAvailable(KFFrame frame) {
//            mRenderView.render((KFTextureFrame) frame);
//        }
//    };
//}
