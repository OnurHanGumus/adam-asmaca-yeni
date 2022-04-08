package com.example.adamasmacaoyunu;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    TextInputEditText harfTahminTxt;
    TextInputEditText kelimeTahminTxt;
    TextView kelimeTxt;
    ImageView adamImg;

    String deger;
    //String gosterilecekDeger = "";
    StringBuilder gosterilecekDeger;
    char[] karakterDizisi;

    boolean harfeSahipMi = false;
    int maxHataToleransi = 6;
    int mevcutHata = 0;


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

        harfTahminTxt = findViewById(R.id.harfTahminTxt);
        kelimeTxt = findViewById(R.id.kelimeTxt);
        kelimeTahminTxt = findViewById(R.id.kelimeTahminTxt);
        adamImg = findViewById(R.id.adamImg);


    }

    void registerEventHandlers() {

        harfTahminTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count == 0){
                    return;
                }
                harfeSahipMi(harfTahminTxt.getText().toString());
                kazanildiMi();
                kelimeTxt.setText(gosterilecekDeger);
            }

            @Override
            public void afterTextChanged(Editable s) {


            }
        });

        kelimeTahminTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                girilenKelimeDogruMu();
                kelimeTxt.setText(gosterilecekDeger);


            }
        });
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

    public void harfeSahipMi(String gelen){
        Log.d("sahipMi",gelen);
        for (int i = 0; i < deger.length(); i++) {
            if (gelen.equals(Character.toString(karakterDizisi[i]))){
                gosterilecekDeger = gosterilecekDeger.replace(i*2, i*2+1, Character.toString(karakterDizisi[i]));
                harfeSahipMi = true;
            }
        }
        Log.w("deneme", gosterilecekDeger.toString());
        if (harfeSahipMi == false){
            yanlisHarf();
        }
        harfeSahipMi = false;
    }
    public void girilenKelimeDogruMu(){
        if (deger.equalsIgnoreCase(kelimeTahminTxt.getText().toString())){
            for (int i = 0; i < deger.length(); i++) {
                gosterilecekDeger = gosterilecekDeger.replace(i*2, i*2+1, Character.toString(karakterDizisi[i]));
            }
            kazandildi();
        }

    }
    public void yanlisHarf(){
        mevcutHata++;
        if (mevcutHata == 0){
            adamImg.setImageResource(R.drawable.adam0);

        }
        else if(mevcutHata == 1){
            adamImg.setImageResource(R.drawable.adam1);

        }
        else if(mevcutHata == 2){
            adamImg.setImageResource(R.drawable.adam2);

        }
        else if(mevcutHata == 3){
            adamImg.setImageResource(R.drawable.adam3);

        }
        else if(mevcutHata == 4){
            adamImg.setImageResource(R.drawable.adam4);

        }
        else if(mevcutHata == 5){
            adamImg.setImageResource(R.drawable.adam5);

        }
        else if(mevcutHata == 6){
            adamImg.setImageResource(R.drawable.adam6);

        }

        if (mevcutHata == maxHataToleransi){
            kaybedildi();
        }
    }

    public void kaybedildi(){
        Toast.makeText(this, "Kaybettiniz.", Toast.LENGTH_SHORT).show();
        this.setTitle("Kaybettiniz");
    }
    public void kazanildiMi(){

        int altcizgiKalmisMi = gosterilecekDeger.indexOf("_");
        if (altcizgiKalmisMi == -1){
            kazandildi();
        }

    }
    public void kazandildi(){
        Toast.makeText(this, "Kazandınız.", Toast.LENGTH_SHORT).show();
        this.setTitle("Kazandınız");
    }

    public void BasligiDegistir(){

        this.setTitle("Adam Asmaca");
    }
}