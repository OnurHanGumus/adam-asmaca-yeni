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
    private static final String KEY_SAVED_LANGUAGE = "ana_oyun_kayitli_dil";

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
                .remove(KEY_SAVED_LANGUAGE)
                .apply();

        return newLevel;
    }

    public static boolean hasSavedWord(Context context) {
        String word = getSavedWord(context);
        if (word == null || word.trim().isEmpty()) {
            return false;
        }
        String savedLang = getSavedLanguage(context);
        String currentLang = LocaleHelper.getLanguage(context);
        if (!currentLang.equals(savedLang)) {
            clearSavedWord(context);
            return false;
        }
        return true;
    }

    public static String getSavedWord(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_SAVED_WORD, "");
    }

    public static String getSavedLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_SAVED_LANGUAGE, "");
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
        String currentLang = LocaleHelper.getLanguage(context);
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(KEY_SAVED_WORD, word)
                .putString(KEY_SAVED_HINT, hint)
                .putString(KEY_SAVED_CATEGORY, category)
                .putString(KEY_SAVED_LANGUAGE, currentLang)
                .apply();
    }

    public static void clearSavedWord(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .remove(KEY_SAVED_WORD)
                .remove(KEY_SAVED_HINT)
                .remove(KEY_SAVED_CATEGORY)
                .remove(KEY_SAVED_LANGUAGE)
                .apply();
    }

    private static final String KEY_LAST_WHEEL_SPUN_LEVEL = "son_cark_cevrilen_seviye";

    public static int getLastWheelSpunLevel(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_LAST_WHEEL_SPUN_LEVEL, 0);
    }

    public static void setLastWheelSpunLevel(Context context, int level) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_LAST_WHEEL_SPUN_LEVEL, level).apply();
    }

    public static boolean isWheelAvailableForLevel(Context context, int level) {
        return (level > 0) && (level % 5 == 0) && (getLastWheelSpunLevel(context) < level);
    }
}
