package com.OnurHan.hangingman;

import android.content.Context;
import android.content.SharedPreferences;

public class CurrencyManager {

    private static final String PREF_NAME = "veriler";
    private static final String KEY_COINS = "oyuncu_altin";

    public static final int INITIAL_COINS = 50;
    public static final int HINT_COST = 15;
    public static final int REVEAL_LETTER_COST = 10;
    public static final int LEVEL_WIN_REWARD = 15;
    public static final int REFILL_LIFE_COST = 10;

    public static int getCoins(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_COINS, INITIAL_COINS);
    }

    public static void addCoins(Context context, int amount) {
        if (amount <= 0) {
            return;
        }
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int current = prefs.getInt(KEY_COINS, INITIAL_COINS);
        prefs.edit().putInt(KEY_COINS, current + amount).apply();
    }

    public static boolean spendCoins(Context context, int amount) {
        if (amount <= 0) {
            return true;
        }
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int current = prefs.getInt(KEY_COINS, INITIAL_COINS);
        if (current >= amount) {
            prefs.edit().putInt(KEY_COINS, current - amount).apply();
            return true;
        }
        return false;
    }
}
