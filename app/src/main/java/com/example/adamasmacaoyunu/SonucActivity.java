package com.example.adamasmacaoyunu;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SonucActivity extends AppCompatActivity {

    private static final String MESAJ_YUKSEK_PUAN = "Yüksek Puan!";
    private static final String MESAJ_YUKSEK_DEGIL = "Puanınız.\nYüksek Puanınız: ";

    public TextView puanTxt, puanDurumuTxt, bulunacakKelimeTxt;
    public Button tekrarOynaBtn, cikisBtn;
    private ImageView sonucResim;
    private Vibrator v;
    private SharedPreferences sharedPreferences;

    private int puan;

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
        });

        cikisBtn.setOnClickListener(v -> onBackPressed());
    }

    private void degerleriAyarla() {
        puanTxt.setText(String.valueOf(puan));
        int enYuksekPuan = sharedPreferences.getInt("yuksekPuan", 0);

        if (puan > enYuksekPuan) {
            titresim05Saniye();
            sonucResim.setImageResource(R.drawable.adam_ozgur);
            puanDurumuTxt.setText(MESAJ_YUKSEK_PUAN);
            sharedPreferences.edit().putInt("yuksekPuan", puan).apply();
        } else {
            sonucResim.setImageResource(R.drawable.adam6);
            puanDurumuTxt.setText(MESAJ_YUKSEK_DEGIL + "\n" + enYuksekPuan);
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

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}