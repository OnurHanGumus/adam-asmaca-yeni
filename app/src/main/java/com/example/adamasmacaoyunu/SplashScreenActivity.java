package com.example.adamasmacaoyunu;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashScreenActivity extends AppCompatActivity {
    private float SPLASH_TIME_OUT = 2000; // 2 sn
    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        SPLASH_TIME_OUT = getResources().getInteger(R.integer.splash_screen);
        timer();

    }
    public void timer(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(SplashScreenActivity.this, MainActivity.class);
                startActivity(i);
                // activity’nin sonlandırılması
                finish();
            }
        }, (long) SPLASH_TIME_OUT);
    }
}