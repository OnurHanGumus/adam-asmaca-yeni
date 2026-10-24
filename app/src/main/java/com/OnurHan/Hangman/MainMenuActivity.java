package com.OnurHan.Hangman;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.switchmaterial.SwitchMaterial;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.FirebaseApp;

public class MainMenuActivity extends AppCompatActivity {

    public static final String EXTRA_GAME_MODE = "extra_game_mode";

    private TextView appTitleTxt;
    private TextView coinTxt;
    private TextView canTxt;
    private ImageView adamOzgurImg;
    private MaterialButton btnAnaOyun;
    private MaterialButton btnPratik;
    private MaterialButton btnGunlukCark;
    private ImageButton btnAyarlar;

    private String aktifKategori = CategoryManager.CATEGORY_ALL;
    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            guncelleCanVeAltinUI();
            timerHandler.postDelayed(this, 1000);
        }
    };

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
        AppCheckManager.init(this);
        AdManager.init(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_main_menu);
        hideSystemBars();

        initComponents();
        registerEventHandlers();
        guncelleUI();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemBars();
        }
    }

    private void hideSystemBars() {
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (controller != null) {
            controller.hide(WindowInsetsCompat.Type.systemBars());
            controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        guncelleUI();
        timerHandler.post(timerRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        timerHandler.removeCallbacks(timerRunnable);
    }

    private void initComponents() {
        appTitleTxt = findViewById(R.id.appTitleTxt);
        coinTxt = findViewById(R.id.coinTxt);
        canTxt = findViewById(R.id.canTxt);
        adamOzgurImg = findViewById(R.id.adamOzgurImg);
        btnAnaOyun = findViewById(R.id.btnAnaOyun);
        btnPratik = findViewById(R.id.btnPratik);
        btnGunlukCark = findViewById(R.id.btnGunlukCark);
        btnAyarlar = findViewById(R.id.btnAyarlar);

        aktifKategori = CategoryManager.getSelectedCategory(this);
    }

    private void registerEventHandlers() {
        btnAnaOyun.setOnClickListener(v -> onAnaOyunTiklandi());

        btnPratik.setOnClickListener(v -> pratikModuPaneliniGoster());

        if (btnGunlukCark != null) {
            btnGunlukCark.setOnClickListener(v -> onGunlukCarkTiklandi());
        }

        if (btnAyarlar != null) {
            btnAyarlar.setOnClickListener(v -> ayarlarPaneliniGoster());
        }
    }

    private void onGunlukCarkTiklandi() {
        if (ProgressionManager.isDailyWheelAvailable(this)) {
            FortuneWheelDialog.showDaily(this, wonCoins -> guncelleUI());
        } else {
            long remainingMillis = ProgressionManager.getRemainingMillisUntilDailyWheel(this);
            String remainingStr = ProgressionManager.formatDailyWheelRemainingTime(remainingMillis);
            Toast.makeText(this, getString(R.string.gunluk_cark_toast_format, remainingStr), Toast.LENGTH_SHORT).show();
        }
    }

    private void onAnaOyunTiklandi() {
        int lives = LifeManager.getLives(this);
        if (lives > 0) {
            Intent intent = new Intent(MainMenuActivity.this, MainActivity.class);
            intent.putExtra(EXTRA_GAME_MODE, GameMode.MAIN_GAME.name());
            startActivity(intent);
        } else {
            canBittiDiyaloguGoster();
        }
    }

    private void canBittiDiyaloguGoster() {
        OutOfLivesDialog.show(this, false, this::guncelleUI);
    }

    private void guncelleUI() {
        if (appTitleTxt != null) {
            appTitleTxt.setText(R.string.app_name);
        }

        int level = ProgressionManager.getLevel(this);
        if (btnAnaOyun != null) {
            btnAnaOyun.setText(getString(R.string.ana_oyun_format, level));
        }
        if (btnPratik != null) {
            btnPratik.setText(R.string.pratik_modu);
        }

        guncelleCanVeAltinUI();
    }

    private void guncelleCanVeAltinUI() {
        int coins = CurrencyManager.getCoins(this);
        if (coinTxt != null) {
            coinTxt.setText(getString(R.string.altin_format, coins));
        }

        int lives = LifeManager.getLives(this);
        if (canTxt != null) {
            if (lives >= LifeManager.MAX_LIVES) {
                canTxt.setText(getString(R.string.can_tam_format, lives, LifeManager.MAX_LIVES));
            } else {
                long remainingMillis = LifeManager.getRemainingMillisUntilNextLife(this);
                String remainingStr = LifeManager.formatRemainingTime(remainingMillis);
                canTxt.setText(getString(R.string.can_sure_format, lives, LifeManager.MAX_LIVES, remainingStr));
            }
        }

        if (btnGunlukCark != null) {
            long dailyWheelRemaining = ProgressionManager.getRemainingMillisUntilDailyWheel(this);
            if (dailyWheelRemaining <= 0L) {
                btnGunlukCark.setText(R.string.gunluk_cark_hazir);
                btnGunlukCark.setAlpha(1.0f);
            } else {
                String timeStr = ProgressionManager.formatDailyWheelRemainingTime(dailyWheelRemaining);
                btnGunlukCark.setText(getString(R.string.gunluk_cark_bekleme_format, timeStr));
                btnGunlukCark.setAlpha(0.75f);
            }
        }
    }

    private String getCategoryDisplayName(String categoryKey) {
        return CategoryManager.getCategoryDisplayName(this, categoryKey);
    }

    private void kategoriSecimDiyaloguGoster(Runnable onCategorySelected) {
        String[] categoryNames = CategoryManager.getAllCategoryDisplayNames(this);
        aktifKategori = CategoryManager.getSelectedCategory(this);
        int checkedItem = 0;
        for (int i = 0; i < CategoryManager.CATEGORY_KEYS.length; i++) {
            if (CategoryManager.CATEGORY_KEYS[i].equals(aktifKategori)) {
                checkedItem = i;
                break;
            }
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.kategori_sec)
                .setSingleChoiceItems(categoryNames, checkedItem, (dialog, which) -> {
                    aktifKategori = CategoryManager.CATEGORY_KEYS[which];
                    CategoryManager.setSelectedCategory(this, aktifKategori);
                    dialog.dismiss();
                    if (onCategorySelected != null) {
                        onCategorySelected.run();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void dilSecimDiyaloguGoster() {
        String[] diller = new String[]{
                getString(R.string.dil_turkce),
                getString(R.string.dil_ingilizce),
                getString(R.string.dil_fransizca),
                getString(R.string.dil_italyanca),
                getString(R.string.dil_almanca),
                getString(R.string.dil_ispanyolca),
                getString(R.string.dil_portekizce),
                getString(R.string.dil_rusca),
                getString(R.string.dil_japonca)
        };
        int checkedItem = 0;
        String curLang = LocaleHelper.getLanguage(this);
        if ("en".equals(curLang)) {
            checkedItem = 1;
        } else if ("fr".equals(curLang)) {
            checkedItem = 2;
        } else if ("it".equals(curLang)) {
            checkedItem = 3;
        } else if ("de".equals(curLang)) {
            checkedItem = 4;
        } else if ("es".equals(curLang)) {
            checkedItem = 5;
        } else if ("pt".equals(curLang)) {
            checkedItem = 6;
        } else if ("ru".equals(curLang)) {
            checkedItem = 7;
        } else if ("ja".equals(curLang)) {
            checkedItem = 8;
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.menu_dil_secimi)
                .setSingleChoiceItems(diller, checkedItem, (dialog, which) -> {
                    String secilenDil;
                    if (which == 1) {
                        secilenDil = "en";
                    } else if (which == 2) {
                        secilenDil = "fr";
                    } else if (which == 3) {
                        secilenDil = "it";
                    } else if (which == 4) {
                        secilenDil = "de";
                    } else if (which == 5) {
                        secilenDil = "es";
                    } else if (which == 6) {
                        secilenDil = "pt";
                    } else if (which == 7) {
                        secilenDil = "ru";
                    } else if (which == 8) {
                        secilenDil = "ja";
                    } else {
                        secilenDil = "tr";
                    }
                    dialog.dismiss();
                    if (!secilenDil.equals(LocaleHelper.getLanguage(this))) {
                        LocaleHelper.setLocale(this, secilenDil);
                        ProgressionManager.clearSavedWord(this);
                        recreate();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void ayarlarPaneliniGoster() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.dialog_settings, null);
        dialog.setContentView(sheetView);

        SwitchMaterial switchSes = sheetView.findViewById(R.id.switchSes);
        SwitchMaterial switchTitresim = sheetView.findViewById(R.id.switchTitresim);
        View layoutDilAyari = sheetView.findViewById(R.id.layoutDilAyari);
        TextView txtMevcutDil = sheetView.findViewById(R.id.txtMevcutDil);
        View btnKapat = sheetView.findViewById(R.id.btnKapatAyarlar);

        if (switchSes != null) {
            switchSes.setChecked(SoundManager.isSoundEnabled(this));
            switchSes.setOnCheckedChangeListener((buttonView, isChecked) -> {
                SoundManager.setSoundEnabled(this, isChecked);
            });
        }

        if (switchTitresim != null) {
            switchTitresim.setChecked(VibrationManager.isVibrationEnabled(this));
            switchTitresim.setOnCheckedChangeListener((buttonView, isChecked) -> {
                VibrationManager.setVibrationEnabled(this, isChecked);
                if (isChecked) {
                    VibrationManager.vibrateShort(this);
                }
            });
        }

        if (txtMevcutDil != null) {
            String lang = LocaleHelper.getLanguage(this);
            String langDisplay;
            if ("ja".equals(lang)) {
                langDisplay = getString(R.string.dil_japonca);
            } else if ("ru".equals(lang)) {
                langDisplay = getString(R.string.dil_rusca);
            } else if ("pt".equals(lang)) {
                langDisplay = getString(R.string.dil_portekizce);
            } else if ("es".equals(lang)) {
                langDisplay = getString(R.string.dil_ispanyolca);
            } else if ("de".equals(lang)) {
                langDisplay = getString(R.string.dil_almanca);
            } else if ("it".equals(lang)) {
                langDisplay = getString(R.string.dil_italyanca);
            } else if ("fr".equals(lang)) {
                langDisplay = getString(R.string.dil_fransizca);
            } else if ("en".equals(lang)) {
                langDisplay = getString(R.string.dil_ingilizce);
            } else {
                langDisplay = getString(R.string.dil_turkce);
            }
            txtMevcutDil.setText(langDisplay + " ▾");
        }

        if (layoutDilAyari != null) {
            layoutDilAyari.setOnClickListener(v -> {
                dialog.dismiss();
                dilSecimDiyaloguGoster();
            });
        }

        if (btnKapat != null) {
            btnKapat.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    private void pratikModuPaneliniGoster() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.dialog_practice_mode, null);
        dialog.setContentView(sheetView);

        MaterialButton btnKategoriSecimi = sheetView.findViewById(R.id.btnKategoriSecimi);
        MaterialButton btnPratikBaslat = sheetView.findViewById(R.id.btnPratikBaslat);

        guncellePratikKategoriBtn(btnKategoriSecimi);

        if (btnKategoriSecimi != null) {
            btnKategoriSecimi.setOnClickListener(v -> {
                kategoriSecimDiyaloguGoster(() -> guncellePratikKategoriBtn(btnKategoriSecimi));
            });
        }

        if (btnPratikBaslat != null) {
            btnPratikBaslat.setOnClickListener(v -> {
                dialog.dismiss();
                Intent intent = new Intent(MainMenuActivity.this, MainActivity.class);
                intent.putExtra(EXTRA_GAME_MODE, GameMode.PRACTICE.name());
                startActivity(intent);
            });
        }

        dialog.show();
    }

    private void guncellePratikKategoriBtn(MaterialButton btn) {
        if (btn != null) {
            aktifKategori = CategoryManager.getSelectedCategory(this);
            btn.setText(getString(R.string.kategori_format, getCategoryDisplayName(aktifKategori)));
        }
    }
}
