package com.example.myandroid.util.dialog;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.fragment.app.FragmentManager;

/**
 * @author pxp
 */
public class CustomDialog extends BaseCustomDialog implements DialogInterface{
    private ViewConvertListener convertListener;
    private DismissListener dismissListener;
    private CancelListener cancelListener;

    private static CustomDialog dialog;

    public static CustomDialog init() {
        dialog = new CustomDialog();
        return dialog;
    }

    public void show(@LayoutRes int layoutId, boolean isBottom, FragmentManager manager, ViewConvertListener viewConvertListener) {
        if (dialog != null) {
            dialog.setLayoutId(layoutId)     //设置dialog布局文件
                    .setConvertListener(viewConvertListener)    //进行相关View操作的回调
                    .setDimAmount(0.5f)     //调节灰色背景透明度[0-1]，默认0.5f
                    .setShowBottom(isBottom)     //是否在底部显示dialog，默认flase
                    .setMargin(38)     //dialog左右两边到屏幕边缘的距离（单位：dp），默认0dp
                    .setOutCancel(true)     //点击dialog外是否可取消，默认true
                    .show(manager);     //显示dialog
        }
    }


    @Override
    public int initTheme() {
        return theme;
    }

    @Override
    public int intLayoutId() {
        return layoutId;
    }

    @Override
    public void convertView(ViewHolder holder, BaseCustomDialog dialog) {
        if (convertListener != null) {
            convertListener.convertView(holder, dialog);
        }
    }

    public CustomDialog setTheme(@StyleRes int theme) {
        this.theme = theme;
        return this;
    }

    public CustomDialog setLayoutId(@LayoutRes int layoutId) {
        this.layoutId = layoutId;
        return this;
    }

    public CustomDialog setConvertListener(ViewConvertListener convertListener) {
        this.convertListener = convertListener;
        return this;
    }

    public CustomDialog setCancelListener(CancelListener cancelListener) {
        this.cancelListener = cancelListener;
        return this;
    }

    public CustomDialog setDismissListener(DismissListener dismissListener) {
        this.dismissListener = dismissListener;
        return this;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            convertListener = savedInstanceState.getParcelable("listener");
        }
    }

    /**
     * 保存接口
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("listener", convertListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        convertListener = null;
    }

    @Override
    public void cancel() {

    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        if(cancelListener != null){
            cancelListener.onCancel(dialog);
        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if(dismissListener != null){
            dismissListener.onDismiss(dialog);
        }
    }

    public interface CancelListener{
        void onCancel(DialogInterface dialog);
    }

    public interface DismissListener{
        void onDismiss(DialogInterface dialog);
    }
}
