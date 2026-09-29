package com.example.adamasmacaoyunu;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final Locale LOCALE_TR = new Locale("tr", "TR");
    private static final int MAX_HATA_TOLERANSI = 6;

    private TextInputEditText harfTahminTxt;
    private TextInputEditText kelimeTahminTxt;
    private TextView kelimeTxt;
    private TextView yanlisHarflerTxt;
    private ImageView adamImg;

    private String ayrac;
    private String bulunacakKelime = "";
    private StringBuilder oyuncuyaGosterilecekMetin;

    private int mevcutHata = 0;
    private int puan = 0;

    private final int[] adamResimleri = {
            R.drawable.adam0,
            R.drawable.adam1,
            R.drawable.adam2,
            R.drawable.adam3,
            R.drawable.adam4,
            R.drawable.adam5,
            R.drawable.adam6
    };

    private List<Character> yanlisHarfler;
    private DatabaseReference myRef;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);

        setContentView(R.layout.activity_main);
        initComponents();
        initComponentsFirebase();
        registerEventHandlers();

        ayrac = getResources().getString(R.string.ayrac);
        oyuncuyaGosterilecekMetin = new StringBuilder();

        puaniGuncelle(0);
        bulunacakKelimeyiUret();
    }

    public void programaDevamEt() {
        setContentDescriptions();
        harfTahminTxt.setEnabled(true);
        kelimeTahminTxt.setEnabled(true);
        oyuncuyaGosterilecekMetniGizle();
        oyuncuyaGosterilecekMetniOyuncuyaGoster();
    }

    private void initComponents() {
        yanlisHarfler = new ArrayList<>();
        harfTahminTxt = findViewById(R.id.harfTahminTxt);
        kelimeTxt = findViewById(R.id.kelimeTxt);
        kelimeTahminTxt = findViewById(R.id.kelimeTahminTxt);
        adamImg = findViewById(R.id.adamImg);
        yanlisHarflerTxt = findViewById(R.id.yanlisHarfler);
    }

    private void initComponentsFirebase() {
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        } catch (Exception e) {
            Log.w("firebase", "Persistence already enabled or initialization issue", e);
        }
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference("0");
    }

    private void registerEventHandlers() {
        harfTahminTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count == 0 || harfTahminTxt.getText() == null) {
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
                girilenKelimeDogruMu();
                oyuncuyaGosterilecekMetniOyuncuyaGoster();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    public void bulunacakKelimeyiUret() {
        if (!isNetworkAvailable()) {
            bulunacakKelime = getYedekKelime();
            Toast.makeText(MainActivity.this, getString(R.string.baglanti_kurulamadi), Toast.LENGTH_SHORT).show();
            programaDevamEt();
            return;
        }

        Random random = new Random();
        int index = random.nextInt(81);
        harfTahminTxt.setEnabled(false);
        kelimeTahminTxt.setEnabled(false);
        firebaseUzerindenRandomDegerIleKelimeAl(index);
    }

    public void firebaseUzerindenRandomDegerIleKelimeAl(int random) {
        if (!isNetworkAvailable()) {
            bulunacakKelime = getYedekKelime();
            Toast.makeText(MainActivity.this, getString(R.string.baglanti_kurulamadi), Toast.LENGTH_SHORT).show();
            programaDevamEt();
            return;
        }

        myRef.child(String.valueOf(random)).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists() && task.getResult().getValue() != null) {
                String kelime = String.valueOf(task.getResult().getValue()).trim().toLowerCase(LOCALE_TR);
                if (!kelime.isEmpty() && !kelime.equals("null")) {
                    bulunacakKelime = kelime;
                    Log.d("firebase", "Kelime başarıyla alındı: " + bulunacakKelime);
                    programaDevamEt();
                    return;
                }
            }

            Log.e("firebase", "Kelime alınamadı veya geçersiz veri döndü", task.getException());
            bulunacakKelime = getYedekKelime();
            Toast.makeText(MainActivity.this, getString(R.string.baglanti_kurulamadi), Toast.LENGTH_SHORT).show();
            programaDevamEt();
        });

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Network network = connectivityManager.getActiveNetwork();
                if (network != null) {
                    NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
                    return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
                }
            } else {
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                return networkInfo != null && networkInfo.isConnected();
            }
        }
        return false;
    }

    private String getYedekKelime() {
        String[] yedekKelimeler = {"ankara", "istanbul", "izmir", "bursa", "antalya", "trabzon", "adana", "konya", "eskisehir"};
        Random random = new Random();
        return yedekKelimeler[random.nextInt(yedekKelimeler.length)];
    }

    public void oyuncuyaGosterilecekMetniGizle() {
        oyuncuyaGosterilecekMetin.setLength(0);
        for (int i = 0; i < bulunacakKelime.length(); i++) {
            oyuncuyaGosterilecekMetin.append(ayrac).append(" ");
        }
    }

    public void oyuncuyaGosterilecekMetniOyuncuyaGoster() {
        kelimeTxt.setText(oyuncuyaGosterilecekMetin.toString());
        yanlisHarflerTxt.setText(yanlisHarfler.toString());
        setContentDescriptions();
    }

    public void bulunacakKelimeGirilenHarfeSahipMi(String girilenHarf) {
        girilenHarf = harfiBicimlendir(girilenHarf);
        if (girilenHarf.isEmpty()) {
            return;
        }

        char harf = girilenHarf.charAt(0);
        boolean harfBulundu = false;

        for (int i = 0; i < bulunacakKelime.length(); i++) {
            if (bulunacakKelime.charAt(i) == harf) {
                oyuncuyaGosterilecekMetin.replace(i * 2, i * 2 + 1, String.valueOf(harf));
                harfBulundu = true;
            }
        }

        if (!harfBulundu) {
            yanlisHarf(String.valueOf(harf));
        }
    }

    public String harfiBicimlendir(String girilenHarf) {
        if (girilenHarf == null || girilenHarf.isEmpty()) {
            return "";
        }
        return girilenHarf.toLowerCase(LOCALE_TR);
    }

    public void girilenKelimeDogruMu() {
        String girilen = kelimeTahminTxt.getText() != null ? kelimeTahminTxt.getText().toString() : "";
        if (!girilen.isEmpty() && girilen.equalsIgnoreCase(bulunacakKelime)) {
            for (int i = 0; i < bulunacakKelime.length(); i++) {
                char harf = bulunacakKelime.charAt(i);
                oyuncuyaGosterilecekMetin.replace(i * 2, i * 2 + 1, String.valueOf(harf));
            }
            bilindi();
        }
    }

    public void yanlisHarf(String girilenHarf) {
        if (girilenHarf == null || girilenHarf.isEmpty()) {
            return;
        }
        Character girilenChar = girilenHarf.charAt(0);
        if (!yanlisHarfler.contains(girilenChar)) {
            yanlisHarfler.add(girilenChar);
        }

        mevcutHata++;
        resmiIlerlet();
    }

    public void resmiIlerlet() {
        if (mevcutHata >= 0 && mevcutHata < adamResimleri.length) {
            adamImg.setImageResource(adamResimleri[mevcutHata]);
        }

        if (mevcutHata >= MAX_HATA_TOLERANSI) {
            bilinemedi();
        }
    }

    public void bilinemedi() {
        Toast.makeText(this, getString(R.string.kaybettiniz), Toast.LENGTH_SHORT).show();
        sonucAktivitesineGec();
    }

    public void bilindiMi() {
        int altcizgiKalmisMi = oyuncuyaGosterilecekMetin.indexOf(ayrac);
        if (altcizgiKalmisMi == -1) {
            bilindi();
        }
    }

    public void bilindi() {
        kelimeTahminTxt.setText("");
        puaniGuncelle(5);
        sonrakiSeviye();
    }

    public void puaniGuncelle(int eklenecekPuan) {
        puan += eklenecekPuan;
        this.setTitle(getString(R.string.puan_format, puan));
    }

    public void sonrakiSeviye() {
        mevcutHata = 0;
        yanlisHarfler.clear();
        resmiIlerlet();
        sonrakiSoru();
    }

    public void sonrakiSoru() {
        bulunacakKelimeyiUret();
    }

    public void sonucAktivitesineGec() {
        Intent intent = new Intent(MainActivity.this, SonucActivity.class);
        intent.putExtra("puan", puan);
        intent.putExtra("bulunacakKelime", bulunacakKelime);
        startActivity(intent);
    }

    public void setContentDescriptions() {
        adamImg.setContentDescription(getString(R.string.mevcut_hata_desc, mevcutHata));
        yanlisHarflerTxt.setContentDescription(getString(R.string.bulunmayan_harfler_desc, yanlisHarfler.toString()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_lang_tr) {
            diliDegistir("tr");
            return true;
        } else if (id == R.id.action_lang_en) {
            diliDegistir("en");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void diliDegistir(String languageCode) {
        LocaleHelper.setLocale(this, languageCode);
        recreate();
    }
}