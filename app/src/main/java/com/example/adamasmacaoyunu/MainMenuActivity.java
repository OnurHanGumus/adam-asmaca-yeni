package com.example.adamasmacaoyunu;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.FirebaseApp;

public class MainMenuActivity extends AppCompatActivity {

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

    private TextView appTitleTxt;
    private TextView enYuksekPuanTxt;
    private ImageView adamOzgurImg;
    private MaterialButton btnOyna;
    private MaterialButton btnKategori;
    private MaterialButton btnDil;

    private String aktifKategori = "all";

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
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_main_menu);

        initComponents();
        registerEventHandlers();
        guncelleUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        guncelleUI();
    }

    private void initComponents() {
        appTitleTxt = findViewById(R.id.appTitleTxt);
        enYuksekPuanTxt = findViewById(R.id.enYuksekPuanTxt);
        adamOzgurImg = findViewById(R.id.adamOzgurImg);
        btnOyna = findViewById(R.id.btnOyna);
        btnKategori = findViewById(R.id.btnKategori);
        btnDil = findViewById(R.id.btnDil);

        aktifKategori = getSharedPreferences(PREF_NAME, MODE_PRIVATE).getString(KEY_CATEGORY, "all");
    }

    private void registerEventHandlers() {
        btnOyna.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, MainActivity.class);
            startActivity(intent);
        });

        btnKategori.setOnClickListener(v -> kategoriSecimDiyaloguGoster());

        btnDil.setOnClickListener(v -> dilSecimDiyaloguGoster());
    }

    private void guncelleUI() {
        if (appTitleTxt != null) {
            appTitleTxt.setText(R.string.app_name);
        }
        if (btnOyna != null) {
            btnOyna.setText(R.string.oyna);
        }

        SharedPreferences verilerPref = getSharedPreferences("veriler", MODE_PRIVATE);
        int yuksekPuan = verilerPref.getInt("yuksekPuan", 0);
        enYuksekPuanTxt.setText(getString(R.string.en_yuksek_puan_format, yuksekPuan));

        aktifKategori = getSharedPreferences(PREF_NAME, MODE_PRIVATE).getString(KEY_CATEGORY, "all");
        btnKategori.setText(getString(R.string.kategori_format, getCategoryDisplayName(aktifKategori)));

        String lang = LocaleHelper.getLanguage(this);
        String langDisplay = "en".equals(lang) ? getString(R.string.dil_ingilizce) : getString(R.string.dil_turkce);
        btnDil.setText(getString(R.string.dil_format, langDisplay));
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

    private void kategoriSecimDiyaloguGoster() {
        String[] categoryNames = new String[]{
                getString(R.string.kategori_hepsi),
                getString(R.string.kat_body_parts),
                getString(R.string.kat_electronic_devices),
                getString(R.string.kat_countries),
                getString(R.string.kat_animals),
                getString(R.string.kat_fruits_vegetables)
        };

        int checkedItem = 0;
        for (int i = 0; i < CATEGORY_KEYS.length; i++) {
            if (CATEGORY_KEYS[i].equals(aktifKategori)) {
                checkedItem = i;
                break;
            }
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.kategori_sec)
                .setSingleChoiceItems(categoryNames, checkedItem, (dialog, which) -> {
                    aktifKategori = CATEGORY_KEYS[which];
                    getSharedPreferences(PREF_NAME, MODE_PRIVATE)
                            .edit()
                            .putString(KEY_CATEGORY, aktifKategori)
                            .apply();
                    dialog.dismiss();
                    guncelleUI();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void dilSecimDiyaloguGoster() {
        String[] diller = new String[]{
                getString(R.string.dil_turkce),
                getString(R.string.dil_ingilizce)
        };
        int checkedItem = "en".equals(LocaleHelper.getLanguage(this)) ? 1 : 0;

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.menu_dil_secimi)
                .setSingleChoiceItems(diller, checkedItem, (dialog, which) -> {
                    String secilenDil = (which == 1) ? "en" : "tr";
                    dialog.dismiss();
                    if (!secilenDil.equals(LocaleHelper.getLanguage(this))) {
                        LocaleHelper.setLocale(this, secilenDil);
                        recreate();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}
