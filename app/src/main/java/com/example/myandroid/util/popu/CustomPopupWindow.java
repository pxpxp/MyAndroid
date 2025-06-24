package com.example.myandroid.util.popu;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

/**
 * PopupWindow 封装工具类
 */
public class CustomPopupWindow extends PopupWindow {

    private CustomPopupWindow(Builder builder) {
        super(builder.context);
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        builder.view.measure(widthMeasureSpec, heightMeasureSpec);

//        builder.view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        setContentView(builder.view);
        if (builder.cancelTouchout) {
            setBackgroundDrawable(new ColorDrawable(0x00000000));//设置透明背景
            setOutsideTouchable(builder.cancelTouchout);//设置outside可点击
        }
        setFocusable(builder.isFocusable);
        setTouchable(builder.isTouchable);

        if (builder.animStyle != 0) {
            setAnimationStyle(builder.animStyle);
        }

        if (builder.convertListener != null) {
            builder.convertListener.convertView(ViewHolder.create(builder.view), this);
        }

        if (builder.width == 0) {
            setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        } else if (builder.width == -1) {
            setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
//            setWidth(getScreenWidth(builder.context));
        } else {
            setWidth(builder.width);
        }

        if (builder.height == 0) {
            setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        } else if (builder.height == -1) {
            setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        } else {
            setHeight(builder.height);
        }
    }

    @Override
    public int getWidth() {
        return getContentView().getMeasuredWidth();
    }

    @Override
    public int getHeight() {
        return getContentView().getMeasuredHeight();
    }

    private int getScreenWidth(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.widthPixels;
    }

    public static final class Builder {

        private Context context;
        private int height, width;
        private boolean cancelTouchout = true;
        private boolean isFocusable = true;
        private boolean isTouchable = true;
        private View view;
        private int animStyle;
        private ViewConvertListener convertListener;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder view(int resView) {
            this.view = LayoutInflater.from(context).inflate(resView, null);
            return this;
        }

        public Builder view(View resVew) {
            this.view = resVew;
            return this;
        }

        public Builder height(int height) {
            if(height == -1){
                this.height = height;
                return this;
            }
            this.height = dip2px(this.context, height);
            return this;
        }

        public Builder width(int width) {
            if(width == -1){
                this.width = width;
                return this;
            }
            this.width = dip2px(this.context, width);
            return this;
        }

        public Builder cancelTouchout(boolean cancelTouchout) {
            this.cancelTouchout = cancelTouchout;
            return this;
        }

        public Builder isFocusable(boolean isFocusable) {
            this.isFocusable = isFocusable;
            return this;
        }

        public Builder isTouchable(boolean isTouchable) {
            this.isTouchable = isTouchable;
            return this;
        }

        public Builder animStyle(int animStyle) {
            this.animStyle = animStyle;
            return this;
        }

        public Builder addViewOnclick(int viewRes, View.OnClickListener listener) {
            view.findViewById(viewRes).setOnClickListener(listener);
            return this;
        }

        public Builder setConvertListener(ViewConvertListener convertListener) {
            this.convertListener = convertListener;
            return this;
        }

        private int dip2px(Context context, float dipValue) {
            final float scale = context.getResources().getDisplayMetrics().density;
            return (int) (dipValue * scale + 0.5f);
        }

        public CustomPopupWindow build() {
            return new CustomPopupWindow(this);
        }
    }
}

