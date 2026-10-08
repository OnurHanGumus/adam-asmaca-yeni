package com.OnurHan.hangingman;

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

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.switchmaterial.SwitchMaterial;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

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
        btnAyarlar = findViewById(R.id.btnAyarlar);

        aktifKategori = CategoryManager.getSelectedCategory(this);
    }

    private void registerEventHandlers() {
        btnAnaOyun.setOnClickListener(v -> onAnaOyunTiklandi());

        btnPratik.setOnClickListener(v -> pratikModuPaneliniGoster());

        if (btnAyarlar != null) {
            btnAyarlar.setOnClickListener(v -> ayarlarPaneliniGoster());
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
            String langDisplay = "en".equals(lang) ? getString(R.string.dil_ingilizce) : getString(R.string.dil_turkce);
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
