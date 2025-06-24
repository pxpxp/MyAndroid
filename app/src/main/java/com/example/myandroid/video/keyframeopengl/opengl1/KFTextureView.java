package com.example.myandroid.video.keyframeopengl.opengl1;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.view.Surface;
import android.view.TextureView;

import androidx.annotation.NonNull;

/**
 * @author pxp
 * @description
 */
public class KFTextureView extends TextureView implements TextureView.SurfaceTextureListener{
    private KFRenderListener mListener = null; // 回调
    private Surface mSurface = null; // 渲染缓存
    private SurfaceTexture mSurfaceTexture = null; // 纹理缓存

    public KFTextureView(Context context, KFRenderListener listener) {
        super(context);
        this.setSurfaceTextureListener(this);
        mListener = listener;
    }

    @Override
    public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int width, int height) {
        // 纹理缓存创建
        mSurfaceTexture = surfaceTexture;
        // 根据 SurfaceTexture 创建 Surface
        mSurface = new Surface(surfaceTexture);
        if (mListener != null) {
            // 创建时候回调一次分辨率变更，对应 SurfaceView 接口
            mListener.surfaceCreate(mSurface);
            mListener.surfaceChanged(mSurface,width,height);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int width, int height) {
        // 纹理缓存变更分辨率
        if (mListener != null) {
            mListener.surfaceChanged(mSurface,width,height);
        }
    }

    @Override
    public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
        // 纹理缓存销毁
        if (mListener != null) {
            mListener.surfaceDestroy(mSurface);
        }
        if (mSurface != null) {
            mSurface.release();
            mSurface = null;
        }
        return false;
    }
}