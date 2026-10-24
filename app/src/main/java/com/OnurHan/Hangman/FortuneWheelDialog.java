package com.OnurHan.Hangman;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Random;

public class FortuneWheelDialog {

    // 10 sectors matching the wheel graphic (starting from top 0° clockwise):
    // 100 (x1), 15 (x2), 25 (x3), 10 (x2), 40 (x1), 50 (x1)
    public static final int[] WHEEL_SECTOR_VALUES = {100, 15, 25, 10, 40, 25, 15, 50, 10, 25};
    // Probability weights summing to 100:
    // 10 (40% total), 15 (28% total), 25 (21% total), 40 (6%), 50 (4%), 100 (1%)
    public static final int[] WHEEL_SECTOR_WEIGHTS = {1, 14, 7, 20, 6, 7, 14, 4, 20, 7};
    public static final int SECTOR_COUNT = 10;
    public static final float DEGREES_PER_SECTOR = 360f / SECTOR_COUNT;

    public interface WheelCallback {
        void onRewardClaimed(int wonCoins);
    }

    public static AlertDialog show(Context context, int level, WheelCallback callback) {
        return showInternal(context, level, false, callback);
    }

    public static AlertDialog showDaily(Context context, WheelCallback callback) {
        return showInternal(context, 0, true, callback);
    }

    private static AlertDialog showInternal(Context context, int level, boolean isDaily, WheelCallback callback) {
        if (context instanceof Activity && ((Activity) context).isFinishing()) {
            return null;
        }

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_fortune_wheel, null);
        AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        dialog.setOnShowListener(d -> {
            if (dialog.getWindow() != null) {
                WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(dialog.getWindow(), dialog.getWindow().getDecorView());
                if (controller != null) {
                    controller.hide(WindowInsetsCompat.Type.systemBars());
                    controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                }
            }
        });

        TextView carkAciklamaTxt = dialogView.findViewById(R.id.carkAciklamaTxt);
        TextView carkSonucTxt = dialogView.findViewById(R.id.carkSonucTxt);
        ImageView carkTekerlek = dialogView.findViewById(R.id.carkTekerlek);
        ImageButton carkKapatBtn = dialogView.findViewById(R.id.carkKapatBtn);
        MaterialButton carkCevirBtn = dialogView.findViewById(R.id.carkCevirBtn);

        if (isDaily) {
            carkAciklamaTxt.setText(R.string.gunluk_cark_aciklama);
        } else {
            carkAciklamaTxt.setText(context.getString(R.string.cark_aciklama_format, level));
        }
        carkSonucTxt.setText("");

        carkKapatBtn.setOnClickListener(v -> dialog.dismiss());

        final boolean[] isSpinning = new boolean[]{false};
        final boolean[] isSpun = new boolean[]{false};
        final boolean[] callbackCalled = new boolean[]{false};
        final int[] finalWonCoins = new int[]{0};

        Runnable notifyReward = () -> {
            if (!callbackCalled[0] && isSpun[0]) {
                callbackCalled[0] = true;
                if (callback != null) {
                    callback.onRewardClaimed(finalWonCoins[0]);
                }
            }
        };

        carkCevirBtn.setOnClickListener(v -> {
            if (isSpun[0]) {
                notifyReward.run();
                dialog.dismiss();
                return;
            }

            if (isSpinning[0]) {
                return;
            }

            isSpinning[0] = true;
            dialog.setCancelable(false);
            carkKapatBtn.setVisibility(View.INVISIBLE);
            carkCevirBtn.setEnabled(false);
            carkCevirBtn.setText(R.string.cark_cevriliyor);

            Random random = new Random();
            int targetSector = pickWeightedSector(random);
            finalWonCoins[0] = WHEEL_SECTOR_VALUES[targetSector];

            // Jitter within [-9, +9] degrees to avoid landing dead on boundary (+/- 18)
            float jitter = (random.nextFloat() * 18f) - 9f;
            int fullSpins = 5 + random.nextInt(3); // 5 to 7 full revolutions

            float currentRotation = carkTekerlek.getRotation();
            float normalizedCurrent = currentRotation % 360f;
            if (normalizedCurrent < 0) {
                normalizedCurrent += 360f;
            }

            float baseTargetAngle = ((360f - (targetSector * DEGREES_PER_SECTOR)) % 360f) + jitter;
            float delta = baseTargetAngle - normalizedCurrent;
            if (delta < 0) {
                delta += 360f;
            }

            float finalRotation = currentRotation + (fullSpins * 360f) + delta;

            ValueAnimator animator = ValueAnimator.ofFloat(currentRotation, finalRotation);
            animator.setDuration(4200);
            animator.setInterpolator(new DecelerateInterpolator(2.2f));

            final int[] lastSectorTick = new int[]{-1};
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

            animator.addUpdateListener(animation -> {
                float value = (float) animation.getAnimatedValue();
                carkTekerlek.setRotation(value);

                int currentSectorIndex = (int) (value / DEGREES_PER_SECTOR);
                if (currentSectorIndex != lastSectorTick[0]) {
                    lastSectorTick[0] = currentSectorIndex;
                    playTickHaptic(context, vibrator);
                }
            });

            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    isSpinning[0] = false;
                    isSpun[0] = true;
                    dialog.setCancelable(true);

                    // Add coins to user's currency
                    CurrencyManager.addCoins(context, finalWonCoins[0]);
                    if (isDaily) {
                        ProgressionManager.recordDailyWheelSpin(context);
                    } else {
                        ProgressionManager.setLastWheelSpunLevel(context, level);
                    }

                    // Victory vibration
                    playWinHaptic(context, vibrator);

                    // Show congratulatory result
                    carkSonucTxt.setText(context.getString(R.string.cark_kazandin_format, finalWonCoins[0]));
                    carkSonucTxt.setScaleX(0.7f);
                    carkSonucTxt.setScaleY(0.7f);
                    carkSonucTxt.animate().scaleX(1.0f).scaleY(1.0f).setDuration(300).start();

                    carkCevirBtn.setEnabled(true);
                    carkCevirBtn.setText(R.string.topla);
                    carkCevirBtn.setOnClickListener(btn -> {
                        notifyReward.run();
                        dialog.dismiss();
                    });
                }
            });

            animator.start();
        });

        dialog.setOnDismissListener(d -> notifyReward.run());

        dialog.show();
        return dialog;
    }

    public static int pickWeightedSector(Random random) {
        int totalWeight = 0;
        for (int weight : WHEEL_SECTOR_WEIGHTS) {
            totalWeight += weight;
        }
        int roll = random.nextInt(totalWeight);
        int cumulative = 0;
        for (int i = 0; i < SECTOR_COUNT; i++) {
            cumulative += WHEEL_SECTOR_WEIGHTS[i];
            if (roll < cumulative) {
                return i;
            }
        }
        return 0;
    }

    private static void playTickHaptic(Context context, Vibrator vibrator) {
        if (!VibrationManager.isVibrationEnabled(context) || vibrator == null) return;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK));
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(10);
            }
        } catch (Exception ignored) {
        }
    }

    private static void playWinHaptic(Context context, Vibrator vibrator) {
        if (!VibrationManager.isVibrationEnabled(context) || vibrator == null) return;
        try {
            long[] timings = {0, 100, 80, 200};
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                int[] amplitudes = {0, VibrationEffect.DEFAULT_AMPLITUDE, 0, VibrationEffect.DEFAULT_AMPLITUDE};
                vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1));
            } else {
                vibrator.vibrate(timings, -1);
            }
        } catch (Exception ignored) {
        }
    }
}
