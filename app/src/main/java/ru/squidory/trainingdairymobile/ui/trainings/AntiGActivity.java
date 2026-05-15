package ru.squidory.trainingdairymobile.ui.trainings;

import android.app.AlertDialog;
import android.content.Intent;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import ru.squidory.trainingdairymobile.R;
import ru.squidory.trainingdairymobile.data.local.PreferencesManager;
import ru.squidory.trainingdairymobile.data.model.AntiGSessionResponse;
import ru.squidory.trainingdairymobile.data.repository.AntiGSessionRepository;

/**
 * Activity for anti-G breathing training.
 */
public class AntiGActivity extends AppCompatActivity {

    // UI elements
    private MaterialToolbar toolbar;
    private TextView phaseTextView;
    private TextView countdownTextView;
    private Button startButton;
    private Button stopButton;
    private CheckBox vibrationCheckbox;
    private CheckBox soundCheckbox;
    private Button historyButton;
    private View[] segments = new View[4];

    // Timer state
    private Handler timerHandler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;
    private long startTimeInMillis;
    private long timeLeftInMillis;

    // Phases: inhale, hold, strain, exhale
    private static final int[] PHASE_DURATIONS = {2000, 1000, 4000, 1000}; // in milliseconds
    private static final String[] PHASE_NAMES = {"Вдох", "Задержка", "Натуживание", "Выдох"};
    private static final int PREPARATION_DURATION = 3000; // 3 seconds preparation
    private int currentPhaseIndex = 0;
    private int cycleCount = 0;
    private boolean isPreparing = false;
    
    // UI elements for cycle count
    private TextView cycleCountTextView;

    // Settings
    private boolean vibrationEnabled = true;
    private boolean soundEnabled = true;

    // Dependencies
    private AntiGSessionRepository antiGSessionRepository;
    private long userId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_anti_g); // Reuse the fragment layout

        // Initialize repository and user ID
        antiGSessionRepository = AntiGSessionRepository.getInstance();
        userId = PreferencesManager.getInstance().getUserId();

        initViews();
        setupToolbar();
        loadSettings();
        setupListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        phaseTextView = findViewById(R.id.phaseTextView);
        countdownTextView = findViewById(R.id.countdownTextView);
        cycleCountTextView = findViewById(R.id.cycleCountTextView);
        startButton = findViewById(R.id.startButton);
        stopButton = findViewById(R.id.stopButton);
        vibrationCheckbox = findViewById(R.id.vibrationCheckbox);
        soundCheckbox = findViewById(R.id.soundCheckbox);
        historyButton = findViewById(R.id.historyButton);
        segments[0] = findViewById(R.id.segment1);
        segments[1] = findViewById(R.id.segment2);
        segments[2] = findViewById(R.id.segment3);
        segments[3] = findViewById(R.id.segment4);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadSettings() {
        vibrationEnabled = PreferencesManager.getInstance().getBoolean("anti_g_vibration", true);
        soundEnabled = PreferencesManager.getInstance().getBoolean("anti_g_sound", true);
        vibrationCheckbox.setChecked(vibrationEnabled);
        soundCheckbox.setChecked(soundEnabled);
    }

    private void setupListeners() {
        startButton.setOnClickListener(v -> startTraining());
        stopButton.setOnClickListener(v -> stopTraining());
        vibrationCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            vibrationEnabled = isChecked;
            PreferencesManager.getInstance().putBoolean("anti_g_vibration", isChecked);
        });
        soundCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            soundEnabled = isChecked;
            PreferencesManager.getInstance().putBoolean("anti_g_sound", isChecked);
        });
        historyButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AntiGHistoryActivity.class);
            startActivity(intent);
        });
    }

    private void startTraining() {
        // Reset state
        currentPhaseIndex = 0;
        cycleCount = 0;
        isPreparing = true;
        timeLeftInMillis = PREPARATION_DURATION;
        updateUI();

        // Disable start button, enable stop button
        startButton.setEnabled(false);
        stopButton.setEnabled(true);

        // Start preparation countdown
        startTimer();
    }

    private void stopTraining() {
        // Stop the timer
        stopTimer();
        isPreparing = false;

        // If at least one cycle was completed, ask for comment and save
        if (cycleCount > 0) {
            showCommentDialog();
        } else {
            // Reset UI
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            phaseTextView.setText("");
            countdownTextView.setText("");
            Toast.makeText(this, "Тренировка завершена. Циклов: 0", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Shows a dialog to input a comment after training.
     */
    private void showCommentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_comment_input, null);
        builder.setView(dialogView);

        TextInputEditText commentInput = dialogView.findViewById(R.id.commentInput);
        MaterialButton cancelButton = dialogView.findViewById(R.id.dialogCancelButton);
        MaterialButton saveButton = dialogView.findViewById(R.id.dialogSaveButton);

        AlertDialog dialog = builder.create();
        dialog.setCancelable(true);

        cancelButton.setOnClickListener(v -> {
            // Cancel - do NOT save, just dismiss
            dialog.dismiss();
            // Reset UI to initial state
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            phaseTextView.setText("");
            countdownTextView.setText("");
            Toast.makeText(this, "Тренировка отменена", Toast.LENGTH_SHORT).show();
        });

        saveButton.setOnClickListener(v -> {
            String comment = commentInput.getText() != null ? commentInput.getText().toString().trim() : "";
            saveSession(comment);
            dialog.dismiss();
        });

        dialog.show();
    }

    /**
     * Saves the anti-G session with the given comment via backend API.
     * @param comment The comment to save with the session.
     */
    private void saveSession(String comment) {
        // Use the repository to save the session via backend API
        antiGSessionRepository.saveSession(cycleCount, comment, new AntiGSessionRepository.AntiGSessionCallback() {
            @Override
            public void onSuccess(AntiGSessionResponse session) {
                // Reset UI
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
                phaseTextView.setText("");
                countdownTextView.setText("");
                Toast.makeText(AntiGActivity.this, "Тренировка завершена. Циклов: " + cycleCount, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                // Reset UI (even on error, we reset so user can try again)
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
                phaseTextView.setText("");
                countdownTextView.setText("");
                Toast.makeText(AntiGActivity.this, "Ошибка сохранения: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void startTimer() {
        stopTimer(); // Ensure no existing timer

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                long millisUntilFinished = timeLeftInMillis;
                if (millisUntilFinished <= 0) {
                    // Phase finished, move to next
                    onPhaseFinished();
                    return;
                }

                // Update countdown UI (show seconds with one decimal, e.g. 2.5)
                countdownTextView.setText(String.format(Locale.getDefault(), "%.1f", millisUntilFinished / 1000.0));

                // Continue timer
                timeLeftInMillis -= 100;
                timerHandler.postDelayed(this, 100);
            }
        };

        timerHandler.postDelayed(timerRunnable, 0);
    }

    private void stopTimer() {
        timerHandler.removeCallbacks(timerRunnable);
    }

    private void onPhaseFinished() {
        if (isPreparing) {
            // Preparation done, start actual breathing cycle
            isPreparing = false;
            timeLeftInMillis = PHASE_DURATIONS[currentPhaseIndex];
            updateUI();
            startTimer();
            return;
        }

        if (vibrationEnabled) {
            vibrate();
        }
        if (soundEnabled) {
            playBeep();
        }

        // Move to next phase
        currentPhaseIndex = (currentPhaseIndex + 1) % PHASE_DURATIONS.length;
        if (currentPhaseIndex == 0) {
            cycleCount++;
        }

        timeLeftInMillis = PHASE_DURATIONS[currentPhaseIndex];
        updateUI();
        startTimer();
    }

    private void vibrate() {
        try {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(150);
                }
            }
        } catch (Exception e) {
            // ignore
        }
    }

    private void playBeep() {
        try {
            ToneGenerator toneGen = new ToneGenerator(android.media.AudioManager.STREAM_NOTIFICATION, 100);
            toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 200);
            // ToneGenerator releases itself after the tone completes
        } catch (Exception e) {
            // ignore
        }
    }

    private void updateUI() {
        if (isPreparing) {
            phaseTextView.setText("Приготовьтесь...");
        } else {
            phaseTextView.setText(PHASE_NAMES[currentPhaseIndex]);
        }
        countdownTextView.setText(String.format(Locale.getDefault(), "%.1f", timeLeftInMillis / 1000.0));
        cycleCountTextView.setText("Циклов: " + cycleCount);

        for (int i = 0; i < segments.length; i++) {
            if (segments[i] != null) {
                if (!isPreparing && i == currentPhaseIndex) {
                    segments[i].setBackgroundColor(getColor(android.R.color.holo_green_dark));
                } else {
                    segments[i].setBackgroundColor(getColor(android.R.color.darker_gray));
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
    }
}