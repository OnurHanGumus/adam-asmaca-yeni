package com.OnurHan.hangingman;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
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
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final Locale LOCALE_TR = new Locale("tr", "TR");
    private static final int MAX_HATA_TOLERANSI = 6;
    private static final int HEDEF_TUR = 1; // Both Practice and Main Game Level are 1 word per round

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

    private static final String[][] YEDEK_KELIMELER_TR = {
            {"elma", "Kırmızı veya yeşil renkte, tatlı ve sulu meyve.", "fruits_vegetables"},
            {"kedi", "Evcil ve sevimli, miyavlayan hayvan.", "animals"},
            {"telefon", "İletişim ve internet için kullandığımız taşınabilir cihaz.", "electronic_devices"},
            {"turkiye", "Asya ile Avrupa arasında köprü olan ülke.", "countries"},
            {"burun", "Koku almamızı sağlayan duyu organı.", "body_parts"}
    };

    private static final String[][] YEDEK_KELIMELER_EN = {
            {"apple", "A round fruit with sweet red or green skin.", "fruits_vegetables"},
            {"cat", "A small domesticated carnivorous mammal.", "animals"},
            {"phone", "A portable electronic device for communication.", "electronic_devices"},
            {"turkey", "A country situated between Europe and Asia.", "countries"},
            {"nose", "The organ used for smelling and breathing.", "body_parts"}
    };

    private static final String PREF_NAME = "ayarlar";
    private static final String KEY_CATEGORY = "secilen_kategori";

    private static final String[] CATEGORY_KEYS = {
            "all",
            "body_parts",
            "electronic_devices",
            "countries",
            "animals",
            "fruits_vegetables",
            "food",
            "sports",
            "vehicles",
            "professions",
            "space",
            "fantastic_elements",
            "musical_instruments",
            "superheroes",
            "weather"
    };

    private GameMode gameMode = GameMode.MAIN_GAME;
    private int currentLevel = 1;

    private TextView modVeSeviyeTxt;
    private TextView gameCanTxt;
    private TextView gameCoinTxt;
    private TextView kelimeTxt;
    private TextView yanlisHarflerTxt;
    private TextView kategoriTxt;
    private TextView ipucuTxt;
    private MaterialButton btnIpucuAl;
    private MaterialButton btnHarfAc;
    private ImageView adamImg;
    private LinearLayout keyboardLayout;
    private final List<MaterialButton> keyboardButtons = new ArrayList<>();

    private String ayrac;
    private String bulunacakKelime = "";
    private String ipucu = "";
    private String aktifKategori = "all";
    private boolean ipucuAcikMi = false;
    private StringBuilder oyuncuyaGosterilecekMetin;

    private int mevcutHata = 0;
    private int puan = 0;

    private int mevcutTur = 1;
    private int cozulenKelimeSayisi = 0;
    private int kusursuzKelimeSayisi = 0;
    private int komboSerisi = 0;
    private boolean roundGecisiYapiliyor = false;
    private final List<Integer> kullanilabilirIndeksler = new ArrayList<>();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Vibrator vibrator;

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

        String modeExtra = getIntent().getStringExtra(MainMenuActivity.EXTRA_GAME_MODE);
        if (modeExtra != null) {
            try {
                gameMode = GameMode.valueOf(modeExtra);
            } catch (Exception e) {
                gameMode = GameMode.MAIN_GAME;
            }
        }
        currentLevel = ProgressionManager.getLevel(this);

        setContentView(R.layout.activity_main);
        initComponents();
        initComponentsFirebase();
        registerEventHandlers();

        ayrac = getResources().getString(R.string.ayrac);
        oyuncuyaGosterilecekMetin = new StringBuilder();

        puaniGuncelle(0);
        guncelleCanVeAltinUI();
        bulunacakKelimeyiUret();
    }

    public void programaDevamEt() {
        roundGecisiYapiliyor = false;
        setContentDescriptions();
        resetKeyboard();
        oyuncuyaGosterilecekMetniGizle();
        oyuncuyaGosterilecekMetniOyuncuyaGoster();
    }

    private void initComponents() {
        yanlisHarfler = new ArrayList<>();
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        modVeSeviyeTxt = findViewById(R.id.modVeSeviyeTxt);
        gameCanTxt = findViewById(R.id.gameCanTxt);
        gameCoinTxt = findViewById(R.id.gameCoinTxt);
        kelimeTxt = findViewById(R.id.kelimeTxt);
        adamImg = findViewById(R.id.adamImg);
        yanlisHarflerTxt = findViewById(R.id.yanlisHarfler);
        kategoriTxt = findViewById(R.id.kategoriTxt);
        ipucuTxt = findViewById(R.id.ipucuTxt);
        btnIpucuAl = findViewById(R.id.btnIpucuAl);
        btnHarfAc = findViewById(R.id.btnHarfAc);
        keyboardLayout = findViewById(R.id.keyboardLayout);

        if (gameMode == GameMode.MAIN_GAME) {
            modVeSeviyeTxt.setText(getString(R.string.seviye_format, currentLevel));
            gameCanTxt.setVisibility(View.VISIBLE);
        } else {
            modVeSeviyeTxt.setText(R.string.pratik_modu_etiket);
            gameCanTxt.setVisibility(View.GONE);
        }

        btnIpucuAl.setText(getString(R.string.btn_ipucu_al_format, CurrencyManager.HINT_COST));
        btnHarfAc.setText(getString(R.string.btn_harf_ac_format, CurrencyManager.REVEAL_LETTER_COST));

        aktifKategori = getSharedPreferences(PREF_NAME, MODE_PRIVATE).getString(KEY_CATEGORY, "all");
        indeksleriYenile();
        setupKeyboard();
    }

    private void indeksleriYenile() {
        kullanilabilirIndeksler.clear();
        for (int i = 0; i < 10; i++) {
            kullanilabilirIndeksler.add(i);
        }
        Collections.shuffle(kullanilabilirIndeksler);
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
        btnIpucuAl.setOnClickListener(v -> ipucuAc());
        btnHarfAc.setOnClickListener(v -> rastgeleHarfAc());
    }

    private void ipucuAc() {
        if (roundGecisiYapiliyor) {
            return;
        }

        if (ipucuAcikMi) {
            Toast.makeText(this, R.string.ipucu_zaten_acik, Toast.LENGTH_SHORT).show();
            return;
        }

        if (ipucu == null || ipucu.trim().isEmpty()) {
            Toast.makeText(this, R.string.ipucu_yok, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!CurrencyManager.spendCoins(this, CurrencyManager.HINT_COST)) {
            Toast.makeText(this, getString(R.string.yetersiz_altin_format, CurrencyManager.HINT_COST), Toast.LENGTH_SHORT).show();
            return;
        }

        ipucuAcikMi = true;
        if (gameMode == GameMode.MAIN_GAME) {
            ProgressionManager.setHintRevealed(this, true);
        }

        guncelleCanVeAltinUI();
        guncelleKategoriVeIpucuUI(aktifKategori, ipucu);
        Toast.makeText(this, R.string.ipucu_acik, Toast.LENGTH_SHORT).show();
    }

    private void rastgeleHarfAc() {
        if (roundGecisiYapiliyor) {
            return;
        }

        List<Character> unrevealed = new ArrayList<>();
        for (int i = 0; i < bulunacakKelime.length(); i++) {
            char target = bulunacakKelime.charAt(i);
            if (target != ' ') {
                if (i * 2 < oyuncuyaGosterilecekMetin.length() &&
                        oyuncuyaGosterilecekMetin.charAt(i * 2) == ayrac.charAt(0)) {
                    if (!unrevealed.contains(target)) {
                        unrevealed.add(target);
                    }
                }
            }
        }

        if (unrevealed.isEmpty()) {
            Toast.makeText(this, R.string.tum_harfler_acik, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!CurrencyManager.spendCoins(this, CurrencyManager.REVEAL_LETTER_COST)) {
            Toast.makeText(this, getString(R.string.yetersiz_altin_format, CurrencyManager.REVEAL_LETTER_COST), Toast.LENGTH_SHORT).show();
            return;
        }

        guncelleCanVeAltinUI();

        char chosenChar = unrevealed.get(new Random().nextInt(unrevealed.size()));

        for (int i = 0; i < bulunacakKelime.length(); i++) {
            if (bulunacakKelime.charAt(i) == chosenChar) {
                oyuncuyaGosterilecekMetin.replace(i * 2, i * 2 + 1, String.valueOf(chosenChar));
            }
        }

        Locale locale = "en".equals(LocaleHelper.getLanguage(this)) ? Locale.ENGLISH : LOCALE_TR;
        String chosenUpper = String.valueOf(chosenChar).toUpperCase(locale);
        for (MaterialButton btn : keyboardButtons) {
            if (btn.getText() != null && btn.getText().toString().toUpperCase(locale).equals(chosenUpper)) {
                btn.setEnabled(false);
                break;
            }
        }

        titret(60);
        oyuncuyaGosterilecekMetniOyuncuyaGoster();
        Toast.makeText(this, getString(R.string.harf_acildi_format, chosenUpper), Toast.LENGTH_SHORT).show();
        bilindiMi();
    }

    private void guncelleCanVeAltinUI() {
        int coins = CurrencyManager.getCoins(this);
        if (gameCoinTxt != null) {
            gameCoinTxt.setText(getString(R.string.altin_format, coins));
        }

        if (gameCanTxt != null) {
            if (gameMode == GameMode.MAIN_GAME) {
                int lives = LifeManager.getLives(this);
                gameCanTxt.setText(getString(R.string.can_tam_format, lives, LifeManager.MAX_LIVES));
                gameCanTxt.setVisibility(View.VISIBLE);
            } else {
                gameCanTxt.setVisibility(View.GONE);
            }
        }

        if (btnIpucuAl != null) {
            if (ipucuAcikMi) {
                btnIpucuAl.setText(R.string.ipucu_acik);
                btnIpucuAl.setEnabled(false);
            } else {
                btnIpucuAl.setText(getString(R.string.btn_ipucu_al_format, CurrencyManager.HINT_COST));
                btnIpucuAl.setEnabled(true);
            }
        }
    }

    public void bulunacakKelimeyiUret() {
        // Main Game Mode: Check if there's already a saved word from a previous failure
        if (gameMode == GameMode.MAIN_GAME && ProgressionManager.hasSavedWord(this)) {
            bulunacakKelime = ProgressionManager.getSavedWord(this);
            ipucu = ProgressionManager.getSavedHint(this);
            aktifKategori = ProgressionManager.getSavedCategory(this);
            ipucuAcikMi = ProgressionManager.isHintRevealed(this);
            guncelleKategoriVeIpucuUI(aktifKategori, ipucu);
            programaDevamEt();
            return;
        }

        // New word needed
        ipucuAcikMi = false;

        if (!isNetworkAvailable()) {
            kullanYedekKelime();
            Toast.makeText(MainActivity.this, getString(R.string.baglanti_kurulamadi), Toast.LENGTH_SHORT).show();
            programaDevamEt();
            return;
        }

        String categoryToFetch;
        int index;

        if (gameMode == GameMode.MAIN_GAME) {
            // Main Game Mode cycles through categories and indices based on level
            int catIndex = 1 + ((currentLevel - 1) % (CATEGORY_KEYS.length - 1));
            categoryToFetch = CATEGORY_KEYS[catIndex];
            index = ((currentLevel - 1) / (CATEGORY_KEYS.length - 1)) % 10;
        } else {
            // Practice Mode uses selected category
            if ("all".equals(aktifKategori)) {
                Random rand = new Random();
                int catIndex = 1 + rand.nextInt(CATEGORY_KEYS.length - 1);
                categoryToFetch = CATEGORY_KEYS[catIndex];
            } else {
                categoryToFetch = aktifKategori;
            }
            if (kullanilabilirIndeksler.isEmpty()) {
                indeksleriYenile();
            }
            index = kullanilabilirIndeksler.remove(0);
        }

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
        if (roundGecisiYapiliyor) {
            return;
        }
        btn.setEnabled(false);
        bulunacakKelimeGirilenHarfeSahipMi(letter);
        oyuncuyaGosterilecekMetniOyuncuyaGoster();
        bilindiMi();
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
                    aktifKategori = categoryKey;

                    if (gameMode == GameMode.MAIN_GAME) {
                        ProgressionManager.saveCurrentLevelWord(MainActivity.this, bulunacakKelime, ipucu, aktifKategori);
                    }

                    guncelleKategoriVeIpucuUI(aktifKategori, ipucu);
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
        String[][] yedekHavuzu = isEn ? YEDEK_KELIMELER_EN : YEDEK_KELIMELER_TR;
        int yedekIndex = (gameMode == GameMode.MAIN_GAME)
                ? (currentLevel - 1) % yedekHavuzu.length
                : (mevcutTur - 1) % yedekHavuzu.length;

        bulunacakKelime = yedekHavuzu[yedekIndex][0];
        ipucu = yedekHavuzu[yedekIndex][1];
        aktifKategori = yedekHavuzu[yedekIndex][2];

        if (gameMode == GameMode.MAIN_GAME) {
            ProgressionManager.saveCurrentLevelWord(MainActivity.this, bulunacakKelime, ipucu, aktifKategori);
        }

        guncelleKategoriVeIpucuUI(aktifKategori, ipucu);
    }

    private void guncelleKategoriVeIpucuUI(String categoryKey, String hint) {
        if (kategoriTxt != null) {
            kategoriTxt.setText(getString(R.string.kategori_format, getCategoryDisplayName(categoryKey)));
        }
        if (ipucuTxt != null) {
            if (ipucuAcikMi && hint != null && !hint.trim().isEmpty()) {
                ipucuTxt.setText(getString(R.string.ipucu_format, hint));
                ipucuTxt.setVisibility(View.VISIBLE);
            } else {
                ipucuTxt.setVisibility(View.GONE);
            }
        }
        if (btnIpucuAl != null) {
            if (ipucuAcikMi) {
                btnIpucuAl.setText(R.string.ipucu_acik);
                btnIpucuAl.setEnabled(false);
            } else {
                btnIpucuAl.setText(getString(R.string.btn_ipucu_al_format, CurrencyManager.HINT_COST));
                btnIpucuAl.setEnabled(true);
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
            case "food":
                return getString(R.string.kat_food);
            case "sports":
                return getString(R.string.kat_sports);
            case "vehicles":
                return getString(R.string.kat_vehicles);
            case "professions":
                return getString(R.string.kat_professions);
            case "space":
                return getString(R.string.kat_space);
            case "fantastic_elements":
                return getString(R.string.kat_fantastic_elements);
            case "musical_instruments":
                return getString(R.string.kat_musical_instruments);
            case "superheroes":
                return getString(R.string.kat_superheroes);
            case "weather":
                return getString(R.string.kat_weather);
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

        if (harfBulundu) {
            titret(40);
        } else {
            komboSerisi = 0;
            titret(150);
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
        titretUzun();

        if (gameMode == GameMode.MAIN_GAME) {
            LifeManager.loseLife(this);
            Toast.makeText(this, getString(R.string.seviye_kayip_mesaj, LifeManager.getLives(this)), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.kaybettiniz), Toast.LENGTH_SHORT).show();
        }

        guncelleCanVeAltinUI();
        sonucAktivitesineGec(false);
    }

    public void bilindiMi() {
        int altcizgiKalmisMi = oyuncuyaGosterilecekMetin.indexOf(ayrac);
        if (altcizgiKalmisMi == -1) {
            bilindi();
        }
    }

    public void bilindi() {
        roundGecisiYapiliyor = true;
        setKeyboardEnabled(false);

        // Tam kelimeyi ekranda eksiksiz göster
        oyuncuyaGosterilecekMetin.setLength(0);
        for (int i = 0; i < bulunacakKelime.length(); i++) {
            char c = bulunacakKelime.charAt(i);
            if (c == ' ') {
                oyuncuyaGosterilecekMetin.append("  ");
            } else {
                oyuncuyaGosterilecekMetin.append(c).append(" ");
            }
        }
        oyuncuyaGosterilecekMetniOyuncuyaGoster();

        // Dinamik Puan Hesaplama
        int harfSayisi = bulunacakKelime.replace(" ", "").length();
        int tabanPuan = harfSayisi * 10;
        int canBonusu = (MAX_HATA_TOLERANSI - mevcutHata) * 5;
        boolean kusursuz = (mevcutHata == 0);
        int kusursuzBonus = kusursuz ? 25 : 0;
        if (kusursuz) {
            kusursuzKelimeSayisi++;
        }

        komboSerisi++;
        int komboBonus = (komboSerisi > 1) ? (komboSerisi * 10) : 0;
        int toplamKazanilan = tabanPuan + canBonusu + kusursuzBonus + komboBonus;

        cozulenKelimeSayisi++;
        puaniGuncelle(toplamKazanilan);

        titretCiftDarbe();

        if (gameMode == GameMode.MAIN_GAME) {
            CurrencyManager.addCoins(this, CurrencyManager.LEVEL_WIN_REWARD);
            ProgressionManager.advanceLevel(this);
            Toast.makeText(this, getString(R.string.seviye_zafer_mesaj, CurrencyManager.LEVEL_WIN_REWARD), Toast.LENGTH_SHORT).show();
        } else {
            String tebrikMesaji = kusursuz
                    ? getString(R.string.tebrikler_kusursuz, toplamKazanilan)
                    : getString(R.string.tebrikler_puan, toplamKazanilan);
            if (komboSerisi > 1) {
                tebrikMesaji += " • " + getString(R.string.kombo_ekstra, komboBonus);
            }
            Toast.makeText(this, tebrikMesaji, Toast.LENGTH_SHORT).show();
        }

        guncelleCanVeAltinUI();
        handler.postDelayed(this::zaferKazanildi, 1300);
    }

    public void zaferKazanildi() {
        sonucAktivitesineGec(true);
    }

    public void puaniGuncelle(int eklenecekPuan) {
        puan += eklenecekPuan;
        if (gameMode == GameMode.MAIN_GAME) {
            this.setTitle(getString(R.string.seviye_format, currentLevel) + " • " + getString(R.string.puan_format, puan));
        } else {
            this.setTitle(getString(R.string.pratik_modu_etiket) + " • " + getString(R.string.puan_format, puan));
        }
    }

    public void sonucAktivitesineGec(boolean kazandi) {
        Intent intent = new Intent(MainActivity.this, SonucActivity.class);
        intent.putExtra("gameMode", gameMode.name());
        intent.putExtra("kazandi", kazandi);
        intent.putExtra("puan", puan);
        intent.putExtra("bulunacakKelime", bulunacakKelime);
        intent.putExtra("cozulenKelime", cozulenKelimeSayisi);
        intent.putExtra("hedefTur", HEDEF_TUR);
        intent.putExtra("kusursuzSayisi", kusursuzKelimeSayisi);
        intent.putExtra("seviye", currentLevel);
        intent.putExtra("kalanCan", LifeManager.getLives(this));
        intent.putExtra("kazanilanAltin", (kazandi && gameMode == GameMode.MAIN_GAME) ? CurrencyManager.LEVEL_WIN_REWARD : 0);
        startActivity(intent);
        finish();
    }

    private void titret(long millis) {
        if (vibrator == null) return;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(millis);
            }
        } catch (Exception ignored) {
        }
    }

    private void titretCiftDarbe() {
        if (vibrator == null) return;
        try {
            long[] timings = {0, 80, 80, 140};
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                int[] amplitudes = {0, VibrationEffect.DEFAULT_AMPLITUDE, 0, VibrationEffect.DEFAULT_AMPLITUDE};
                vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1));
            } else {
                vibrator.vibrate(timings, -1);
            }
        } catch (Exception ignored) {
        }
    }

    private void titretUzun() {
        if (vibrator == null) return;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(400, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(400);
            }
        } catch (Exception ignored) {
        }
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

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}