//package com.example.myandroid.video.keyframe.video8;
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
//import com.example.myandroid.video.keyframe.video7.KFGLContext;
//import com.example.myandroid.video.keyframe.video7.KFIVideoCapture;
//import com.example.myandroid.video.keyframe.video7.KFRenderView;
//import com.example.myandroid.video.keyframe.video7.KFTextureFrame;
//import com.example.myandroid.video.keyframe.video7.KFVideoCaptureConfig;
//import com.example.myandroid.video.keyframe.video7.KFVideoCaptureListener;
//import com.example.myandroid.video.keyframe.video7.KFVideoCaptureV1;
//import com.example.myandroid.video.keyframe.video7.KFVideoCaptureV2;
//
///**
// * https://mp.weixin.qq.com/s?__biz=MjM5MTkxOTQyMQ==&mid=2257485813&idx=1&sn=187d98e7b2ff5ea1f226e3b57ea69eb5&chksm=a5d4e3a792a36ab1538d0043fe5bfa671a271520227bfcad8e1f4cce3de534ba898e548e25bf&scene=178&cur_album_id=2336659494606667777#rd
// * Android AVDemo（8）：视频编码，H.264 和 H.265 都支持，采集视频数据进行 H.264/H.265 编码和存储
// */
//public class KFVideoActivity8 extends AppCompatActivity {
//
//    private KFIVideoCapture mCapture; ///< 相机采集。
//    private KFVideoCaptureConfig mCaptureConfig; ///< 相机采集配置。
//    private KFRenderView mRenderView; ///< 渲染视图。
//    private KFGLContext mGLContext; ///< OpenGL 上下文。
//
//
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
//        if (useCamera2) {
//            mCapture = new KFVideoCaptureV2();
//        } else {
//            mCapture = new KFVideoCaptureV1();
//        }
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
