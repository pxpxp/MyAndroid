package com.example.myandroid.video.keyframeopengl.opengl1;

import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDisableVertexAttribArray;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glVertexAttribPointer;

import android.content.Context;
import android.opengl.EGLContext;
import android.opengl.GLES20;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.example.myandroid.video.keyframe.video7.KFGLContext;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * @author pxp
 * @description
 */
public class KFRenderView extends ViewGroup {
    // 顶点着色器
    public static String customVertexShader =
            "attribute vec4 v_position;\n" +
                    "attribute vec4 v_color;\n" +
                    "varying mediump vec4 f_color;\n" +
                    "void main() {\n" +
                    "  f_color = v_color;\n" +
                    "  gl_Position = v_position;\n" +
                    "}\n";

    // 片元着色器
    public static String customFragmentShader =
            "varying mediump vec4 f_color;\n" +
                    "void main() {\n" +
                    "  gl_FragColor = f_color;\n" +
                    "}\n";

    private KFGLContext mEGLContext = null; // OpenGL 上下文
    private EGLContext mShareContext = null; // 共享上下文
    private View mRenderView = null; // 渲染视图基类
    private int mSurfaceWidth = 0; // 渲染缓存宽
    private int mSurfaceHeight = 0; // 渲染缓存高
    private boolean mSurfaceChanged = false; // 渲染缓存是否变更
    private KFGLProgram mProgram;
    private FloatBuffer mVerticesBuffer = null; // 顶点 buffer
    private int mPositionAttribute = -1; // 顶点坐标
    private int mColorAttribute = -1; // 顶点颜色

    public KFRenderView(Context context, EGLContext eglContext) {
        super(context);
        mShareContext = eglContext; // 共享上下文

        // 1、选择实际的渲染视图
        boolean isSurfaceView = false; // TextureView 与 SurfaceView 开关
        if (isSurfaceView) {
            mRenderView = new KFSurfaceView(context, mListener);
        } else {
            mRenderView = new KFTextureView(context, mListener);
        }

        this.addView(mRenderView); // 添加视图到父视图
    }

    public void release() {
        // 释放 OpenGL 上下文、特效
        if (mEGLContext != null) {
            mEGLContext.bind();
            mEGLContext.unbind();

            mEGLContext.release();
            mEGLContext = null;
        }
    }

    public void render() {
        mProgram.use();

        // 设置帧缓存背景色
        glClearColor(0.5f, 0.5f, 0.5f, 1);
        // 清空帧缓存颜色
        glClear(GLES20.GL_COLOR_BUFFER_BIT);
        // 设置渲染窗口区域
        GLES20.glViewport(0, 0, mSurfaceWidth, mSurfaceHeight);

        // 启用顶点着色器顶点坐标属性
        glEnableVertexAttribArray(mPositionAttribute);
        mVerticesBuffer.position(0); // 定位到第一个位置分量
        glVertexAttribPointer(
                mPositionAttribute,
                3, // x, y, z 有 3 个分量
                GLES20.GL_FLOAT,
                false,
                7 * 4, // 每个顶点有 xyzrgba 7 个分量，每个分量是 4 字节，所以步进为 7 * 4 字节
                mVerticesBuffer);

        // 启用顶点着色器顶点颜色属性
        glEnableVertexAttribArray(mColorAttribute);
        mVerticesBuffer.position(3); // 定位到第一个颜色分量
        glVertexAttribPointer(
                mColorAttribute,
                4, // r, g, b, a 有 4 个分量
                GLES20.GL_FLOAT,
                false,
                7 * 4, // 每个顶点有 xyzrgba 7 个分量，每个分量是 4 字节，所以步进为 7 * 4 字节
                mVerticesBuffer);

        // 绘制三角形
        glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);

        // 关闭顶点着色器顶点坐标属性
        glDisableVertexAttribArray(mPositionAttribute);

        // 关闭顶点着色器顶点颜色属性
        glDisableVertexAttribArray(mColorAttribute);

        mEGLContext.swapBuffers();
    }

    private KFRenderListener mListener = new KFRenderListener() {
        @Override
        // 渲染缓存创建
        public void surfaceCreate(@NonNull Surface surface) {
            // 2、创建 OpenGL 上下文
            mEGLContext = new KFGLContext(mShareContext, surface);
            mEGLContext.bind();
            // 3、初始化 GL 相关环境：加载和编译 shader、链接到着色器程序、设置顶点数据
            _setupGL();
            mEGLContext.unbind();
        }

        @Override
        // 渲染缓存变更
        public void surfaceChanged(@NonNull Surface surface, int width, int height) {
            mSurfaceWidth = width;
            mSurfaceHeight = height;
            mSurfaceChanged = true;
            mEGLContext.bind();
            // 4、设置 OpenGL 上下文 Surface
            mEGLContext.setSurface(surface);
            // 5、绘制三角形
            render();
            mEGLContext.unbind();
        }

        @Override
        public void surfaceDestroy(@NonNull Surface surface) {

        }
    };

    private void _setupGL() {
        // 加载和编译 shader，并链接到着色器程序
        mProgram = new KFGLProgram(customVertexShader, customFragmentShader);

        // 获取与 shader 中对应的属性信息
        mPositionAttribute = mProgram.getAttribLocation("v_position");
        mColorAttribute = mProgram.getAttribLocation("v_color");

        // 3 个顶点，每个顶点有 7 个分量：x, y, z, r, g, b, a
        final float vertices[] = {
                -0.5f, 0.5f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, // 左下 // 红色
                -0.5f, -0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, // 右下 // 绿色
                0.5f, -0.5f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, // 左上 // 蓝色
        };
        ByteBuffer verticesByteBuffer = ByteBuffer.allocateDirect(4 * vertices.length);
        verticesByteBuffer.order(ByteOrder.nativeOrder());
        mVerticesBuffer = verticesByteBuffer.asFloatBuffer();
        mVerticesBuffer.put(vertices);
        mVerticesBuffer.position(0);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        // 视图变更 Size
        this.mRenderView.layout(left, top, right, bottom);
    }
}
