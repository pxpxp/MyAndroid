package com.example.myandroid.video.study1.video1;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myandroid.R;

import java.io.InputStream;

public class VideoActivity1 extends AppCompatActivity {

    private ImageView mIv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video1);

        mIv = findViewById(R.id.iv);

        initView();
    }

    private void initView() {
//        Bitmap bitmap = BitmapFactory.decodeFile("file:///android_asset/meinv1.jpg");
        InputStream is = getClass().getResourceAsStream("/assets/meinv1.jpg");
        Bitmap bitmap = BitmapFactory.decodeStream(is);
        mIv.setImageBitmap(bitmap);
    }
}
