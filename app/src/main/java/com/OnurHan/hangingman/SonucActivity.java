package com.OnurHan.hangingman;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SonucActivity extends AppCompatActivity {

    public TextView puanTxt, puanDurumuTxt, bulunacakKelimeTxt;
    public Button tekrarOynaBtn, cikisBtn;
    private ImageView sonucResim;
    private Vibrator v;
    private SharedPreferences sharedPreferences;

    private int puan;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    public void applyOverrideConfiguration(Configuration overrideConfiguration) {
        if (overrideConfiguration != null) {
            int uiMode = overrideConfiguration.uiMode;
            overrideConfiguration.setTo(getBaseContext().getResources().getConfiguration());
            overrideConfiguration.uiMode = uiMode;
        }
        super.applyOverrideConfiguration(overrideConfiguration);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sonuc);
        initComponents();
        registerEventHandlers();
        degerleriAyarla();
    }

    private void initComponents() {
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        puanTxt = findViewById(R.id.puanTxt);
        puanDurumuTxt = findViewById(R.id.puanDurumuTxt);
        bulunacakKelimeTxt = findViewById(R.id.bulunacakKelimeTxt);
        tekrarOynaBtn = findViewById(R.id.tektatOynaBtn);
        cikisBtn = findViewById(R.id.cikisBtn);
        sonucResim = findViewById(R.id.sonucResim);
        sharedPreferences = getSharedPreferences("veriler", MODE_PRIVATE);

        Intent intent = getIntent();
        puan = intent.getIntExtra("puan", 0);
        String bulunacakKelime = intent.getStringExtra("bulunacakKelime");
        if (bulunacakKelime != null) {
            bulunacakKelimeTxt.setText(bulunacakKelime);
        }
    }

    private void registerEventHandlers() {
        tekrarOynaBtn.setOnClickListener(v -> {
            Intent intent = new Intent(SonucActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        cikisBtn.setOnClickListener(v -> anaMenuyeDon());
    }

    private void degerleriAyarla() {
        puanTxt.setText(String.valueOf(puan));
        int enYuksekPuan = sharedPreferences.getInt("yuksekPuan", 0);

        if (puan > enYuksekPuan) {
            titresim05Saniye();
            sonucResim.setImageResource(R.drawable.adam_ozgur);
            puanDurumuTxt.setText(getString(R.string.mesaj_yuksek_puan));
            sharedPreferences.edit().putInt("yuksekPuan", puan).apply();
        } else {
            sonucResim.setImageResource(R.drawable.adam6);
            puanDurumuTxt.setText(getString(R.string.mesaj_yuksek_degil, enYuksekPuan));
        }
    }

    private void titresim05Saniye() {
        if (v == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            v.vibrate(500);
        }
    }

    private void anaMenuyeDon() {
        Intent intent = new Intent(SonucActivity.this, MainMenuActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        anaMenuyeDon();
    }
}