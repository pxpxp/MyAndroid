package com.example.myandroid.util.popu;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * PopupWindow 封装工具类
 */
public abstract class ViewConvertListener  implements Parcelable {

    protected abstract void convertView(ViewHolder holder, CustomPopupWindow popupWindow);

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    public ViewConvertListener() {
    }

    protected ViewConvertListener(Parcel in) {
    }

    public static final Creator<ViewConvertListener> CREATOR = new Creator<ViewConvertListener>() {
        @Override
        public ViewConvertListener createFromParcel(Parcel source) {
            return new ViewConvertListener(source){
                @Override
                protected void convertView(ViewHolder holder, CustomPopupWindow popupWindow) {

                }
            };
        }

        @Override
        public ViewConvertListener[] newArray(int size) {
            return new ViewConvertListener[size];
        }
    };
}
