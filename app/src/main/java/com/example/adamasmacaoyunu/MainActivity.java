package com.example.adamasmacaoyunu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    TextInputEditText harfTahminTxt;
    TextInputEditText kelimeTahminTxt;
    TextView kelimeTxt;
    TextView yanlisHarflerTxt;
    ImageView adamImg;

    String ayrac;

    String bulunacakKelime;
    String firebasedenAlinanKelime = "s";
    StringBuilder oyuncuyaGosterilecekMetin;
    char[] bulunacakKelimeKarakterDizisi;

    boolean harfeSahipMi = false;
    int maxHataToleransi = 6;
    int mevcutHata = 0;

    ArrayList<Character>yanlisHarfler;

    int puan = 0;

    FirebaseDatabase database;
    DatabaseReference myRef;

    static {
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);

        setContentView(R.layout.activity_main);
        initComponents();
        initComponentsFirebase();
        registerEventHandlers();
        ayrac = getResources().getString(R.string.ayrac);
        oyuncuyaGosterilecekMetin = new StringBuilder(""); // _ _ _ _ _ _

        puaniGuncelle(0);
        bulunacakKelimeyiUret();


    }
    public void programaDevamEt(){
        harfTahminTxt.setEnabled(true);
        kelimeTahminTxt.setEnabled(true);
        karakterDizisiniBoyutlandir();
        karakterDizisineDegerleriAta();
        oyuncuyaGosterilecekMetniGizle();
        oyuncuyaGosterilecekMetniOyuncuyaGoster();
    }

    void initComponents(){
        yanlisHarfler = new ArrayList<Character>();
        harfTahminTxt = findViewById(R.id.harfTahminTxt);
        kelimeTxt = findViewById(R.id.kelimeTxt);
        kelimeTahminTxt = findViewById(R.id.kelimeTahminTxt);
        adamImg = findViewById(R.id.adamImg);
        yanlisHarflerTxt = findViewById(R.id.yanlisHarfler);

    }
    void initComponentsFirebase(){

        database = FirebaseDatabase.getInstance();

        myRef = database.getReference("0");



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
        int index = random.nextInt(81 - 0) + 0;
        Log.w("uyarı", String.valueOf(index));
        harfTahminTxt.setEnabled(false);
        kelimeTahminTxt.setEnabled(false);
        firebaseUzerindenRandomDegerIleKelimeAl(index);


    }
    public void firebaseUzerindenRandomDegerIleKelimeAl(int random){

        myRef.child(String.valueOf(random)).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                    firebasedenAlinanKelime = "bulunamadi";


                }
                else {
                    firebasedenAlinanKelime = String.valueOf(task.getResult().getValue());
                    firebasedenAlinanKelime = firebasedenAlinanKelime.toLowerCase(Locale.ROOT);
                    bulunacakKelime = firebasedenAlinanKelime;
                    Log.d("firebase", bulunacakKelime);
                    programaDevamEt();

                }
            }
        });


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

        yanlisHarflerTxt.setText(yanlisHarfler.toString());
    }

    public void bulunacakKelimeGirilenHarfeSahipMi(String girilenHarf){
        girilenHarf = harfiBicimlendir(girilenHarf);
        for (int i = 0; i < bulunacakKelime.length(); i++) {
            if (girilenHarf.equals(Character.toString(bulunacakKelimeKarakterDizisi[i]))){
                oyuncuyaGosterilecekMetin = oyuncuyaGosterilecekMetin.replace(i*2, i*2+1, Character.toString(bulunacakKelimeKarakterDizisi[i]));
                harfeSahipMi = true;
            }
        }
        Log.w("deneme", oyuncuyaGosterilecekMetin.toString());
        if (harfeSahipMi == false){
            yanlisHarf(girilenHarf);
        }
        harfeSahipMi = false;
    }

    public String harfiBicimlendir(String girilenHarf){
        Log.d("sahipMi",girilenHarf);
        if (girilenHarf.equals("İ")){
            girilenHarf = "i";
        }
        else if (girilenHarf.equals("I")){
            girilenHarf = "ı";

        }
        girilenHarf = girilenHarf.toLowerCase(Locale.ROOT);
        Log.d("sahipMi",girilenHarf);
        return girilenHarf;
    }
    public void girilenKelimeDogruMu(){
        if (bulunacakKelime.equalsIgnoreCase(kelimeTahminTxt.getText().toString())){
            for (int i = 0; i < bulunacakKelime.length(); i++) {
                oyuncuyaGosterilecekMetin = oyuncuyaGosterilecekMetin.replace(i*2, i*2+1, Character.toString(bulunacakKelimeKarakterDizisi[i]));
            }
            bilindi();
        }

    }

    public void yanlisHarf(String girilenHarf){
        Character girilenChar = girilenHarf.charAt(0);
        if (!yanlisHarfler.contains(girilenChar))
            yanlisHarfler.add(girilenHarf.charAt(0));

        Log.i("firebase", yanlisHarfler.toString());

        mevcutHata++;
        resmiIlerlet();
    }
    public void resmiIlerlet(){
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
        kelimeTahminTxt.setText("");
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
        yanlisHarfler.clear();
        resmiIlerlet();
        sonrakiSoru();

    }

    public void sonrakiSoru(){
        bulunacakKelimeyiUret();

    }

    public void sonucAktivitesineGec(){
        Intent intent = new Intent(MainActivity.this, SonucActivity.class);
        String mesaj = getTitle().toString().substring(6);
        Log.i("tag", "mesaj:"+mesaj);
        intent.putExtra("puan", mesaj);
        intent.putExtra("bulunacakKelime", bulunacakKelime);
        startActivity(intent);
    }
}