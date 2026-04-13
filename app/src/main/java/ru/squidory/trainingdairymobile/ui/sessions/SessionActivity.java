package ru.squidory.trainingdairymobile.ui.sessions;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import ru.squidory.trainingdairymobile.R;
import ru.squidory.trainingdairymobile.data.local.PreferencesManager;
import ru.squidory.trainingdairymobile.data.model.CompleteSessionRequest;
import ru.squidory.trainingdairymobile.data.model.ExerciseResponse;
import ru.squidory.trainingdairymobile.data.model.PlannedSetResponse;
import ru.squidory.trainingdairymobile.data.model.SessionExerciseResponse;
import ru.squidory.trainingdairymobile.data.model.SessionResponse;
import ru.squidory.trainingdairymobile.data.model.SessionSetResponse;
import ru.squidory.trainingdairymobile.data.model.WorkoutExerciseResponse;
import ru.squidory.trainingdairymobile.data.remote.AuthInterceptor;
import ru.squidory.trainingdairymobile.data.remote.NetworkClient;
import ru.squidory.trainingdairymobile.data.remote.api.SessionApi;
import ru.squidory.trainingdairymobile.data.repository.ExerciseRepository;
import ru.squidory.trainingdairymobile.data.repository.ProgramRepository;
import ru.squidory.trainingdairymobile.data.repository.SessionRepository;
import ru.squidory.trainingdairymobile.ui.trainings.ExercisePickerActivity;
import ru.squidory.trainingdairymobile.util.Constants;

/**
 * Activity для выполнения тренировки (сессии).
 * Все данные загружаются ОДИН РАЗ при старте.
 * Во время тренировки НЕТ запросов к серверу.
 * При завершении — один запрос POST /api/sessions/complete.
 */
public class SessionActivity extends AppCompatActivity {

    public static final String EXTRA_WORKOUT_ID = "workout_id";
    public static final String EXTRA_WORKOUT_NAME = "workout_name";
    public static final String EXTRA_WORKOUT_COMMENT = "workout_comment";

    private static final String TAG = "SessionActivity";

    private MaterialToolbar toolbar;
    private TextView workoutNameTextView;
    private TextView workoutCommentTextView;
    private RecyclerView exercisesRecyclerView;
    private MaterialButton completeSessionButton;
    private MaterialButton addExerciseButton;

    private SessionExerciseAdapter adapter;
    private SessionRepository sessionRepository;
    private SessionApi sessionApi;
    private ExerciseRepository exerciseRepository;
    private ProgramRepository programRepository;

    private long workoutId;
    private String workoutName;
    private String workoutComment;
    private long sessionId;
    private long sessionStartedAt;
    private List<SessionExerciseResponse> sessionExercises = new ArrayList<>();
    private final Map<Long, ExerciseResponse> exerciseMap = new HashMap<>();

    // Видео — кэшируется при старте
    private final Map<Long, File> videoFileCache = new HashMap<>();
    private boolean videosLoaded = false;
    private VideoView currentVideoView;

    private ActivityResultLauncher<Intent> exercisePickerLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);

        workoutId = getIntent().getLongExtra(EXTRA_WORKOUT_ID, -1);
        workoutName = getIntent().getStringExtra(EXTRA_WORKOUT_NAME);
        workoutComment = getIntent().getStringExtra(EXTRA_WORKOUT_COMMENT);

        if (workoutId == -1) {
            Toast.makeText(this, "Ошибка: тренировка не найдена", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        sessionRepository = SessionRepository.getInstance();
        sessionApi = NetworkClient.getSessionApi();
        exerciseRepository = ExerciseRepository.getInstance();
        programRepository = ProgramRepository.getInstance();

        // Регистрируем ActivityResultLauncher (ОБЯЗАТЕЛЬНО в onCreate)
        exercisePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        ArrayList<Long> selectedIds = (ArrayList<Long>) result.getData()
                                .getSerializableExtra(ExercisePickerActivity.EXTRA_SELECTED_IDS);
                        if (selectedIds != null && !selectedIds.isEmpty()) {
                            addSelectedExercises(selectedIds);
                        }
                    }
                }
        );

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupCompleteButton();
        setupAddExerciseButton();
        preloadExerciseMap();
        startSession();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        workoutNameTextView = findViewById(R.id.workoutNameTextView);
        workoutCommentTextView = findViewById(R.id.workoutCommentTextView);
        exercisesRecyclerView = findViewById(R.id.exercisesRecyclerView);
        completeSessionButton = findViewById(R.id.completeSessionButton);
        addExerciseButton = findViewById(R.id.addExerciseButton);

        if (workoutName != null && !workoutName.isEmpty()) {
            workoutNameTextView.setText(workoutName);
        }
        if (workoutComment != null && !workoutComment.isEmpty()) {
            workoutCommentTextView.setText(workoutComment);
            workoutCommentTextView.setVisibility(View.VISIBLE);
        }
    }

    private void setupAddExerciseButton() {
        addExerciseButton.setOnClickListener(v -> showExercisePicker());
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupCompleteButton() {
        completeSessionButton.setOnClickListener(v -> showCompleteConfirmation());
    }

    // ==================== Добавление упражнений ====================

    private void showExercisePicker() {
        Intent intent = new Intent(this, ExercisePickerActivity.class);
        exercisePickerLauncher.launch(intent);
    }

    private void addSelectedExercises(List<Long> exerciseIds) {
        int startOrder = sessionExercises.size() + 1;
        final int totalCount = exerciseIds.size();
        final int[] completed = {0};
        final boolean[] hasError = {false};

        for (int i = 0; i < exerciseIds.size(); i++) {
            long exerciseId = exerciseIds.get(i);
            final int index = i;

            // Создаём SessionExerciseResponse из exerciseMap
            ExerciseResponse ex = exerciseMap.get(exerciseId);
            if (ex == null) continue;

            SessionExerciseResponse se = new SessionExerciseResponse();
            se.setExercise(ex);
            se.setExerciseId(exerciseId);
            se.setExerciseOrder(startOrder + i);
            se.setSupersetGroupNumber(null);

            String exerciseType = ex.getExerciseType();
            if (exerciseType == null || exerciseType.isEmpty()) {
                exerciseType = "REPS_WEIGHT";
            }
            se.setExerciseType(exerciseType);

            // Создаём 1 подход по умолчанию (с нулевыми значениями)
            List<SessionSetResponse> defaultSets = new ArrayList<>();
            SessionSetResponse defaultSet = new SessionSetResponse();
            defaultSet.setSetNumber(1);
            defaultSet.setSetOrder(0);
            defaultSet.setIsWarmup(false);
            defaultSet.setIsDropset(false);
            // Поля будут заполнены пользователем
            defaultSets.add(defaultSet);
            se.setCompletedSets(defaultSets);

            sessionExercises.add(se);

            completed[0]++;
            if (completed[0] == totalCount && !hasError[0]) {
                runOnUiThread(() -> {
                    // Пересортируем упражнения
                    sessionExercises.sort((a, b) -> Integer.compare(
                            a.getExerciseOrder() != null ? a.getExerciseOrder() : 0,
                            b.getExerciseOrder() != null ? b.getExerciseOrder() : 0
                    ));
                    adapter.setExercises(sessionExercises);
                    updateUI();
                    Toast.makeText(this, "Упражнение добавлено", Toast.LENGTH_SHORT).show();
                });
            }
        }

        // Предзагружаем видео для новых упражнений
        preloadAllVideos();
    }

    private void preloadExerciseMap() {
        exerciseRepository.getExercises(null, null, null, new ExerciseRepository.ExercisesCallback() {
            @Override
            public void onSuccess(List<ExerciseResponse> exercises) {
                exerciseMap.clear();
                for (ExerciseResponse ex : exercises) {
                    exerciseMap.put(ex.getId(), ex);
                }
            }
            @Override public void onError(String error) {}
        });
    }

    // ==================== Запуск сессии ====================

    private void startSession() {
        Log.d(TAG, "startSession: workoutId=" + workoutId);
        loadExercisesFromWorkoutTemplate();
    }

    private void loadExercisesFromWorkoutTemplate() {
        if (sessionExercises != null && !sessionExercises.isEmpty()) return;

        programRepository.getWorkoutExercises(workoutId, new ProgramRepository.WorkoutExercisesCallback() {
            @Override
            public void onSuccess(List<WorkoutExerciseResponse> workoutExercises) {
                Log.d(TAG, "Loaded " + (workoutExercises != null ? workoutExercises.size() : 0) + " exercises");
                if (workoutExercises == null || workoutExercises.isEmpty()) {
                    sessionExercises = new ArrayList<>();
                    runOnUiThread(() -> {
                        adapter.setExerciseMap(exerciseMap);
                        adapter.setExercises(sessionExercises);
                        updateUI();
                    });
                    return;
                }

                sessionExercises = new ArrayList<>();
                for (WorkoutExerciseResponse we : workoutExercises) {
                    SessionExerciseResponse se = convertToSessionExercise(we);
                    sessionExercises.add(se);
                }
                sessionId = System.currentTimeMillis();
                sessionStartedAt = sessionId;

                loadPlannedSetsForExercises();
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to load exercises: " + error);
                sessionExercises = new ArrayList<>();
                runOnUiThread(() -> {
                    adapter.setExerciseMap(exerciseMap);
                    adapter.setExercises(sessionExercises);
                    updateUI();
                });
            }
        });
    }

    private void loadPlannedSetsForExercises() {
        if (sessionExercises.isEmpty()) {
            runOnUiThread(() -> {
                adapter.setExerciseMap(exerciseMap);
                adapter.setExercises(sessionExercises);
                updateUI();
                preloadAllVideos();
            });
            return;
        }

        final int totalCount = sessionExercises.size();
        final int[] completed = {0};

        for (int i = 0; i < sessionExercises.size(); i++) {
            final SessionExerciseResponse se = sessionExercises.get(i);
            final long workoutExerciseId = se.getWorkoutExerciseId();
            final int index = i;

            if (workoutExerciseId == 0) {
                se.setCompletedSets(new ArrayList<>());
                completed[0]++;
                checkDone(completed[0], totalCount);
                continue;
            }

            programRepository.getPlannedSets(workoutExerciseId, new ProgramRepository.PlannedSetsCallback() {
                @Override
                public void onSuccess(List<PlannedSetResponse> sets) {
                    List<PlannedSetResponse> sorted = new ArrayList<>(sets);
                    sorted.sort((a, b) -> {
                        int oa = a.getSetNumber() != null ? a.getSetNumber() : 0;
                        int ob = b.getSetNumber() != null ? b.getSetNumber() : 0;
                        return Integer.compare(oa, ob);
                    });

                    List<SessionSetResponse> sessionSets = new ArrayList<>();
                    for (int j = 0; j < sorted.size(); j++) {
                        PlannedSetResponse ps = sorted.get(j);
                        SessionSetResponse ss = new SessionSetResponse();
                        ss.setId(ps.getId());
                        ss.setSetNumber(ps.getSetNumber());
                        ss.setSetOrder(j);
                        ss.setWeight(ps.getTargetWeight());
                        ss.setReps(ps.getTargetReps());
                        ss.setDurationSeconds(ps.getTargetTime());
                        ss.setDistanceMeters(ps.getTargetDistance());
                        ss.setIsWarmup(false);
                        ss.setIsDropset(false);
                        sessionSets.add(ss);
                    }
                    se.setCompletedSets(sessionSets);
                    Log.d(TAG, "Loaded " + sessionSets.size() + " sets for exercise #" + index);

                    completed[0]++;
                    checkDone(completed[0], totalCount);
                }

                @Override
                public void onError(String error) {
                    se.setCompletedSets(new ArrayList<>());
                    completed[0]++;
                    checkDone(completed[0], totalCount);
                }
            });
        }
    }

    private void checkDone(int done, int total) {
        if (done == total) {
            runOnUiThread(() -> {
                adapter.setExerciseMap(exerciseMap);
                adapter.setExercises(sessionExercises);
                updateUI();
                preloadAllVideos();
            });
        }
    }

    private SessionExerciseResponse convertToSessionExercise(WorkoutExerciseResponse we) {
        SessionExerciseResponse se = new SessionExerciseResponse();
        se.setId(we.getId());
        se.setWorkoutExerciseId(we.getId());
        se.setSessionExerciseId(we.getId());
        se.setExerciseOrder(we.getExerciseOrder());
        se.setSupersetGroupNumber(we.getSupersetGroupNumber());
        se.setExercise(we.getExercise());
        se.setExerciseId(we.getExerciseId());

        String exerciseType = null;
        if (we.getExercise() != null && we.getExercise().getExerciseType() != null) {
            exerciseType = we.getExercise().getExerciseType();
        }
        if (exerciseType == null || exerciseType.isEmpty()) {
            exerciseType = "REPS_WEIGHT";
        }
        se.setExerciseType(exerciseType);
        se.setCompletedSets(new ArrayList<>());
        return se;
    }

    // ==================== Видео ====================

    private void setupRecyclerView() {
        adapter = new SessionExerciseAdapter();
        exercisesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        exercisesRecyclerView.setAdapter(adapter);

        adapter.setOnExerciseExpandListener(new SessionExerciseAdapter.OnExerciseExpandListener() {
            @Override
            public void onExpand(SessionExerciseResponse exercise, VideoView videoView) {
                // Не перезапускаем если уже играет это видео
                if (currentVideoView == videoView && videoView.isPlaying()) return;
                playCachedVideo(exercise.getExerciseId(), videoView);
                currentVideoView = videoView;
            }

            @Override
            public void onCollapse(VideoView videoView) {
                if (videoView != null && videoView.isPlaying()) {
                    videoView.stopPlayback();
                }
                if (currentVideoView == videoView) {
                    currentVideoView = null;
                }
            }
        });

        adapter.setOnSetActionListener(new SessionExerciseAdapter.OnSetActionListener() {
            @Override
            public void onAddSet(SessionExerciseResponse exercise) {
                showAddSetDialog(exercise);
            }

            @Override
            public void onDeleteSet(SessionExerciseResponse exercise, SessionSetResponse set) {
                confirmDeleteSet(exercise, set);
            }

            @Override
            public void onTimePickerClick(SessionExerciseResponse exercise, SessionSetResponse set, SessionExerciseAdapter.OnTimeSelectedCallback callback) {
                showInlineTimePicker(set, callback);
            }
        });

        adapter.setOnExerciseDeleteListener(new SessionExerciseAdapter.OnExerciseDeleteListener() {
            @Override
            public void onDeleteExercise(SessionExerciseResponse exercise) {
                confirmDeleteExercise(exercise);
            }
        });
    }

    private void preloadAllVideos() {
        if (videosLoaded) return;
        videosLoaded = true;

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor())
                .build();

        for (SessionExerciseResponse ex : sessionExercises) {
            long exerciseId = ex.getExerciseId();
            if (exerciseId <= 0) continue;

            File cached = videoFileCache.get(exerciseId);
            if (cached != null && cached.exists()) continue;

            String videoUrl = Constants.BASE_URL + "exercises/" + exerciseId + "/video";
            Request request = new Request.Builder().url(videoUrl).build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Video failed for exercise " + exerciseId + ": " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful() || response.body() == null) return;
                    File tempFile = new File(getCacheDir(), "session_exercise_" + exerciseId + ".mp4");
                    try (InputStream is = response.body().byteStream();
                         FileOutputStream fos = new FileOutputStream(tempFile)) {
                        byte[] buffer = new byte[8192];
                        int read;
                        while ((read = is.read(buffer)) != -1) {
                            fos.write(buffer, 0, read);
                        }
                    }
                    videoFileCache.put(exerciseId, tempFile);
                    Log.d(TAG, "Video downloaded for exercise " + exerciseId);
                }
            });
        }
    }

    private void playCachedVideo(long exerciseId, VideoView videoView) {
        File cached = videoFileCache.get(exerciseId);
        if (cached == null || !cached.exists()) {
            videoView.setVisibility(View.GONE);
            return;
        }
        try {
            videoView.setVisibility(View.VISIBLE);
            videoView.setVideoURI(Uri.fromFile(cached));
            videoView.setOnPreparedListener(mp -> {
                mp.setVolume(0f, 0f);
                mp.setLooping(true);
                mp.start();
            });
            videoView.setOnErrorListener((mp, what, extra) -> {
                videoView.setVisibility(View.GONE);
                return true;
            });
            videoView.requestFocus();
        } catch (Exception e) {
            videoView.setVisibility(View.GONE);
        }
    }

    // ==================== Диалоги подходов ====================

    private void showAddSetDialog(SessionExerciseResponse exercise) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_session_set, null);
        builder.setView(dialogView);

        com.google.android.material.textfield.TextInputLayout weightInputLayout = dialogView.findViewById(R.id.weightInputLayout);
        com.google.android.material.textfield.TextInputEditText weightInput = dialogView.findViewById(R.id.weightInput);
        com.google.android.material.textfield.TextInputLayout repsInputLayout = dialogView.findViewById(R.id.repsInputLayout);
        com.google.android.material.textfield.TextInputEditText repsInput = dialogView.findViewById(R.id.repsInput);
        com.google.android.material.textfield.TextInputLayout timeInputLayout = dialogView.findViewById(R.id.timeInputLayout);
        com.google.android.material.textfield.TextInputEditText timeInput = dialogView.findViewById(R.id.timeInput);
        com.google.android.material.textfield.TextInputLayout distanceInputLayout = dialogView.findViewById(R.id.distanceInputLayout);
        com.google.android.material.textfield.TextInputEditText distanceInput = dialogView.findViewById(R.id.distanceInput);

        com.google.android.material.button.MaterialButton addRegularButton = dialogView.findViewById(R.id.addRegularSetButton);
        com.google.android.material.button.MaterialButton addDropsetButton = dialogView.findViewById(R.id.addDropsetButton);

        View dropsetDivider = dialogView.findViewById(R.id.dropsetDivider);
        TextView dropsetLabel = dialogView.findViewById(R.id.dropsetLabel);
        com.google.android.material.button.MaterialButton dialogCancelButton = dialogView.findViewById(R.id.dialogCancelButton);
        com.google.android.material.button.MaterialButton dialogAddButton = dialogView.findViewById(R.id.dialogAddButton);
        LinearLayout dropsetEntriesContainer = dialogView.findViewById(R.id.dropsetEntriesContainer);
        com.google.android.material.button.MaterialButton addDropsetEntryButton = dialogView.findViewById(R.id.addDropsetEntryButton);

        String exerciseType = exercise.getExerciseType();
        if ((exerciseType == null || exerciseType.isEmpty()) && exerciseMap != null) {
            Long exId = exercise.getExerciseId();
            if (exId != null && exId > 0 && exerciseMap.containsKey(exId)) {
                String mapType = exerciseMap.get(exId).getExerciseType();
                if (mapType != null && !mapType.isEmpty()) exerciseType = mapType;
            }
        }
        if (exerciseType == null || exerciseType.isEmpty()) exerciseType = "REPS_WEIGHT";

        boolean isRepsWeight = "REPS_WEIGHT".equalsIgnoreCase(exerciseType);
        boolean isTimeWeight = "TIME_WEIGHT".equalsIgnoreCase(exerciseType);
        boolean isTimeDistance = "TIME_DISTANCE".equalsIgnoreCase(exerciseType);
        boolean isTimeWeightDistance = "TIME_WEIGHT_DISTANCE".equalsIgnoreCase(exerciseType);

        weightInputLayout.setVisibility(isRepsWeight || isTimeWeight || isTimeWeightDistance ? View.VISIBLE : View.GONE);
        repsInputLayout.setVisibility(isRepsWeight ? View.VISIBLE : View.GONE);
        timeInputLayout.setVisibility(isTimeWeight || isTimeDistance || isTimeWeightDistance ? View.VISIBLE : View.GONE);
        distanceInputLayout.setVisibility(isTimeDistance || isTimeWeightDistance ? View.VISIBLE : View.GONE);

        addDropsetButton.setVisibility(isRepsWeight ? View.VISIBLE : View.GONE);

        if (timeInputLayout.getVisibility() == View.VISIBLE) {
            timeInput.setFocusable(false);
            timeInput.setClickable(true);
            timeInput.setOnClickListener(v -> showTimePickerDialog(timeInput));
        }

        final List<DropsetRow> dropsetRows = new ArrayList<>();
        addDropsetEntryButton.setOnClickListener(v -> addDropsetRow(dropsetEntriesContainer, dropsetRows));

        addRegularButton.setOnClickListener(v -> {
            dropsetDivider.setVisibility(View.GONE);
            dropsetLabel.setVisibility(View.GONE);
            dropsetEntriesContainer.setVisibility(View.GONE);
            addDropsetEntryButton.setVisibility(View.GONE);
        });

        addDropsetButton.setOnClickListener(v -> {
            dropsetDivider.setVisibility(View.VISIBLE);
            dropsetLabel.setVisibility(View.VISIBLE);
            dropsetEntriesContainer.setVisibility(View.VISIBLE);
            addDropsetEntryButton.setVisibility(View.VISIBLE);
        });

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);

        dialogCancelButton.setOnClickListener(v -> dialog.dismiss());

        dialogAddButton.setOnClickListener(v -> {
            SessionSetResponse newSet = new SessionSetResponse();
            boolean isDropsetMode = dropsetEntriesContainer.getVisibility() == View.VISIBLE && !dropsetRows.isEmpty();

            if (isDropsetMode) {
                if (dropsetRows.isEmpty()) {
                    Toast.makeText(this, "Добавьте хотя бы одну запись дропсета", Toast.LENGTH_SHORT).show();
                    return;
                }
                DropsetRow firstRow = dropsetRows.get(0);
                String wStr = firstRow.getWeight();
                String rStr = firstRow.getReps();
                if (!wStr.isEmpty()) try { newSet.setWeight(Double.parseDouble(wStr)); } catch (NumberFormatException e) {}
                if (!rStr.isEmpty()) try { newSet.setReps(Integer.parseInt(rStr)); } catch (NumberFormatException e) {}
                if (dropsetRows.size() >= 2) {
                    DropsetRow dropRow = dropsetRows.get(1);
                    String dw = dropRow.getWeight();
                    String dr = dropRow.getReps();
                    if (!dw.isEmpty()) try { newSet.setDropsetWeight(Double.parseDouble(dw)); } catch (NumberFormatException e) {}
                    if (!dr.isEmpty()) try { newSet.setDropsetReps(Integer.parseInt(dr)); } catch (NumberFormatException e) {}
                    newSet.setIsDropset(true);
                }
            } else {
                if (isRepsWeight || isTimeWeight || isTimeWeightDistance) {
                    String wStr = getInputText(weightInput);
                    if (!wStr.isEmpty()) {
                        try { newSet.setWeight(Double.parseDouble(wStr)); }
                        catch (NumberFormatException e) { weightInputLayout.setError("Неверный формат"); return; }
                    }
                }
                if (isRepsWeight) {
                    String rStr = getInputText(repsInput);
                    if (!rStr.isEmpty()) {
                        try { newSet.setReps(Integer.parseInt(rStr)); }
                        catch (NumberFormatException e) { repsInputLayout.setError("Неверный формат"); return; }
                    }
                }
                if (isTimeWeight || isTimeDistance || isTimeWeightDistance) {
                    String tStr = getInputText(timeInput);
                    if (!tStr.isEmpty()) {
                        try {
                            if (tStr.contains(":")) {
                                String[] parts = tStr.split(":");
                                int min = Integer.parseInt(parts[0]);
                                int sec = Integer.parseInt(parts[1]);
                                newSet.setDurationSeconds(min * 60 + sec);
                            } else {
                                newSet.setDurationSeconds(Integer.parseInt(tStr));
                            }
                        } catch (NumberFormatException e) { timeInputLayout.setError("Неверный формат"); return; }
                    }
                }
                if (isTimeDistance || isTimeWeightDistance) {
                    String dStr = getInputText(distanceInput);
                    if (!dStr.isEmpty()) {
                        try {
                            newSet.setDistanceMeters(Double.parseDouble(dStr) * 1000);
                        } catch (NumberFormatException e) { distanceInputLayout.setError("Неверный формат"); return; }
                    }
                }
                newSet.setIsDropset(false);
            }

            newSet.setIsWarmup(false);
            addSetToExercise(exercise, newSet);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showTimePickerDialog(com.google.android.material.textfield.TextInputEditText timeInput) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_time_picker, null);
        android.widget.NumberPicker minutesPicker = dialogView.findViewById(R.id.minutesPicker);
        android.widget.NumberPicker secondsPicker = dialogView.findViewById(R.id.secondsPicker);

        minutesPicker.setMinValue(0);
        minutesPicker.setMaxValue(59);
        secondsPicker.setMinValue(0);
        secondsPicker.setMaxValue(59);

        String current = timeInput.getText() != null ? timeInput.getText().toString().trim() : "";
        if (!current.isEmpty() && current.contains(":")) {
            try {
                String[] parts = current.split(":");
                minutesPicker.setValue(Integer.parseInt(parts[0]));
                secondsPicker.setValue(Integer.parseInt(parts[1]));
            } catch (NumberFormatException e) {}
        }

        new AlertDialog.Builder(this)
                .setTitle("Время")
                .setView(dialogView)
                .setPositiveButton("OK", (d, w) -> {
                    int min = minutesPicker.getValue();
                    int sec = secondsPicker.getValue();
                    timeInput.setText(String.format("%02d:%02d", min, sec));
                    timeInput.setTag(min * 60 + sec);
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void showInlineTimePicker(SessionSetResponse set, SessionExerciseAdapter.OnTimeSelectedCallback callback) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_time_picker, null);
        android.widget.NumberPicker minutesPicker = dialogView.findViewById(R.id.minutesPicker);
        android.widget.NumberPicker secondsPicker = dialogView.findViewById(R.id.secondsPicker);

        minutesPicker.setMinValue(0);
        minutesPicker.setMaxValue(59);
        secondsPicker.setMinValue(0);
        secondsPicker.setMaxValue(59);

        int currentSec = set.getDurationSeconds() != null ? set.getDurationSeconds() : 0;
        minutesPicker.setValue(currentSec / 60);
        secondsPicker.setValue(currentSec % 60);

        new AlertDialog.Builder(this)
                .setTitle("Время")
                .setView(dialogView)
                .setPositiveButton("OK", (d, w) -> callback.onTimeSelected(minutesPicker.getValue() * 60 + secondsPicker.getValue()))
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void addSetToExercise(SessionExerciseResponse exercise, SessionSetResponse newSet) {
        List<SessionSetResponse> completedSets = exercise.getCompletedSets();
        if (completedSets == null) {
            completedSets = new ArrayList<>();
            exercise.setCompletedSets(completedSets);
        }
        int maxSetNumber = 0;
        for (SessionSetResponse s : completedSets) {
            int n = s.getSetNumber() != null ? s.getSetNumber() : 0;
            if (n > maxSetNumber) maxSetNumber = n;
        }
        newSet.setSetNumber(maxSetNumber + 1);
        newSet.setSetOrder(maxSetNumber + 1);
        completedSets.add(newSet);

        // НЕ отправляем на сервер — подходы хранятся локально до завершения
        // Обновляем только UI — видео придётся раскрыть заново
        adapter.notifyDataSetChanged();
    }

    private void confirmDeleteSet(SessionExerciseResponse exercise, SessionSetResponse set) {
        new AlertDialog.Builder(this)
            .setTitle("Удалить подход")
            .setMessage("Удалить подход?")
            .setPositiveButton("Удалить", (dialog, which) -> {
                List<SessionSetResponse> completedSets = exercise.getCompletedSets();
                if (completedSets != null) {
                    // Удаляем по ссылке на объект (не по ID!)
                    boolean removed = completedSets.remove(set);
                    if (!removed) {
                        // Fallback: удаляем по setNumber
                        completedSets.removeIf(s -> s.getSetNumber() != null &&
                            s.getSetNumber().equals(set.getSetNumber()));
                    }
                    // Перенумеруем
                    for (int i = 0; i < completedSets.size(); i++) {
                        completedSets.get(i).setSetNumber(i + 1);
                        completedSets.get(i).setSetOrder(i);
                    }

                    // АВТОУДАЛЕНИЕ: если подходов не осталось — удаляем упражнение
                    if (completedSets.isEmpty()) {
                        removeExerciseFromSession(exercise);
                        return;
                    }

                    // НЕ отправляем на сервер — только локально
                }
                adapter.notifyDataSetChanged();
            })
            .setNegativeButton("Отмена", null)
            .show();
    }

    private void confirmDeleteExercise(SessionExerciseResponse exercise) {
        String exerciseName = exercise.getExerciseName();
        new AlertDialog.Builder(this)
            .setTitle("Удалить упражнение")
            .setMessage("Удалить \"" + exerciseName + "\" из тренировки?")
            .setPositiveButton("Удалить", (dialog, which) -> {
                removeExerciseFromSession(exercise);
            })
            .setNegativeButton("Отмена", null)
            .show();
    }

    private void removeExerciseFromSession(SessionExerciseResponse exercise) {
        boolean removed = sessionExercises.remove(exercise);
        if (removed) {
            // Пересортируем и обновим order
            for (int i = 0; i < sessionExercises.size(); i++) {
                sessionExercises.get(i).setExerciseOrder(i + 1);
            }
            adapter.setExercises(sessionExercises);
            updateUI();
            Toast.makeText(this, "Упражнение удалено", Toast.LENGTH_SHORT).show();
        }
    }

    private void addDropsetRow(LinearLayout container, List<DropsetRow> rows) {
        View row = LayoutInflater.from(this).inflate(R.layout.item_dropset_entry, container, false);
        com.google.android.material.textfield.TextInputEditText wInput = row.findViewById(R.id.dropsetWeightInput);
        com.google.android.material.textfield.TextInputEditText rInput = row.findViewById(R.id.dropsetRepsInput);
        android.widget.ImageButton removeButton = row.findViewById(R.id.removeDropsetEntryButton);
        TextView label = row.findViewById(R.id.dropsetEntryLabel);

        label.setText("Запись #" + (rows.size() + 1));

        DropsetRow rowObj = new DropsetRow(wInput, rInput);
        rows.add(rowObj);

        removeButton.setOnClickListener(v -> {
            container.removeView(row);
            rows.remove(rowObj);
        });

        container.addView(row);
    }

    private String getInputText(com.google.android.material.textfield.TextInputEditText input) {
        return input.getText() != null ? input.getText().toString().trim() : "";
    }

    private static class DropsetRow {
        final com.google.android.material.textfield.TextInputEditText weightInput;
        final com.google.android.material.textfield.TextInputEditText repsInput;

        DropsetRow(com.google.android.material.textfield.TextInputEditText weightInput,
                   com.google.android.material.textfield.TextInputEditText repsInput) {
            this.weightInput = weightInput;
            this.repsInput = repsInput;
        }

        String getWeight() {
            return weightInput.getText() != null ? weightInput.getText().toString().trim() : "";
        }

        String getReps() {
            return repsInput.getText() != null ? repsInput.getText().toString().trim() : "";
        }
    }

    // ==================== Завершение сессии ====================

    private void showCompleteConfirmation() {
        if (!validateSessionData()) {
            new AlertDialog.Builder(this)
                .setTitle("Незаполненные поля")
                .setMessage("Не все поля заполнены. Заполните все обязательные поля перед завершением тренировки.")
                .setPositiveButton("OK", null)
                .show();
            return;
        }

        new AlertDialog.Builder(this)
            .setTitle("Завершить тренировку?")
            .setMessage("Вы уверены, что хотите завершить тренировку? Все данные будут отправлены на сервер.")
            .setPositiveButton("Завершить", (dialog, which) -> completeSession())
            .setNegativeButton("Отмена", null)
            .show();
    }

    private boolean validateSessionData() {
        for (SessionExerciseResponse exercise : sessionExercises) {
            String exerciseType = exercise.getExerciseType();
            if ((exerciseType == null || exerciseType.isEmpty()) && exerciseMap != null) {
                Long exId = exercise.getExerciseId();
                if (exId != null && exId > 0 && exerciseMap.containsKey(exId)) {
                    String mapType = exerciseMap.get(exId).getExerciseType();
                    if (mapType != null && !mapType.isEmpty()) exerciseType = mapType;
                }
            }
            if (exerciseType == null || exerciseType.isEmpty()) exerciseType = "REPS_WEIGHT";

            boolean isRepsWeight = "REPS_WEIGHT".equalsIgnoreCase(exerciseType);
            boolean isTimeWeight = "TIME_WEIGHT".equalsIgnoreCase(exerciseType);
            boolean isTimeDistance = "TIME_DISTANCE".equalsIgnoreCase(exerciseType);
            boolean isTimeWeightDistance = "TIME_WEIGHT_DISTANCE".equalsIgnoreCase(exerciseType);

            List<SessionSetResponse> sets = exercise.getCompletedSets();
            if (sets == null || sets.isEmpty()) return false;

            for (SessionSetResponse set : sets) {
                if (isRepsWeight) {
                    if (set.getWeight() == null || set.getReps() == null) return false;
                } else if (isTimeWeight) {
                    if (set.getWeight() == null || set.getDurationSeconds() == null) return false;
                } else if (isTimeDistance) {
                    if (set.getDurationSeconds() == null || set.getDistanceMeters() == null) return false;
                } else if (isTimeWeightDistance) {
                    if (set.getWeight() == null || set.getDurationSeconds() == null || set.getDistanceMeters() == null) return false;
                }
            }
        }
        return true;
    }

    private void completeSession() {
        CompleteSessionRequest request = new CompleteSessionRequest();
        request.setWorkoutId(workoutId);
        request.setStartedAt(new Date(sessionStartedAt));
        request.setCompletedAt(new Date());

        List<CompleteSessionRequest.ExerciseCompletion> exerciseCompletions = new ArrayList<>();
        for (SessionExerciseResponse exercise : sessionExercises) {
            CompleteSessionRequest.ExerciseCompletion completion = new CompleteSessionRequest.ExerciseCompletion();
            completion.setExerciseId(exercise.getExerciseId());
            completion.setExerciseOrder(exercise.getExerciseOrder());
            // Отправляем exerciseType — бэкенд использует его для записи правильных полей
            completion.setExerciseType(exercise.getExerciseType());

            List<CompleteSessionRequest.CompletedSetData> setData = new ArrayList<>();
            if (exercise.getCompletedSets() != null) {
                List<SessionSetResponse> sortedSets = new ArrayList<>(exercise.getCompletedSets());
                sortedSets.sort((a, b) -> {
                    int oa = a.getSetNumber() != null ? a.getSetNumber() : 0;
                    int ob = b.getSetNumber() != null ? b.getSetNumber() : 0;
                    return Integer.compare(oa, ob);
                });
                for (SessionSetResponse set : sortedSets) {
                    CompleteSessionRequest.CompletedSetData data = new CompleteSessionRequest.CompletedSetData();
                    data.setWeight(set.getWeight());
                    data.setReps(set.getReps());
                    data.setDurationSeconds(set.getDurationSeconds());
                    data.setDistanceMeters(set.getDistanceMeters());
                    data.setSetOrder(set.getSetNumber());
                    data.setIsWarmup(set.isWarmup());
                    data.setIsDropset(set.isDropset());
                    data.setDropsetWeight(set.getDropsetWeight());
                    data.setDropsetReps(set.getDropsetReps());
                    setData.add(data);
                }
            }
            completion.setCompletedSets(setData);
            exerciseCompletions.add(completion);
        }
        request.setExercises(exerciseCompletions);

        // Логируем что отправляем для отладки
        for (CompleteSessionRequest.ExerciseCompletion ec : exerciseCompletions) {
            Log.d(TAG, "completeSession: exerciseId=" + ec.getExerciseId() +
                    ", exerciseType=" + ec.getExerciseType() +
                    ", sets=" + (ec.getCompletedSets() != null ? ec.getCompletedSets().size() : 0));
            if (ec.getCompletedSets() != null) {
                for (CompleteSessionRequest.CompletedSetData sd : ec.getCompletedSets()) {
                    Log.d(TAG, "  set: weight=" + sd.getWeight() +
                            ", reps=" + sd.getReps() +
                            ", duration=" + sd.getDurationSeconds() +
                            ", distance=" + sd.getDistanceMeters());
                }
            }
        }

        sessionRepository.completeSession(workoutId, sessionStartedAt, request, new SessionRepository.SessionCallback() {
            @Override
            public void onSuccess(SessionResponse session) {
                runOnUiThread(() -> {
                    Toast.makeText(SessionActivity.this, "Тренировка завершена!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(SessionActivity.this, "Ошибка завершения: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    // ==================== Lifecycle ====================

    @Override
    protected void onPause() {
        super.onPause();
        if (currentVideoView != null && currentVideoView.isPlaying()) {
            currentVideoView.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentVideoView != null && currentVideoView.isPlaying()) {
            currentVideoView.resume();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (currentVideoView != null) {
            currentVideoView.stopPlayback();
            currentVideoView = null;
        }
        for (File file : videoFileCache.values()) {
            if (file.exists()) file.delete();
        }
        videoFileCache.clear();
    }

    private void updateUI() {
        if (sessionExercises.isEmpty()) {
            completeSessionButton.setEnabled(false);
            completeSessionButton.setText("Нет упражнений");
        } else {
            completeSessionButton.setEnabled(true);
            completeSessionButton.setText("Завершить тренировку");
        }
    }
}
