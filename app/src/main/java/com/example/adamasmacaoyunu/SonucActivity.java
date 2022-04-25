package com.example.adamasmacaoyunu;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.prefs.Preferences;

public class SonucActivity extends AppCompatActivity {
    public TextView puanTxt, puanDurumuTxt;
    public Button tekrarOynaBtn, cikisBtn;
    Intent intent;
    int puan;
    String mesajYuksekPuan = "Yüksek Puan!";
    String mesajYuksekDegil = "Puanınız.\nYüksek Puanınız: ";

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
        puanTxt = findViewById(R.id.puanTxt);
        puanDurumuTxt = findViewById(R.id.puanDurumuTxt);
        tekrarOynaBtn = findViewById(R.id.tektatOynaBtn);
        cikisBtn = findViewById(R.id.cikisBtn);
        sharedPreferences = getSharedPreferences("veriler", MODE_PRIVATE);

        intent = getIntent();
        puan = Integer.parseInt(intent.getStringExtra("puan"));
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
            puanDurumuTxt.setText(mesajYuksekPuan);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("yuksekPuan", puan);
            editor.apply();
        }
        else
            puanDurumuTxt.setText(mesajYuksekDegil + "\n" + sharedPreferences.getInt("yuksekPuan",0));

    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}