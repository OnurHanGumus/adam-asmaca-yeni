package com.OnurHan.Hangman;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

public class VibrationManager {

    private static final String PREF_NAME = "ayarlar";
    private static final String KEY_VIBRATION = "titresim_durumu";

    public static boolean isVibrationEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_VIBRATION, true);
    }

    public static void setVibrationEnabled(Context context, boolean enabled) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_VIBRATION, enabled).apply();
    }

    public static boolean toggleVibration(Context context) {
        boolean newState = !isVibrationEnabled(context);
        setVibrationEnabled(context, newState);
        return newState;
    }

    public static void vibrate(Context context, long millis) {
        if (!isVibrationEnabled(context)) return;
        try {
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(millis);
                }
            }
        } catch (Exception ignored) {
        }
    }

    public static void vibrateShort(Context context) {
        vibrate(context, 50);
    }

    public static void vibrateLetterFound(Context context) {
        vibrate(context, 40);
    }

    public static void vibrateLetterWrong(Context context) {
        vibrate(context, 150);
    }

    public static void vibrateLong(Context context) {
        vibrate(context, 400);
    }

    public static void vibrateDoublePulse(Context context) {
        if (!isVibrationEnabled(context)) return;
        try {
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator == null) return;
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

    public static void vibrateVictory(Context context) {
        if (!isVibrationEnabled(context)) return;
        try {
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator == null) return;
            long[] timings = {0, 150, 100, 150, 100, 300};
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                int[] amplitudes = {0, VibrationEffect.DEFAULT_AMPLITUDE, 0, VibrationEffect.DEFAULT_AMPLITUDE, 0, VibrationEffect.DEFAULT_AMPLITUDE};
                vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1));
            } else {
                vibrator.vibrate(timings, -1);
            }
        } catch (Exception ignored) {
        }
    }
}
