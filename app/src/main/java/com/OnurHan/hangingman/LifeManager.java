package com.OnurHan.hangingman;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Locale;

public class LifeManager {

    private static final String PREF_NAME = "veriler";
    private static final String KEY_LIVES = "oyuncu_can";
    private static final String KEY_LAST_REGEN_TIME = "son_can_yenilenme_zamani";

    public static final int MAX_LIVES = 3;
    public static final long REGEN_INTERVAL_MS = 20 * 60 * 1000L; // 20 minutes

    public static int getLives(Context context) {
        return syncLives(context);
    }

    public static synchronized int syncLives(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int currentLives = prefs.getInt(KEY_LIVES, MAX_LIVES);
        long lastRegenTime = prefs.getLong(KEY_LAST_REGEN_TIME, 0L);

        if (currentLives >= MAX_LIVES) {
            if (lastRegenTime != 0L) {
                prefs.edit().putLong(KEY_LAST_REGEN_TIME, 0L).apply();
            }
            return MAX_LIVES;
        }

        long now = System.currentTimeMillis();
        if (lastRegenTime <= 0L) {
            prefs.edit().putLong(KEY_LAST_REGEN_TIME, now).apply();
            return currentLives;
        }

        long elapsed = now - lastRegenTime;
        if (elapsed < 0) {
            // Clock was modified backwards
            prefs.edit().putLong(KEY_LAST_REGEN_TIME, now).apply();
            return currentLives;
        }

        if (elapsed >= REGEN_INTERVAL_MS) {
            int livesToAdd = (int) (elapsed / REGEN_INTERVAL_MS);
            int newLives = Math.min(MAX_LIVES, currentLives + livesToAdd);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(KEY_LIVES, newLives);

            if (newLives >= MAX_LIVES) {
                editor.putLong(KEY_LAST_REGEN_TIME, 0L);
            } else {
                editor.putLong(KEY_LAST_REGEN_TIME, lastRegenTime + ((long) livesToAdd * REGEN_INTERVAL_MS));
            }
            editor.apply();
            return newLives;
        }

        return currentLives;
    }

    public static synchronized void loseLife(Context context) {
        int current = syncLives(context);
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        long lastRegenTime = prefs.getLong(KEY_LAST_REGEN_TIME, 0L);
        long now = System.currentTimeMillis();

        if (current > 0) {
            current--;
        }

        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_LIVES, current);

        if (current < MAX_LIVES && lastRegenTime <= 0L) {
            editor.putLong(KEY_LAST_REGEN_TIME, now);
        }
        editor.apply();
    }

    public static synchronized void refillOneLife(Context context) {
        int current = syncLives(context);
        if (current >= MAX_LIVES) {
            return;
        }

        current++;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_LIVES, current);

        if (current >= MAX_LIVES) {
            editor.putLong(KEY_LAST_REGEN_TIME, 0L);
        }
        editor.apply();
    }

    public static synchronized void refillAllLives(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putInt(KEY_LIVES, MAX_LIVES)
                .putLong(KEY_LAST_REGEN_TIME, 0L)
                .apply();
    }

    public static synchronized long getRemainingMillisUntilNextLife(Context context) {
        int lives = syncLives(context);
        if (lives >= MAX_LIVES) {
            return 0L;
        }

        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        long lastRegenTime = prefs.getLong(KEY_LAST_REGEN_TIME, 0L);
        if (lastRegenTime <= 0L) {
            return REGEN_INTERVAL_MS;
        }

        long now = System.currentTimeMillis();
        long elapsed = now - lastRegenTime;
        if (elapsed < 0) {
            return REGEN_INTERVAL_MS;
        }

        long remaining = REGEN_INTERVAL_MS - (elapsed % REGEN_INTERVAL_MS);
        return Math.max(0L, remaining);
    }

    public static String formatRemainingTime(long millis) {
        long totalSeconds = millis / 1000L;
        long minutes = totalSeconds / 60L;
        long seconds = totalSeconds % 60L;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }
}
