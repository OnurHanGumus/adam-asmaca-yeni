package com.example.adamasmacaoyunu;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

import java.util.Locale;

public class LocaleHelper {

    private static final String PREF_NAME = "ayarlar";
    private static final String KEY_LANGUAGE = "secilen_dil";

    public static Context onAttach(Context context) {
        String lang = getLanguage(context);
        return setLocale(context, lang);
    }

    public static String getLanguage(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return preferences.getString(KEY_LANGUAGE, Locale.getDefault().getLanguage());
    }

    public static Context setLocale(Context context, String language) {
        persist(context, language);
        return updateResources(context, language);
    }

    private static void persist(Context context, String language) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        preferences.edit().putString(KEY_LANGUAGE, language).apply();
    }

    @SuppressWarnings("deprecation")
    private static Context updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration configuration = new Configuration(resources.getConfiguration());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale);
            resources.updateConfiguration(configuration, resources.getDisplayMetrics());
            return context.createConfigurationContext(configuration);
        } else {
            configuration.locale = locale;
            resources.updateConfiguration(configuration, resources.getDisplayMetrics());
            return context;
        }
    }
}
