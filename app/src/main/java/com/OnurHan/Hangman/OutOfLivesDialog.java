package com.OnurHan.Hangman;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class OutOfLivesDialog {

    public static AlertDialog show(Context context, boolean finishCurrentActivity, Runnable onLifePurchased) {
        if (context instanceof Activity && ((Activity) context).isFinishing()) {
            return null;
        }

        long remainingMillis = LifeManager.getRemainingMillisUntilNextLife(context);
        String remainingStr = LifeManager.formatRemainingTime(remainingMillis);

        return new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.can_bitti_baslik)
                .setMessage(context.getString(R.string.can_bitti_mesaj_format, remainingStr, CurrencyManager.REFILL_LIFE_COST))
                .setPositiveButton(context.getString(R.string.can_satin_al_format, CurrencyManager.REFILL_LIFE_COST), (dialog, which) -> {
                    if (CurrencyManager.spendCoins(context, CurrencyManager.REFILL_LIFE_COST)) {
                        LifeManager.refillOneLife(context);
                        Toast.makeText(context, R.string.can_yenilendi, Toast.LENGTH_SHORT).show();
                        if (onLifePurchased != null) {
                            onLifePurchased.run();
                        }
                        Intent intent = new Intent(context, MainActivity.class);
                        intent.putExtra(MainMenuActivity.EXTRA_GAME_MODE, GameMode.MAIN_GAME.name());
                        context.startActivity(intent);
                        if (finishCurrentActivity && context instanceof Activity) {
                            ((Activity) context).finish();
                        }
                    } else {
                        Toast.makeText(context, context.getString(R.string.yetersiz_altin_format, CurrencyManager.REFILL_LIFE_COST), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNeutralButton(R.string.pratik_modu, (dialog, which) -> {
                    Intent intent = new Intent(context, MainActivity.class);
                    intent.putExtra(MainMenuActivity.EXTRA_GAME_MODE, GameMode.PRACTICE.name());
                    context.startActivity(intent);
                    if (finishCurrentActivity && context instanceof Activity) {
                        ((Activity) context).finish();
                    }
                })
                .setNegativeButton(R.string.kapat, null)
                .show();
    }
}
