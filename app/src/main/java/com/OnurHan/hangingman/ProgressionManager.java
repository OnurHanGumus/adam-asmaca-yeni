package com.OnurHan.hangingman;

import android.content.Context;
import android.content.SharedPreferences;

public class ProgressionManager {

    private static final String PREF_NAME = "veriler";
    private static final String KEY_LEVEL = "ana_oyun_seviye";
    private static final String KEY_SAVED_WORD = "ana_oyun_kayitli_kelime";
    private static final String KEY_SAVED_HINT = "ana_oyun_kayitli_ipucu";
    private static final String KEY_SAVED_CATEGORY = "ana_oyun_kayitli_kategori";
    private static final String KEY_HINT_REVEALED = "ana_oyun_ipucu_acik";

    public static int getLevel(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_LEVEL, 1);
    }

    public static int advanceLevel(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int currentLevel = prefs.getInt(KEY_LEVEL, 1);
        int newLevel = currentLevel + 1;

        prefs.edit()
                .putInt(KEY_LEVEL, newLevel)
                .remove(KEY_SAVED_WORD)
                .remove(KEY_SAVED_HINT)
                .remove(KEY_SAVED_CATEGORY)
                .remove(KEY_HINT_REVEALED)
                .apply();

        return newLevel;
    }

    public static boolean hasSavedWord(Context context) {
        String word = getSavedWord(context);
        return word != null && !word.trim().isEmpty();
    }

    public static String getSavedWord(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_SAVED_WORD, "");
    }

    public static String getSavedHint(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_SAVED_HINT, "");
    }

    public static String getSavedCategory(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_SAVED_CATEGORY, "all");
    }

    public static boolean isHintRevealed(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_HINT_REVEALED, false);
    }

    public static void setHintRevealed(Context context, boolean revealed) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_HINT_REVEALED, revealed).apply();
    }

    public static void saveCurrentLevelWord(Context context, String word, String hint, String category) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(KEY_SAVED_WORD, word)
                .putString(KEY_SAVED_HINT, hint)
                .putString(KEY_SAVED_CATEGORY, category)
                .apply();
    }

    public static void clearSavedWord(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .remove(KEY_SAVED_WORD)
                .remove(KEY_SAVED_HINT)
                .remove(KEY_SAVED_CATEGORY)
                .remove(KEY_HINT_REVEALED)
                .apply();
    }
}
