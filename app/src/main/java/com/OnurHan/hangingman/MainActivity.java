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
import android.util.Log;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final Locale LOCALE_TR = new Locale("tr", "TR");
    private static final int MAX_HATA_TOLERANSI = 6;
    private static final int DRAWING_CROSSFADE_DURATION_MS = 200;

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

    // onSaveInstanceState keys (restores the round after rotation / theme change / process death)
    private static final String STATE_SEVIYE = "state_seviye";
    private static final String STATE_KELIME = "state_kelime";
    private static final String STATE_IPUCU = "state_ipucu";
    private static final String STATE_KATEGORI = "state_kategori";
    private static final String STATE_IPUCU_ACIK = "state_ipucu_acik";
    private static final String STATE_GOSTERILEN_METIN = "state_gosterilen_metin";
    private static final String STATE_YANLIS_HARFLER = "state_yanlis_harfler";
    private static final String STATE_HATA = "state_hata";
    private static final String STATE_BASILAN_TUSLAR = "state_basilan_tuslar";
    private static final String STATE_INDEKSLER = "state_indeksler";
    private static final String STATE_ROUND_GECISI = "state_round_gecisi";

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
    // Lowercase letters the player can produce with the active keyboard
    private final Set<Character> tahminEdilebilirHarfler = new HashSet<>();

    private String ayrac;
    private String bulunacakKelime = "";
    private String ipucu = "";
    private String aktifKategori = CategoryManager.CATEGORY_ALL;
    private boolean ipucuAcikMi = false;
    private StringBuilder oyuncuyaGosterilecekMetin;

    private int mevcutHata = 0;

    private boolean roundGecisiYapiliyor = false;
    private final List<Integer> kullanilabilirIndeksler = new ArrayList<>();
    private final Handler handler = new Handler(Looper.getMainLooper());

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
        // After a win the stored level is already advanced, so a restored round keeps its own level
        currentLevel = (savedInstanceState != null)
                ? savedInstanceState.getInt(STATE_SEVIYE, ProgressionManager.getLevel(this))
                : ProgressionManager.getLevel(this);

        setContentView(R.layout.activity_main);
        initComponents();
        initComponentsFirebase();
        registerEventHandlers();

        ayrac = getResources().getString(R.string.ayrac);
        oyuncuyaGosterilecekMetin = new StringBuilder();

        basligiGuncelle();
        guncelleCanVeAltinUI();
        if (!oyunDurumunuGeriYukle(savedInstanceState)) {
            bulunacakKelimeyiUret();
        }
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

        aktifKategori = CategoryManager.getSelectedCategory(this);
        ipucuAcikMi = (gameMode == GameMode.MAIN_GAME) && ProgressionManager.isHintRevealed(this);
        indeksleriYenile();
        setupKeyboard();
        SoundManager.init(this);
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
        ipucuAcikMi = (gameMode == GameMode.MAIN_GAME) && ProgressionManager.isHintRevealed(this);

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
            categoryToFetch = CategoryManager.getMainGameCategoryForLevel(currentLevel);
            index = CategoryManager.getMainGameIndexForLevel(currentLevel);
        } else {
            // Practice Mode uses selected category
            if (CategoryManager.CATEGORY_ALL.equals(aktifKategori)) {
                Random rand = new Random();
                int catIndex = 1 + rand.nextInt(CategoryManager.CATEGORY_KEYS.length - 1);
                categoryToFetch = CategoryManager.CATEGORY_KEYS[catIndex];
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

        Locale locale = isEn ? Locale.ENGLISH : LOCALE_TR;
        tahminEdilebilirHarfler.clear();
        for (String[] row : layout) {
            for (String letter : row) {
                String lower = letter.toLowerCase(locale);
                if (lower.length() == 1) {
                    tahminEdilebilirHarfler.add(lower.charAt(0));
                }
            }
        }
        if (isEn) {
            // English keys also match these Turkish letters (see bulunacakKelimeGirilenHarfeSahipMi)
            Collections.addAll(tahminEdilebilirHarfler, 'ı', 'ç', 'ğ', 'ö', 'ş', 'ü');
        }

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
                : new Random().nextInt(yedekHavuzu.length);

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
        return CategoryManager.getCategoryDisplayName(this, categoryKey);
    }

    public void oyuncuyaGosterilecekMetniGizle() {
        oyuncuyaGosterilecekMetin.setLength(0);
        for (int i = 0; i < bulunacakKelime.length(); i++) {
            char c = bulunacakKelime.charAt(i);
            if (c == ' ') {
                oyuncuyaGosterilecekMetin.append("  ");
            } else if (!tahminEdilebilirHarfler.contains(c)) {
                // Not typeable on the active keyboard (punctuation, digits, Q/W/X in Turkish): show it from the start
                oyuncuyaGosterilecekMetin.append(c).append(" ");
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
            SoundManager.playMistakeStroke(this, mevcutHata);

            Drawable current = adamImg.getDrawable();
            if (current instanceof TransitionDrawable) {
                TransitionDrawable td = (TransitionDrawable) current;
                current = td.getDrawable(td.getNumberOfLayers() - 1);
            }
            if (current != null) {
                current.setAlpha(255);
            }
            Drawable next = ContextCompat.getDrawable(this, adamResimleri[mevcutHata]);
            if (current != null && next != null) {
                TransitionDrawable transition = new TransitionDrawable(new Drawable[]{current, next});
                transition.setCrossFadeEnabled(false);
                adamImg.setImageDrawable(transition);
                transition.startTransition(DRAWING_CROSSFADE_DURATION_MS);
            } else {
                adamImg.setImageResource(adamResimleri[mevcutHata]);
            }
        }

        if (mevcutHata >= MAX_HATA_TOLERANSI) {
            bilinemedi();
        }
    }

    public void bilinemedi() {
        titretUzun();

        if (gameMode == GameMode.MAIN_GAME) {
            LifeManager.loseLife(this);
            Toast.makeText(this, getString(R.string.seviye_kayip_mesaj, LifeManager.getLives(this), LifeManager.MAX_LIVES), Toast.LENGTH_SHORT).show();
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

        boolean kusursuz = (mevcutHata == 0);

        titretCiftDarbe();

        if (gameMode == GameMode.MAIN_GAME) {
            CurrencyManager.addCoins(this, CurrencyManager.LEVEL_WIN_REWARD);
            ProgressionManager.advanceLevel(this);
            Toast.makeText(this, getString(R.string.seviye_zafer_mesaj, CurrencyManager.LEVEL_WIN_REWARD), Toast.LENGTH_SHORT).show();
        } else {
            String tebrikMesaji = kusursuz
                    ? getString(R.string.tebrikler_kusursuz)
                    : getString(R.string.tebrikler_dogru);
            Toast.makeText(this, tebrikMesaji, Toast.LENGTH_SHORT).show();
        }

        guncelleCanVeAltinUI();
        handler.postDelayed(this::zaferKazanildi, 1300);
    }

    public void zaferKazanildi() {
        sonucAktivitesineGec(true);
    }

    public void basligiGuncelle() {
        if (gameMode == GameMode.MAIN_GAME) {
            this.setTitle(getString(R.string.seviye_format, currentLevel));
        } else {
            this.setTitle(getString(R.string.pratik_modu_etiket));
        }
    }

    public void sonucAktivitesineGec(boolean kazandi) {
        Intent intent = new Intent(MainActivity.this, SonucActivity.class);
        intent.putExtra("gameMode", gameMode.name());
        intent.putExtra("kazandi", kazandi);
        intent.putExtra("bulunacakKelime", bulunacakKelime);
        intent.putExtra("seviye", currentLevel);
        intent.putExtra("kalanCan", LifeManager.getLives(this));
        intent.putExtra("kazanilanAltin", (kazandi && gameMode == GameMode.MAIN_GAME) ? CurrencyManager.LEVEL_WIN_REWARD : 0);
        startActivity(intent);
        finish();
    }

    private void titret(long millis) {
        VibrationManager.vibrate(this, millis);
    }

    private void titretCiftDarbe() {
        VibrationManager.vibrateDoublePulse(this);
    }

    private void titretUzun() {
        VibrationManager.vibrateLong(this);
    }

    public void setContentDescriptions() {
        adamImg.setContentDescription(getString(R.string.mevcut_hata_desc, mevcutHata));
        yanlisHarflerTxt.setContentDescription(getString(R.string.bulunmayan_harfler_desc, yanlisHarfler.toString()));
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SEVIYE, currentLevel);
        if (bulunacakKelime == null || bulunacakKelime.isEmpty()) {
            // Word is still loading; it will simply be fetched again after recreation
            return;
        }
        outState.putString(STATE_KELIME, bulunacakKelime);
        outState.putString(STATE_IPUCU, ipucu);
        outState.putString(STATE_KATEGORI, aktifKategori);
        outState.putBoolean(STATE_IPUCU_ACIK, ipucuAcikMi);
        outState.putString(STATE_GOSTERILEN_METIN, oyuncuyaGosterilecekMetin.toString());

        StringBuilder yanlis = new StringBuilder();
        for (Character c : yanlisHarfler) {
            yanlis.append(c);
        }
        outState.putString(STATE_YANLIS_HARFLER, yanlis.toString());
        outState.putInt(STATE_HATA, mevcutHata);

        ArrayList<String> basilanTuslar = new ArrayList<>();
        for (MaterialButton btn : keyboardButtons) {
            if (!btn.isEnabled() && btn.getText() != null) {
                basilanTuslar.add(btn.getText().toString());
            }
        }
        outState.putStringArrayList(STATE_BASILAN_TUSLAR, basilanTuslar);
        outState.putIntegerArrayList(STATE_INDEKSLER, new ArrayList<>(kullanilabilirIndeksler));
        outState.putBoolean(STATE_ROUND_GECISI, roundGecisiYapiliyor);
    }

    /**
     * Restores a round saved by onSaveInstanceState.
     *
     * @return false if there is nothing to restore (caller should fetch a word normally)
     */
    private boolean oyunDurumunuGeriYukle(Bundle state) {
        if (state == null) {
            return false;
        }
        String kelime = state.getString(STATE_KELIME);
        String gosterilen = state.getString(STATE_GOSTERILEN_METIN);
        if (kelime == null || kelime.isEmpty() || gosterilen == null) {
            return false;
        }

        bulunacakKelime = kelime;
        ipucu = state.getString(STATE_IPUCU, "");
        aktifKategori = state.getString(STATE_KATEGORI, "all");
        ipucuAcikMi = state.getBoolean(STATE_IPUCU_ACIK, false);
        mevcutHata = state.getInt(STATE_HATA, 0);

        oyuncuyaGosterilecekMetin.setLength(0);
        oyuncuyaGosterilecekMetin.append(gosterilen);

        yanlisHarfler.clear();
        String yanlis = state.getString(STATE_YANLIS_HARFLER, "");
        for (int i = 0; i < yanlis.length(); i++) {
            yanlisHarfler.add(yanlis.charAt(i));
        }

        ArrayList<Integer> indeksler = state.getIntegerArrayList(STATE_INDEKSLER);
        if (indeksler != null) {
            kullanilabilirIndeksler.clear();
            kullanilabilirIndeksler.addAll(indeksler);
        }

        ArrayList<String> basilanTuslar = state.getStringArrayList(STATE_BASILAN_TUSLAR);
        for (MaterialButton btn : keyboardButtons) {
            boolean basildi = basilanTuslar != null && btn.getText() != null
                    && basilanTuslar.contains(btn.getText().toString());
            btn.setEnabled(!basildi);
        }

        adamImg.setImageResource(adamResimleri[Math.min(mevcutHata, adamResimleri.length - 1)]);
        guncelleKategoriVeIpucuUI(aktifKategori, ipucu);
        oyuncuyaGosterilecekMetniOyuncuyaGoster();

        if (state.getBoolean(STATE_ROUND_GECISI, false)) {
            // Word was already solved (coins/level already granted) - only finish the transition
            roundGecisiYapiliyor = true;
            setKeyboardEnabled(false);
            handler.postDelayed(this::zaferKazanildi, 1300);
        }
        return true;
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