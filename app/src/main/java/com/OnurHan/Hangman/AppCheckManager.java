package com.OnurHan.Hangman;

import android.content.Context;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;

/**
 * Installs the Firebase App Check provider so Realtime Database requests are attested.
 * Release builds use Play Integrity; debug builds use the Debug provider
 * (its debug secret is printed to Logcat under the "DebugAppCheckProvider" tag).
 */
public final class AppCheckManager {

    private static boolean initialized = false;

    private AppCheckManager() {
    }

    public static synchronized void init(Context context) {
        if (initialized) {
            return;
        }
        try {
            FirebaseApp.initializeApp(context.getApplicationContext());
            FirebaseAppCheck appCheck = FirebaseAppCheck.getInstance();
            if (BuildConfig.DEBUG) {
                appCheck.installAppCheckProviderFactory(DebugAppCheckProviderFactory.getInstance());
            } else {
                appCheck.installAppCheckProviderFactory(PlayIntegrityAppCheckProviderFactory.getInstance());
            }
            initialized = true;
        } catch (Exception e) {
            Log.w("firebase", "App Check initialization failed", e);
        }
    }
}
