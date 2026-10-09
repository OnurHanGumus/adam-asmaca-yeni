package com.OnurHan.Hangman;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.material.button.MaterialButton;

public class SonucActivity extends AppCompatActivity {

    public TextView durumTxt, bulunacakKelimeTxt;
    public Button tekrarOynaBtn, cikisBtn;
    public MaterialButton carkBonusBtn;
    private ImageView sonucResim;

    private GameMode gameMode = GameMode.MAIN_GAME;
    private boolean kazandi;
    private int seviye;
    private int kalanCan;
    private int kazanilanAltin;
    private String bulunacakKelime;

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

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_sonuc);
        hideSystemBars();
        initComponents();
        registerEventHandlers();
        degerleriAyarla();
    }

    private void initComponents() {
        durumTxt = findViewById(R.id.durumTxt);
        bulunacakKelimeTxt = findViewById(R.id.bulunacakKelimeTxt);
        tekrarOynaBtn = findViewById(R.id.tektatOynaBtn);
        cikisBtn = findViewById(R.id.cikisBtn);
        carkBonusBtn = findViewById(R.id.carkBonusBtn);
        sonucResim = findViewById(R.id.sonucResim);

        Intent intent = getIntent();
        String modeExtra = intent.getStringExtra("gameMode");
        if (modeExtra != null) {
            try {
                gameMode = GameMode.valueOf(modeExtra);
            } catch (Exception e) {
                gameMode = GameMode.MAIN_GAME;
            }
        }

        kazandi = intent.getBooleanExtra("kazandi", false);
        seviye = intent.getIntExtra("seviye", 1);
        kalanCan = intent.getIntExtra("kalanCan", LifeManager.MAX_LIVES);
        kazanilanAltin = intent.getIntExtra("kazanilanAltin", 0);
        bulunacakKelime = intent.getStringExtra("bulunacakKelime");
    }

    private void registerEventHandlers() {
        tekrarOynaBtn.setOnClickListener(v -> onTekrarOynaTiklandi());
        cikisBtn.setOnClickListener(v -> anaMenuyeDon());
        carkBonusBtn.setOnClickListener(v -> showFortuneWheel());
    }

    private void onTekrarOynaTiklandi() {
        if (gameMode == GameMode.MAIN_GAME) {
            int lives = LifeManager.getLives(this);
            if (lives > 0) {
                Intent intent = new Intent(SonucActivity.this, MainActivity.class);
                intent.putExtra(MainMenuActivity.EXTRA_GAME_MODE, GameMode.MAIN_GAME.name());
                startActivity(intent);
                finish();
            } else {
                canBittiDiyaloguGoster();
            }
        } else {
            Intent intent = new Intent(SonucActivity.this, MainActivity.class);
            intent.putExtra(MainMenuActivity.EXTRA_GAME_MODE, GameMode.PRACTICE.name());
            startActivity(intent);
            finish();
        }
    }

    private void canBittiDiyaloguGoster() {
        OutOfLivesDialog.show(this, true, null);
    }

    private void degerleriAyarla() {
        if (gameMode == GameMode.MAIN_GAME) {
            if (kazandi) {
                sonucResim.setImageResource(R.drawable.adam_ozgur);
                VibrationManager.vibrateVictory(this);

                durumTxt.setText(getString(R.string.seviye_zafer_baslik, seviye));

                String mesaj = getString(R.string.seviye_zafer_mesaj, kazanilanAltin);
                if (bulunacakKelime != null && !bulunacakKelime.trim().isEmpty()) {
                    mesaj += "\n" + getString(R.string.kelime_bilgi_format, bulunacakKelime.toUpperCase());
                }
                bulunacakKelimeTxt.setText(mesaj);
                tekrarOynaBtn.setText(R.string.sonraki_seviye);

                // Check for every 5 levels fortune wheel milestone
                if (seviye % 5 == 0) {
                    if (ProgressionManager.isWheelAvailableForLevel(this, seviye)) {
                        carkBonusBtn.setVisibility(View.VISIBLE);
                        carkBonusBtn.setText(getString(R.string.cark_bonus_buton_format, seviye));
                        carkBonusBtn.setEnabled(true);
                        carkBonusBtn.postDelayed(this::showFortuneWheel, 500);
                    } else if (ProgressionManager.getLastWheelSpunLevel(this) >= seviye) {
                        carkBonusBtn.setVisibility(View.VISIBLE);
                        carkBonusBtn.setText(R.string.cark_tamamlandi);
                        carkBonusBtn.setEnabled(false);
                    } else {
                        carkBonusBtn.setVisibility(View.GONE);
                    }
                } else {
                    carkBonusBtn.setVisibility(View.GONE);
                }
            } else {
                carkBonusBtn.setVisibility(View.GONE);
                sonucResim.setImageResource(R.drawable.adam6);
                VibrationManager.vibrate(this, 500);

                durumTxt.setText(getString(R.string.seviye_kayip_baslik, seviye));

                // Main Game Mode retry: keep the word secret so retry is a real challenge
                bulunacakKelimeTxt.setText(getString(R.string.seviye_kayip_mesaj, kalanCan, LifeManager.MAX_LIVES));
                tekrarOynaBtn.setText(R.string.tekrar_dene);
            }
        } else {
            carkBonusBtn.setVisibility(View.GONE);
            // Practice Mode (1 word at a time)
            if (kazandi) {
                sonucResim.setImageResource(R.drawable.adam_ozgur);
                VibrationManager.vibrateVictory(this);

                durumTxt.setText(getString(R.string.pratik_zafer_baslik));

                String mesaj = getString(R.string.pratik_zafer_mesaj);
                if (bulunacakKelime != null && !bulunacakKelime.trim().isEmpty()) {
                    mesaj += "\n" + getString(R.string.kelime_bilgi_format, bulunacakKelime.toUpperCase());
                }
                bulunacakKelimeTxt.setText(mesaj);
            } else {
                sonucResim.setImageResource(R.drawable.adam6);
                VibrationManager.vibrate(this, 500);

                durumTxt.setText(getString(R.string.pratik_kayip_baslik));

                String mesaj = getString(R.string.pratik_kayip_mesaj);
                if (bulunacakKelime != null && !bulunacakKelime.trim().isEmpty()) {
                    mesaj += "\n" + getString(R.string.kelime_bilgi_format, bulunacakKelime.toUpperCase());
                }
                bulunacakKelimeTxt.setText(mesaj);
            }
            tekrarOynaBtn.setText(R.string.tekrar_oyna);
        }
    }

    private void showFortuneWheel() {
        if (isFinishing()) return;
        FortuneWheelDialog.show(this, seviye, wonCoins -> {
            carkBonusBtn.setVisibility(View.VISIBLE);
            carkBonusBtn.setText(getString(R.string.cark_odul_alindi_format, wonCoins));
            carkBonusBtn.setEnabled(false);

            String mesaj = getString(R.string.seviye_zafer_mesaj, kazanilanAltin)
                    + "\n" + getString(R.string.cark_kazandin_format, wonCoins);
            if (bulunacakKelime != null && !bulunacakKelime.trim().isEmpty()) {
                mesaj += "\n" + getString(R.string.kelime_bilgi_format, bulunacakKelime.toUpperCase());
            }
            bulunacakKelimeTxt.setText(mesaj);
        });
    }

    private void anaMenuyeDon() {
        Intent intent = new Intent(SonucActivity.this, MainMenuActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        anaMenuyeDon();
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
}