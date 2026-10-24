package com.OnurHan.Hangman;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

/**
 * Manages Google Mobile Ads SDK initialization and Rewarded Ad preloading/display.
 * Debug builds use Google's official sample Rewarded Ad Unit ID to avoid invalid traffic;
 * release builds use the production Rewarded Ad Unit ID.
 */
public final class AdManager {

    private static final String TAG = "AdManager";
    private static final String TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917";
    private static final String PROD_REWARDED_AD_UNIT_ID = "ca-app-pub-4708991160900195/9918970544";

    private static boolean initialized = false;
    private static boolean isLoadingRewardedAd = false;
    private static RewardedAd rewardedAd = null;

    private AdManager() {
    }

    public static synchronized void init(Context context) {
        if (context == null) {
            return;
        }
        Context appContext = context.getApplicationContext();
        if (initialized) {
            if (rewardedAd == null && !isLoadingRewardedAd) {
                loadRewardedAd(appContext);
            }
            return;
        }
        initialized = true;
        try {
            MobileAds.initialize(appContext, initializationStatus -> {
                Log.d(TAG, "MobileAds initialized");
                loadRewardedAd(appContext);
            });
        } catch (Exception e) {
            Log.w(TAG, "MobileAds initialization failed", e);
        }
    }

    public static synchronized void loadRewardedAd(Context context) {
        if (context == null || rewardedAd != null || isLoadingRewardedAd) {
            return;
        }
        Context appContext = context.getApplicationContext();
        isLoadingRewardedAd = true;
        String adUnitId = BuildConfig.DEBUG ? TEST_REWARDED_AD_UNIT_ID : PROD_REWARDED_AD_UNIT_ID;

        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(appContext, adUnitId, adRequest, new RewardedAdLoadCallback() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                synchronized (AdManager.class) {
                    rewardedAd = null;
                    isLoadingRewardedAd = false;
                }
                Log.w(TAG, "RewardedAd failed to load: " + loadAdError.getMessage());
            }

            @Override
            public void onAdLoaded(@NonNull RewardedAd ad) {
                synchronized (AdManager.class) {
                    rewardedAd = ad;
                    isLoadingRewardedAd = false;
                }
                Log.d(TAG, "RewardedAd loaded");
            }
        });
    }

    public static synchronized boolean isRewardedAdReady() {
        return rewardedAd != null;
    }

    public static synchronized void showRewardedAd(
            Activity activity,
            Runnable onUserEarnedReward,
            Runnable onAdUnavailable
    ) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            return;
        }
        if (rewardedAd == null) {
            loadRewardedAd(activity);
            if (onAdUnavailable != null) {
                onAdUnavailable.run();
            }
            return;
        }

        RewardedAd adToShow = rewardedAd;
        rewardedAd = null;
        Context appContext = activity.getApplicationContext();

        adToShow.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                loadRewardedAd(appContext);
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                Log.w(TAG, "RewardedAd failed to show: " + adError.getMessage());
                loadRewardedAd(appContext);
                if (onAdUnavailable != null) {
                    onAdUnavailable.run();
                }
            }
        });

        adToShow.show(activity, rewardItem -> {
            if (onUserEarnedReward != null) {
                onUserEarnedReward.run();
            }
        });
    }
}
