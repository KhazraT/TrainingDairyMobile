package ru.squidory.trainingdairymobile.ui.sessions;

import android.app.AlertDialog;
import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import ru.squidory.trainingdairymobile.R;

/**
 * Диалог таймера отдыха с круговым прогрессом.
 */
public class RestTimerDialog {

    private AlertDialog dialog;
    private CircularProgressIndicator timerProgress;
    private TextView timerDisplayText;
    private CountDownTimer activeTimer;
    private int totalSeconds;
    private int remainingSeconds;
    private Ringtone ringtone;
    private OnTimerFinishedListener onFinishListener;

    public interface OnTimerFinishedListener {
        void onTimerFinished();
    }

    public void setOnFinishListener(OnTimerFinishedListener listener) {
        this.onFinishListener = listener;
    }

    public RestTimerDialog(Context context, int initialSeconds) {
        this.totalSeconds = initialSeconds;
        this.remainingSeconds = initialSeconds;

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_rest_timer, null);
        timerProgress = dialogView.findViewById(R.id.timerProgress);
        timerDisplayText = dialogView.findViewById(R.id.timerDisplayText);
        MaterialButton add10sButton = dialogView.findViewById(R.id.add10sButton);
        MaterialButton add30sButton = dialogView.findViewById(R.id.add30sButton);
        MaterialButton add1mButton = dialogView.findViewById(R.id.add1mButton);
        MaterialButton closeButton = dialogView.findViewById(R.id.closeTimerButton);

        // Настройка прогресса
        timerProgress.setMax(100);
        timerProgress.setProgress(100);
        timerDisplayText.setText(formatTime(remainingSeconds));

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);
        builder.setCancelable(false);
        dialog = builder.create();

        // Кнопки добавления времени
        add10sButton.setOnClickListener(v -> addTime(10));
        add30sButton.setOnClickListener(v -> addTime(30));
        add1mButton.setOnClickListener(v -> addTime(60));

        // Кнопка закрытия
        closeButton.setOnClickListener(v -> dismiss());

        // Звук
        Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        ringtone = RingtoneManager.getRingtone(context, ringtoneUri);

        // Запуск таймера
        startTimer(context);
    }

    private void startTimer(Context context) {
        if (activeTimer != null) {
            activeTimer.cancel();
        }

        activeTimer = new CountDownTimer(remainingSeconds * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingSeconds = (int) (millisUntilFinished / 1000);
                updateDisplay();
            }

            @Override
            public void onFinish() {
                remainingSeconds = 0;
                updateDisplay();
                timerDisplayText.setTextColor(0xFF4CAF50);

                // Звук
                if (ringtone != null && !ringtone.isPlaying()) {
                    ringtone.play();
                }

                // Остановить звук через 3 секунды
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (ringtone != null && ringtone.isPlaying()) {
                        ringtone.stop();
                    }
                    if (onFinishListener != null) {
                        onFinishListener.onTimerFinished();
                    }
                }, 3000);
            }
        }.start();
    }

    private void addTime(int seconds) {
        remainingSeconds += seconds;
        totalSeconds += seconds;
        updateDisplay();

        // Перезапустить таймер с новым временем
        if (activeTimer != null) {
            activeTimer.cancel();
        }
        startTimer(timerProgress.getContext());
    }

    private void updateDisplay() {
        timerDisplayText.setText(formatTime(remainingSeconds));
        int progressPercent = totalSeconds > 0 ? (remainingSeconds * 100) / totalSeconds : 0;
        timerProgress.setProgress(progressPercent);
    }

    private String formatTime(int seconds) {
        int min = seconds / 60;
        int sec = seconds % 60;
        return String.format("%02d:%02d", min, sec);
    }

    public void show() {
        if (dialog != null && !dialog.isShowing()) {
            dialog.show();
        }
    }

    public void dismiss() {
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
        }
        if (activeTimer != null) {
            activeTimer.cancel();
            activeTimer = null;
        }
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}
