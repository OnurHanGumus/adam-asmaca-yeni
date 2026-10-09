package com.OnurHan.Hangman;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.util.Log;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Manages sound effects for the game.
 * Plays chalkboard chalk stroke sound effects on mistake progression.
 */
public class SoundManager {

    private static final String TAG = "SoundManager";
    private static final String PREF_NAME = "ayarlar";
    private static final String KEY_SOUND = "ses_durumu";

    private static SoundPool soundPool;
    private static int soundStrokeLongId = 0;
    private static int soundStrokeShortId = 0;
    private static final Set<Integer> loadedSounds = Collections.synchronizedSet(new HashSet<>());
    private static boolean isInitialized = false;

    public static boolean isSoundEnabled(Context context) {
        if (context == null) {
            return true;
        }
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_SOUND, true);
    }

    public static void setSoundEnabled(Context context, boolean enabled) {
        if (context == null) {
            return;
        }
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_SOUND, enabled).apply();
    }

    public static boolean toggleSound(Context context) {
        boolean newState = !isSoundEnabled(context);
        setSoundEnabled(context, newState);
        return newState;
    }

    /**
     * Determines whether the given mistake number (1-based, 1 to 6)
     * corresponds to a long chalk stroke.
     * Order: 1-long, 2-short, 3-long, 4-short, 5-short, 6-long.
     */
    public static boolean isLongStroke(int mistakeNumber) {
        return mistakeNumber == 1 || mistakeNumber == 6;
    }

    /**
     * Initializes the SoundPool and preloads sound effects using application context.
     */
    public static synchronized void init(Context context) {
        if (isInitialized && soundPool != null) {
            return;
        }

        try {
            Context appContext = context.getApplicationContext();

            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            soundPool = new SoundPool.Builder()
                    .setMaxStreams(4)
                    .setAudioAttributes(audioAttributes)
                    .build();

            soundPool.setOnLoadCompleteListener((sp, sampleId, status) -> {
                if (status == 0) {
                    loadedSounds.add(sampleId);
                } else {
                    Log.w(TAG, "Failed to load sample ID: " + sampleId + ", status: " + status);
                }
            });

            loadedSounds.clear();
            soundStrokeLongId = soundPool.load(appContext, R.raw.stroke_on_chalk_board_long, 1);
            soundStrokeShortId = soundPool.load(appContext, R.raw.stroke_on_chalk_board_short, 1);
            isInitialized = true;
        } catch (Exception e) {
            Log.e(TAG, "Error initializing SoundManager", e);
        }
    }

    /**
     * Plays the chalk stroke sound corresponding to the current mistake progression (1 to 6).
     * Order: 1-long, 2-short, 3-long, 4-short, 5-short, 6-long.
     */
    public static void playMistakeStroke(Context context, int mistakeNumber) {
        if (!isSoundEnabled(context)) {
            return;
        }

        if (mistakeNumber < 1) {
            return;
        }

        if (!isInitialized || soundPool == null) {
            init(context);
        }

        int targetSoundId = isLongStroke(mistakeNumber) ? soundStrokeLongId : soundStrokeShortId;

        if (soundPool != null && targetSoundId != 0) {
            try {
                soundPool.play(targetSoundId, 1.0f, 1.0f, 1, 0, 1.0f);
            } catch (Exception e) {
                Log.w(TAG, "Error playing sound", e);
            }
        }
    }

    /**
     * Releases SoundPool resources when no longer needed.
     */
    public static synchronized void release() {
        if (soundPool != null) {
            try {
                soundPool.release();
            } catch (Exception ignored) {
            }
            soundPool = null;
        }
        loadedSounds.clear();
        soundStrokeLongId = 0;
        soundStrokeShortId = 0;
        isInitialized = false;
    }
}
