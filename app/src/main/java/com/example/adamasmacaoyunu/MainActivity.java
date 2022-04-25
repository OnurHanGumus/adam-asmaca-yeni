package com.example.adamasmacaoyunu;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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

    String ayrac;

    String bulunacakKelime;
    StringBuilder oyuncuyaGosterilecekMetin;
    char[] bulunacakKelimeKarakterDizisi;

    boolean harfeSahipMi = false;
    int maxHataToleransi = 6;
    int mevcutHata = 0;

    int puan = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initComponents();
        registerEventHandlers();
        ayrac = getResources().getString(R.string.ayrac);

        oyuncuyaGosterilecekMetin = new StringBuilder(""); // _ _ _ _ _ _
        puaniGuncelle(0);
        bulunacakKelimeyiUret();
        karakterDizisiniBoyutlandir();
        karakterDizisineDegerleriAta();
        oyuncuyaGosterilecekMetniGizle();
        oyuncuyaGosterilecekMetniOyuncuyaGoster();
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
                bulunacakKelimeGirilenHarfeSahipMi(harfTahminTxt.getText().toString());
                bilindiMi();
                harfTahminTxt.setText("");
                oyuncuyaGosterilecekMetniOyuncuyaGoster();
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
                oyuncuyaGosterilecekMetniOyuncuyaGoster();
            }
        });
    }
    public void bulunacakKelimeyiUret(){
        Random random = new Random();
        int index = random.nextInt(5);
        Log.w("uyarı", Integer.toString(index));
        bulunacakKelime = "istanbul";

    }
    public void karakterDizisiniBoyutlandir(){
        bulunacakKelimeKarakterDizisi = new char[bulunacakKelime.length()];
    }

    public void karakterDizisineDegerleriAta(){
        for (int i = 0; i < bulunacakKelime.length(); i++) {
            bulunacakKelimeKarakterDizisi[i] = bulunacakKelime.charAt(i);
        }
       Log.d("selam", Character.toString(bulunacakKelimeKarakterDizisi[0]));
    }

    public void oyuncuyaGosterilecekMetniGizle(){
        oyuncuyaGosterilecekMetin.delete(0, oyuncuyaGosterilecekMetin.length());
        for (int i = 0; i < bulunacakKelime.length(); i++) {
            oyuncuyaGosterilecekMetin.append(ayrac + " ");
        }
    }

    public void oyuncuyaGosterilecekMetniOyuncuyaGoster(){
        kelimeTxt.setText(oyuncuyaGosterilecekMetin);
    }

    public void bulunacakKelimeGirilenHarfeSahipMi(String girilenHarf){
        Log.d("sahipMi",girilenHarf);
        for (int i = 0; i < bulunacakKelime.length(); i++) {
            if (girilenHarf.equals(Character.toString(bulunacakKelimeKarakterDizisi[i]))){
                oyuncuyaGosterilecekMetin = oyuncuyaGosterilecekMetin.replace(i*2, i*2+1, Character.toString(bulunacakKelimeKarakterDizisi[i]));
                harfeSahipMi = true;
            }
        }
        Log.w("deneme", oyuncuyaGosterilecekMetin.toString());
        if (harfeSahipMi == false){
            yanlisHarf();
        }
        harfeSahipMi = false;
    }
    public void girilenKelimeDogruMu(){
        if (bulunacakKelime.equalsIgnoreCase(kelimeTahminTxt.getText().toString())){
            for (int i = 0; i < bulunacakKelime.length(); i++) {
                oyuncuyaGosterilecekMetin = oyuncuyaGosterilecekMetin.replace(i*2, i*2+1, Character.toString(bulunacakKelimeKarakterDizisi[i]));
            }
            bilindi();
        }

    }

    public void yanlisHarf(){
        mevcutHata++;
        resmiIlerlet();
    }
    public void resmiIlerlet(){
        if (mevcutHata == 0){
            adamImg.setImageResource(R.drawable.adam1);

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
            bilinemedi();
        }
    }

    public void bilinemedi(){
        Toast.makeText(this, "Kaybettiniz.", Toast.LENGTH_SHORT).show();
        sonucAktivitesineGec();
    }
    public void bilindiMi(){

        int altcizgiKalmisMi = oyuncuyaGosterilecekMetin.indexOf(ayrac);
        if (altcizgiKalmisMi == -1){
            bilindi();
        }

    }
    public void bilindi(){
        //Toast.makeText(this, "Kazandınız.", Toast.LENGTH_SHORT).show();
        //this.setTitle("Kazandınız");
        puaniGuncelle(5);
        sonrakiSeviye();
    }

    public void puaniGuncelle(int eklenecekPuan){
        puan += eklenecekPuan;
        this.setTitle("Puan: " + puan);
    }

    //----------------sonraki----------------
    public  void sonrakiSeviye(){
        mevcutHata = 0;
        resmiIlerlet();
        sonrakiSoru();
        karakterDizisiniBoyutlandir();
        karakterDizisineDegerleriAta();
        oyuncuyaGosterilecekMetniGizle();
        oyuncuyaGosterilecekMetniOyuncuyaGoster();
    }

    public void sonrakiSoru(){
        bulunacakKelime = "sakarya";

    }

    public void sonucAktivitesineGec(){
        Intent intent = new Intent(MainActivity.this, SonucActivity.class);
        String mesaj = getTitle().toString().substring(6);
        Log.i("tag", "mesaj:"+mesaj);
        intent.putExtra("puan", mesaj);
        startActivity(intent);
    }
}