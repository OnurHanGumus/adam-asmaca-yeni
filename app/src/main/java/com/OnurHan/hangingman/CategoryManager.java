package com.OnurHan.hangingman;

import android.content.Context;
import android.content.SharedPreferences;

public class CategoryManager {

    private static final String PREF_NAME = "ayarlar";
    private static final String KEY_CATEGORY = "secilen_kategori";

    public static final String CATEGORY_ALL = "all";

    public static final String[] CATEGORY_KEYS = {
            "all",
            "body_parts",
            "electronic_devices",
            "countries",
            "animals",
            "fruits_vegetables",
            "food",
            "sports",
            "vehicles",
            "professions",
            "space",
            "fantastic_elements",
            "musical_instruments",
            "superheroes",
            "weather"
    };

    public static String getSelectedCategory(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_CATEGORY, CATEGORY_ALL);
    }

    public static void setSelectedCategory(Context context, String categoryKey) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_CATEGORY, categoryKey).apply();
    }

    public static String getCategoryDisplayName(Context context, String categoryKey) {
        if (categoryKey == null) {
            return context.getString(R.string.kategori_hepsi);
        }
        switch (categoryKey) {
            case "body_parts":
                return context.getString(R.string.kat_body_parts);
            case "electronic_devices":
                return context.getString(R.string.kat_electronic_devices);
            case "countries":
                return context.getString(R.string.kat_countries);
            case "animals":
                return context.getString(R.string.kat_animals);
            case "fruits_vegetables":
                return context.getString(R.string.kat_fruits_vegetables);
            case "food":
                return context.getString(R.string.kat_food);
            case "sports":
                return context.getString(R.string.kat_sports);
            case "vehicles":
                return context.getString(R.string.kat_vehicles);
            case "professions":
                return context.getString(R.string.kat_professions);
            case "space":
                return context.getString(R.string.kat_space);
            case "fantastic_elements":
                return context.getString(R.string.kat_fantastic_elements);
            case "musical_instruments":
                return context.getString(R.string.kat_musical_instruments);
            case "superheroes":
                return context.getString(R.string.kat_superheroes);
            case "weather":
                return context.getString(R.string.kat_weather);
            default:
                return context.getString(R.string.kategori_hepsi);
        }
    }

    public static String[] getAllCategoryDisplayNames(Context context) {
        String[] names = new String[CATEGORY_KEYS.length];
        for (int i = 0; i < CATEGORY_KEYS.length; i++) {
            names[i] = getCategoryDisplayName(context, CATEGORY_KEYS[i]);
        }
        return names;
    }

    public static String getMainGameCategoryForLevel(int level) {
        int catIndex = 1 + ((level - 1) % (CATEGORY_KEYS.length - 1));
        return CATEGORY_KEYS[catIndex];
    }

    public static int getMainGameIndexForLevel(int level) {
        return ((level - 1) / (CATEGORY_KEYS.length - 1)) % 10;
    }
}
