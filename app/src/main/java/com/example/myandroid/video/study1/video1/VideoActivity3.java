package com.example.myandroid.video.study1.video1;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myandroid.R;

public class VideoActivity3 extends AppCompatActivity {

    private CustomView mCv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video3);

        mCv = findViewById(R.id.cv);

        initView();
    }

    private void initView() {

    }
}
