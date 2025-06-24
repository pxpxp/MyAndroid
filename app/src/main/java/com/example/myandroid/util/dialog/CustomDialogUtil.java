package com.example.myandroid.util.dialog;

import androidx.annotation.LayoutRes;
import androidx.fragment.app.FragmentManager;

/**
 * @author pxp
 */
public class CustomDialogUtil {
    private volatile static CustomDialogUtil instance;

    private CustomDialogUtil() {
    }

    public static CustomDialogUtil getInstance() {
        if (instance == null) {
            synchronized (CustomDialogUtil.class) {
                if (instance == null) {
                    instance = new CustomDialogUtil();
                }
            }
        }
        return instance;
    }

    public void show(@LayoutRes int layoutId, boolean isBottom, FragmentManager manager, ViewConvertListener viewConvertListener) {
        CustomDialog.init()
                .setLayoutId(layoutId)     //设置dialog布局文件
//                .setTheme(R.style.MyDialog) // 设置dialog主题，默认主题继承自Theme.AppCompat.Light.Dialog
                .setConvertListener(viewConvertListener)    //进行相关View操作的回调
                .setDimAmount(0.5f)     //调节灰色背景透明度[0-1]，默认0.5f
                .setShowBottom(isBottom)     //是否在底部显示dialog，默认flase
                .setMargin(30)     //dialog左右两边到屏幕边缘的距离（单位：dp），默认0dp
//                .setWidth(-1)     //dialog宽度（单位：dp），默认为屏幕宽度，-1代表WRAP_CONTENT
                .setHeight(-1)     //dialog高度（单位：dp），默认为WRAP_CONTENT
                .setOutCancel(true)     //点击dialog外是否可取消，默认true
//                .setAnimStyle(R.style.EnterExitAnimation)     //设置dialog进入、退出的动画style(底部显示的dialog有默认动画)
                .show(manager);     //显示dialog
    }
}
