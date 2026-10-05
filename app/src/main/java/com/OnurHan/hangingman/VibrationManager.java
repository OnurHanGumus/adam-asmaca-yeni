package com.OnurHan.hangingman;

import android.content.Context;
import android.content.SharedPreferences;

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
}
