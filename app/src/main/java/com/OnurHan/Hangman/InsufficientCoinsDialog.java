package com.OnurHan.Hangman;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class InsufficientCoinsDialog {

    public static AlertDialog show(Context context, int requiredCoins, Runnable onCoinsEarned) {
        if (!(context instanceof Activity)) {
            return null;
        }
        Activity activity = (Activity) context;
        if (activity.isFinishing() || activity.isDestroyed()) {
            return null;
        }

        AlertDialog alertDialog = new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.yetersiz_altin_baslik)
                .setMessage(context.getString(
                        R.string.yetersiz_altin_reklam_mesaj_format,
                        requiredCoins,
                        CurrencyManager.REWARDED_AD_COINS
                ))
                .setPositiveButton(
                        context.getString(R.string.reklam_izle_altin_kazan_format, CurrencyManager.REWARDED_AD_COINS),
                        (dialog, which) -> AdManager.showRewardedAd(
                                activity,
                                () -> {
                                    CurrencyManager.addCoins(context, CurrencyManager.REWARDED_AD_COINS);
                                    Toast.makeText(
                                            context,
                                            context.getString(R.string.reklam_odul_alindi_format, CurrencyManager.REWARDED_AD_COINS),
                                            Toast.LENGTH_SHORT
                                    ).show();
                                    if (onCoinsEarned != null) {
                                        onCoinsEarned.run();
                                    }
                                },
                                () -> Toast.makeText(context, R.string.reklam_hazir_degil, Toast.LENGTH_SHORT).show()
                        )
                )
                .setNegativeButton(R.string.kapat, null)
                .create();

        alertDialog.setOnShowListener(d -> {
            if (alertDialog.getWindow() != null) {
                WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(
                        alertDialog.getWindow(),
                        alertDialog.getWindow().getDecorView()
                );
                if (controller != null) {
                    controller.hide(WindowInsetsCompat.Type.systemBars());
                    controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                }
            }
        });

        alertDialog.show();
        return alertDialog;
    }
}
