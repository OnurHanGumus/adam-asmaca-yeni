package com.example.adamasmacaoyunu;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.google.android.material.button.MaterialButton;
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

    private TextView kelimeTxt;
    private TextView yanlisHarflerTxt;
    private ImageView adamImg;
    private LinearLayout keyboardLayout;
    private final List<MaterialButton> keyboardButtons = new ArrayList<>();

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
        resetKeyboard();
        oyuncuyaGosterilecekMetniGizle();
        oyuncuyaGosterilecekMetniOyuncuyaGoster();
    }

    private void initComponents() {
        yanlisHarfler = new ArrayList<>();
        kelimeTxt = findViewById(R.id.kelimeTxt);
        adamImg = findViewById(R.id.adamImg);
        yanlisHarflerTxt = findViewById(R.id.yanlisHarfler);
        keyboardLayout = findViewById(R.id.keyboardLayout);
        setupKeyboard();
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
        setKeyboardEnabled(false);
        firebaseUzerindenRandomDegerIleKelimeAl(index);
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