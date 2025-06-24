package com.example.myandroid.video.keyframeopengl.opengl1

import android.view.Surface


/**
 * @author pxp
 * @description
 */
open interface KFRenderListener {
    fun surfaceCreate(surface: Surface) // 渲染缓存创建
    fun surfaceChanged(surface: Surface, width: Int, height: Int) // 渲染缓存变更分辨率
    fun surfaceDestroy(surface: Surface) // 渲染缓存销毁
}