package com.example.adamasmacaoyunu;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    TextInputEditText tahminTxt;
    TextView kelimeTxt;

    String deger;
    //String gosterilecekDeger = "";
    StringBuilder gosterilecekDeger;
    char[] karakterDizisi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initComponents();

        gosterilecekDeger = new StringBuilder("");
        registerEventHandlers();
        BasligiDegistir();
        degerUret();
        karakterDizisiniYapilandir();
        degeriAnalizEt();
        degeriGizle();
        degeriOyuncuyaGoster();

    }

    void initComponents(){

        tahminTxt = findViewById(R.id.tahminTxt);
        kelimeTxt = findViewById(R.id.kelimeTxt);
    }

    void registerEventHandlers() {
        tahminTxt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                sahipMi(tahminTxt.getText().toString());
                kelimeTxt.setText(gosterilecekDeger);
            }
        }
        );
    }
    public void degerUret(){
        Random random = new Random();
        int index = random.nextInt(5);
        Log.w("uyarı", Integer.toString(index));
        deger = "sakarya";

    }
    public void karakterDizisiniYapilandir(){
        karakterDizisi = new char[deger.length()];
    }

    public void degeriAnalizEt(){
        for (int i = 0; i < deger.length(); i++) {
            karakterDizisi[i] = deger.charAt(i);
        }
       Log.d("selam", Character.toString(karakterDizisi[0]));
    }

    public void degeriGizle(){
        for (int i = 0; i < deger.length(); i++) {
            //gosterilecekDeger = gosterilecekDeger + " _";
            gosterilecekDeger.append("_ ");
        }


    }

    public void degeriOyuncuyaGoster(){
        kelimeTxt.setText(gosterilecekDeger);
    }

    public void sahipMi(String gelen){
        Log.d("sahipMi",gelen);
        for (int i = 0; i < deger.length(); i++) {
            if (gelen.equals(Character.toString(karakterDizisi[i]))){
                gosterilecekDeger = gosterilecekDeger.replace(i*2, i*2+1, Character.toString(karakterDizisi[i]));
            }
        }
        Log.w("deneme", gosterilecekDeger.toString());
    }

    public void BasligiDegistir(){

        this.setTitle("Adam Asmaca");
    }
}