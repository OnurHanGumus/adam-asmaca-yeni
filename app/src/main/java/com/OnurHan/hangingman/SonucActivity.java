package com.OnurHan.hangingman;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class SonucActivity extends AppCompatActivity {

    public TextView durumTxt, bulunacakKelimeTxt;
    public Button tekrarOynaBtn, cikisBtn;
    private ImageView sonucResim;
    private Vibrator v;

    private GameMode gameMode = GameMode.MAIN_GAME;
    private boolean kazandi;
    private int cozulenKelime;
    private int hedefTur;
    private int kusursuzSayisi;
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
        initComponents();
        registerEventHandlers();
        degerleriAyarla();
    }

    private void initComponents() {
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        durumTxt = findViewById(R.id.durumTxt);
        bulunacakKelimeTxt = findViewById(R.id.bulunacakKelimeTxt);
        tekrarOynaBtn = findViewById(R.id.tektatOynaBtn);
        cikisBtn = findViewById(R.id.cikisBtn);
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
        cozulenKelime = intent.getIntExtra("cozulenKelime", 0);
        hedefTur = intent.getIntExtra("hedefTur", 1);
        kusursuzSayisi = intent.getIntExtra("kusursuzSayisi", 0);
        seviye = intent.getIntExtra("seviye", 1);
        kalanCan = intent.getIntExtra("kalanCan", LifeManager.MAX_LIVES);
        kazanilanAltin = intent.getIntExtra("kazanilanAltin", 0);
        bulunacakKelime = intent.getStringExtra("bulunacakKelime");
    }

    private void registerEventHandlers() {
        tekrarOynaBtn.setOnClickListener(v -> onTekrarOynaTiklandi());
        cikisBtn.setOnClickListener(v -> anaMenuyeDon());
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
        long remainingMillis = LifeManager.getRemainingMillisUntilNextLife(this);
        String remainingStr = LifeManager.formatRemainingTime(remainingMillis);

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.can_bitti_baslik)
                .setMessage(getString(R.string.can_bitti_mesaj_format, remainingStr, CurrencyManager.REFILL_LIFE_COST))
                .setPositiveButton(getString(R.string.can_satin_al_format, CurrencyManager.REFILL_LIFE_COST), (dialog, which) -> {
                    if (CurrencyManager.spendCoins(this, CurrencyManager.REFILL_LIFE_COST)) {
                        LifeManager.refillOneLife(this);
                        Toast.makeText(this, R.string.can_yenilendi, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SonucActivity.this, MainActivity.class);
                        intent.putExtra(MainMenuActivity.EXTRA_GAME_MODE, GameMode.MAIN_GAME.name());
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, getString(R.string.yetersiz_altin_format, CurrencyManager.REFILL_LIFE_COST), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNeutralButton(R.string.pratik_modu, (dialog, which) -> {
                    Intent intent = new Intent(SonucActivity.this, MainActivity.class);
                    intent.putExtra(MainMenuActivity.EXTRA_GAME_MODE, GameMode.PRACTICE.name());
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton(R.string.kapat, null)
                .show();
    }

    private void degerleriAyarla() {
        if (gameMode == GameMode.MAIN_GAME) {
            if (kazandi) {
                sonucResim.setImageResource(R.drawable.adam_ozgur);
                titresimZafer();

                durumTxt.setText(getString(R.string.seviye_zafer_baslik, seviye));

                String mesaj = getString(R.string.seviye_zafer_mesaj, kazanilanAltin);
                if (bulunacakKelime != null && !bulunacakKelime.trim().isEmpty()) {
                    mesaj += "\n" + getString(R.string.kelime_bilgi_format, bulunacakKelime.toUpperCase());
                }
                bulunacakKelimeTxt.setText(mesaj);
                tekrarOynaBtn.setText(R.string.sonraki_seviye);
            } else {
                sonucResim.setImageResource(R.drawable.adam6);
                titresim05Saniye();

                durumTxt.setText(getString(R.string.seviye_kayip_baslik, seviye));

                // Main Game Mode retry: keep the word secret so retry is a real challenge
                bulunacakKelimeTxt.setText(getString(R.string.seviye_kayip_mesaj, kalanCan));
                tekrarOynaBtn.setText(R.string.tekrar_dene);
            }
        } else {
            // Practice Mode (1 word at a time)
            if (kazandi) {
                sonucResim.setImageResource(R.drawable.adam_ozgur);
                titresimZafer();

                durumTxt.setText(getString(R.string.pratik_zafer_baslik));

                String mesaj = getString(R.string.pratik_zafer_mesaj);
                if (bulunacakKelime != null && !bulunacakKelime.trim().isEmpty()) {
                    mesaj += "\n" + getString(R.string.kelime_bilgi_format, bulunacakKelime.toUpperCase());
                }
                bulunacakKelimeTxt.setText(mesaj);
            } else {
                sonucResim.setImageResource(R.drawable.adam6);
                titresim05Saniye();

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

    private void titresimZafer() {
        if (!VibrationManager.isVibrationEnabled(this) || v == null) return;
        try {
            long[] timings = {0, 150, 100, 150, 100, 300};
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                int[] amplitudes = {0, VibrationEffect.DEFAULT_AMPLITUDE, 0, VibrationEffect.DEFAULT_AMPLITUDE, 0, VibrationEffect.DEFAULT_AMPLITUDE};
                v.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1));
            } else {
                v.vibrate(timings, -1);
            }
        } catch (Exception ignored) {
        }
    }

    private void titresim05Saniye() {
        if (!VibrationManager.isVibrationEnabled(this) || v == null) return;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                v.vibrate(500);
            }
        } catch (Exception ignored) {
        }
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
}