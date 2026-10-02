package com.example.adamasmacaoyunu;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final Locale LOCALE_TR = new Locale("tr", "TR");
    private static final int MAX_HATA_TOLERANSI = 6;

    private static final String[][] KEYBOARD_TR = {
            {"E", "R", "T", "Y", "U", "I", "O", "P", "Ğ", "Ü"},
            {"A", "S", "D", "F", "G", "H", "J", "K", "L", "Ş", "İ"},
            {"Z", "C", "V", "B", "N", "M", "Ö", "Ç"}
    };

    private static final String[][] KEYBOARD_EN = {
            {"Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"},
            {"A", "S", "D", "F", "G", "H", "J", "K", "L"},
            {"Z", "X", "C", "V", "B", "N", "M"}
    };

    private static final String PREF_NAME = "ayarlar";
    private static final String KEY_CATEGORY = "secilen_kategori";

    private static final String[] CATEGORY_KEYS = {
            "all",
            "body_parts",
            "electronic_devices",
            "countries",
            "animals",
            "fruits_vegetables"
    };

    private TextView kelimeTxt;
    private TextView yanlisHarflerTxt;
    private TextView kategoriTxt;
    private TextView ipucuTxt;
    private ImageView adamImg;
    private LinearLayout keyboardLayout;
    private final List<MaterialButton> keyboardButtons = new ArrayList<>();

    private String ayrac;
    private String bulunacakKelime = "";
    private String ipucu = "";
    private String aktifKategori = "all";
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
        FirebaseApp.initializeApp(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

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
        resetKeyboard();
        oyuncuyaGosterilecekMetniGizle();
        oyuncuyaGosterilecekMetniOyuncuyaGoster();
    }

    private void initComponents() {
        yanlisHarfler = new ArrayList<>();
        kelimeTxt = findViewById(R.id.kelimeTxt);
        adamImg = findViewById(R.id.adamImg);
        yanlisHarflerTxt = findViewById(R.id.yanlisHarfler);
        kategoriTxt = findViewById(R.id.kategoriTxt);
        ipucuTxt = findViewById(R.id.ipucuTxt);
        keyboardLayout = findViewById(R.id.keyboardLayout);
        aktifKategori = getSharedPreferences(PREF_NAME, MODE_PRIVATE).getString(KEY_CATEGORY, "all");
        setupKeyboard();
    }

    private void initComponentsFirebase() {
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        } catch (Exception e) {
            Log.w("firebase", "Persistence already enabled or initialization issue", e);
        }
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference("words");
    }

    private void registerEventHandlers() {
    }

    public void bulunacakKelimeyiUret() {
        if (!isNetworkAvailable()) {
            kullanYedekKelime();
            Toast.makeText(MainActivity.this, getString(R.string.baglanti_kurulamadi), Toast.LENGTH_SHORT).show();
            programaDevamEt();
            return;
        }

        String categoryToFetch;
        if ("all".equals(aktifKategori)) {
            Random rand = new Random();
            int catIndex = 1 + rand.nextInt(CATEGORY_KEYS.length - 1);
            categoryToFetch = CATEGORY_KEYS[catIndex];
        } else {
            categoryToFetch = aktifKategori;
        }

        Random random = new Random();
        int index = random.nextInt(10);
        setKeyboardEnabled(false);
        firebaseUzerindenKelimeAl(categoryToFetch, index);
    }

    private void setupKeyboard() {
        if (keyboardLayout == null) {
            return;
        }
        keyboardLayout.removeAllViews();
        keyboardButtons.clear();

        boolean isEn = "en".equals(LocaleHelper.getLanguage(this));
        String[][] layout = isEn ? KEYBOARD_EN : KEYBOARD_TR;
        int totalCols = 0;
        for (String[] row : layout) {
            if (row.length > totalCols) {
                totalCols = row.length;
            }
        }
        int btnHeight = dpToPx(42);
        int marginH = dpToPx(1.5f);

        for (String[] row : layout) {
            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            rowLayout.setGravity(Gravity.CENTER);
            rowLayout.setWeightSum(totalCols);

            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            rowParams.bottomMargin = dpToPx(4);
            rowLayout.setLayoutParams(rowParams);

            float spacerWeight = (totalCols - row.length) / 2.0f;
            if (spacerWeight > 0) {
                View leftSpacer = new View(this);
                leftSpacer.setLayoutParams(new LinearLayout.LayoutParams(0, btnHeight, spacerWeight));
                rowLayout.addView(leftSpacer);
            }

            for (String letter : row) {
                MaterialButton btn = (MaterialButton) getLayoutInflater().inflate(R.layout.keyboard_key, rowLayout, false);
                btn.setText(letter);
                if (!isEn) {
                    btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
                }
                LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                        0,
                        btnHeight,
                        1.0f
                );
                btnParams.leftMargin = marginH;
                btnParams.rightMargin = marginH;
                btn.setLayoutParams(btnParams);

                btn.setOnClickListener(v -> onLetterClicked(letter, btn));
                keyboardButtons.add(btn);
                rowLayout.addView(btn);
            }

            if (spacerWeight > 0) {
                View rightSpacer = new View(this);
                rightSpacer.setLayoutParams(new LinearLayout.LayoutParams(0, btnHeight, spacerWeight));
                rowLayout.addView(rightSpacer);
            }

            keyboardLayout.addView(rowLayout);
        }
    }

    private void onLetterClicked(String letter, MaterialButton btn) {
        btn.setEnabled(false);
        bulunacakKelimeGirilenHarfeSahipMi(letter);
        bilindiMi();
        oyuncuyaGosterilecekMetniOyuncuyaGoster();
    }

    private void resetKeyboard() {
        for (MaterialButton btn : keyboardButtons) {
            btn.setEnabled(true);
        }
    }

    private void setKeyboardEnabled(boolean enabled) {
        for (MaterialButton btn : keyboardButtons) {
            btn.setEnabled(enabled);
        }
    }

    private int dpToPx(float dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    public void firebaseUzerindenKelimeAl(String categoryKey, int index) {
        if (!isNetworkAvailable()) {
            kullanYedekKelime();
            Toast.makeText(MainActivity.this, getString(R.string.baglanti_kurulamadi), Toast.LENGTH_SHORT).show();
            programaDevamEt();
            return;
        }

        String langCode = "en".equals(LocaleHelper.getLanguage(this)) ? "en" : "tr";
        Locale locale = "en".equals(langCode) ? Locale.ENGLISH : LOCALE_TR;

        myRef.child(categoryKey).child(String.valueOf(index)).child(langCode).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                DataSnapshot snapshot = task.getResult();
                String word = snapshot.child("word").getValue(String.class);
                String hint = snapshot.child("hint").getValue(String.class);

                if (word != null && !word.trim().isEmpty()) {
                    bulunacakKelime = word.trim().toLowerCase(locale);
                    ipucu = (hint != null) ? hint : "";
                    guncelleKategoriVeIpucuUI(categoryKey, ipucu);
                    Log.d("firebase", "Kelime başarıyla alındı: " + bulunacakKelime);
                    programaDevamEt();
                    return;
                }
            }

            Log.e("firebase", "Kelime alınamadı veya geçersiz veri döndü", task.getException());
            kullanYedekKelime();
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

    private void kullanYedekKelime() {
        boolean isEn = "en".equals(LocaleHelper.getLanguage(this));
        if (isEn) {
            bulunacakKelime = "apple";
            ipucu = "A round fruit with sweet red or green skin.";
            guncelleKategoriVeIpucuUI("fruits_vegetables", ipucu);
        } else {
            bulunacakKelime = "elma";
            ipucu = "Kırmızı veya yeşil renkte, tatlı ve sulu meyve.";
            guncelleKategoriVeIpucuUI("fruits_vegetables", ipucu);
        }
    }

    private void guncelleKategoriVeIpucuUI(String categoryKey, String hint) {
        if (kategoriTxt != null) {
            kategoriTxt.setText(getString(R.string.kategori_format, getCategoryDisplayName(categoryKey)));
        }
        if (ipucuTxt != null) {
            if (hint != null && !hint.trim().isEmpty()) {
                ipucuTxt.setText(getString(R.string.ipucu_format, hint));
                ipucuTxt.setVisibility(View.VISIBLE);
            } else {
                ipucuTxt.setVisibility(View.GONE);
            }
        }
    }

    private String getCategoryDisplayName(String categoryKey) {
        switch (categoryKey) {
            case "body_parts":
                return getString(R.string.kat_body_parts);
            case "electronic_devices":
                return getString(R.string.kat_electronic_devices);
            case "countries":
                return getString(R.string.kat_countries);
            case "animals":
                return getString(R.string.kat_animals);
            case "fruits_vegetables":
                return getString(R.string.kat_fruits_vegetables);
            default:
                return getString(R.string.kategori_hepsi);
        }
    }

    public void oyuncuyaGosterilecekMetniGizle() {
        oyuncuyaGosterilecekMetin.setLength(0);
        for (int i = 0; i < bulunacakKelime.length(); i++) {
            char c = bulunacakKelime.charAt(i);
            if (c == ' ') {
                oyuncuyaGosterilecekMetin.append("  ");
            } else {
                oyuncuyaGosterilecekMetin.append(ayrac).append(" ");
            }
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
        boolean isEn = "en".equals(LocaleHelper.getLanguage(this));

        for (int i = 0; i < bulunacakKelime.length(); i++) {
            char target = bulunacakKelime.charAt(i);
            boolean eslesti = (target == harf);

            if (!eslesti && isEn) {
                if ((harf == 'i' && target == 'ı') ||
                    (harf == 'c' && target == 'ç') ||
                    (harf == 'g' && target == 'ğ') ||
                    (harf == 'o' && target == 'ö') ||
                    (harf == 's' && target == 'ş') ||
                    (harf == 'u' && target == 'ü')) {
                    eslesti = true;
                }
            }

            if (eslesti) {
                oyuncuyaGosterilecekMetin.replace(i * 2, i * 2 + 1, String.valueOf(target));
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
        Locale locale = "en".equals(LocaleHelper.getLanguage(this)) ? Locale.ENGLISH : LOCALE_TR;
        return girilenHarf.toLowerCase(locale);
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
        finish();
    }

    public void setContentDescriptions() {
        adamImg.setContentDescription(getString(R.string.mevcut_hata_desc, mevcutHata));
        yanlisHarflerTxt.setContentDescription(getString(R.string.bulunmayan_harfler_desc, yanlisHarfler.toString()));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}