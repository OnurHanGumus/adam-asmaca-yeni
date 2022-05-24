package com.example.adamasmacaoyunu;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.prefs.Preferences;

public class SonucActivity extends AppCompatActivity {
    public TextView puanTxt, puanDurumuTxt, bulunacakKelimeTxt;
    public Button tekrarOynaBtn, cikisBtn;
    ImageView sonucResim;
    Intent intent;
    int puan;
    String bulunacakKelime;
    String mesajYuksekPuan = "Yüksek Puan!";
    String mesajYuksekDegil = "Puanınız.\nYüksek Puanınız: ";
    Vibrator v;

    SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sonuc);
        initComponents();
        registerEventHandlers();

        degerleriAyarla();
    }
    public void initComponents(){
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        puanTxt = findViewById(R.id.puanTxt);
        puanDurumuTxt = findViewById(R.id.puanDurumuTxt);
        bulunacakKelimeTxt = findViewById(R.id.bulunacakKelimeTxt);
        tekrarOynaBtn = findViewById(R.id.tektatOynaBtn);
        cikisBtn = findViewById(R.id.cikisBtn);
        sonucResim = findViewById(R.id.sonucResim);
        sharedPreferences = getSharedPreferences("veriler", MODE_PRIVATE);

        intent = getIntent();
        puan = Integer.parseInt(intent.getStringExtra("puan"));
        bulunacakKelime = intent.getStringExtra("bulunacakKelime");
        bulunacakKelimeTxt.setText(bulunacakKelime);
        Log.i("tag", "mesaj:"+ puan);

    }
    public void registerEventHandlers(){
        tekrarOynaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SonucActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        cikisBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();

            }
        });
    }

    public void degerleriAyarla(){
        puanTxt.setText(String.valueOf(puan));
        if (puan >  sharedPreferences.getInt("yuksekPuan",0))
        {
            titresim05Saniye();
            sonucResim.setImageResource(R.drawable.adam_ozgur);
            puanDurumuTxt.setText(mesajYuksekPuan);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("yuksekPuan", puan);
            editor.apply();
        }
        else{
            sonucResim.setImageResource(R.drawable.adam6);
            puanDurumuTxt.setText(mesajYuksekDegil + "\n" + sharedPreferences.getInt("yuksekPuan",0));

        }

    }

    void titresim05Saniye(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            v.vibrate(500);
        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}