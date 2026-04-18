package ru.squidory.trainingdairymobile.ui.sessions;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
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
import java.util.Iterator;
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
    private TextView toolbarTitleTextView;
    private TextView timerTextView;
    private LinearLayout workoutInfoLayout;
    private TextView workoutCommentTextView;
    private RecyclerView exercisesRecyclerView;
    private MaterialButton completeSessionButton;
    private MaterialButton addExerciseButton;
    private MaterialButton createSupersetButton;

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

    // Таймер сессии
    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;

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
        setupSupersetButton();
        preloadExerciseMap();
        startSession();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        toolbarTitleTextView = findViewById(R.id.toolbarTitleTextView);
        timerTextView = findViewById(R.id.timerTextView);
        workoutInfoLayout = findViewById(R.id.workoutInfoLayout);
        workoutCommentTextView = findViewById(R.id.workoutCommentTextView);
        exercisesRecyclerView = findViewById(R.id.exercisesRecyclerView);
        completeSessionButton = findViewById(R.id.completeSessionButton);
        addExerciseButton = findViewById(R.id.addExerciseButton);
        createSupersetButton = findViewById(R.id.createSupersetButton);

        // Название в toolbar
        if (workoutName != null && !workoutName.isEmpty()) {
            toolbarTitleTextView.setText(workoutName);
        } else {
            toolbarTitleTextView.setText("Тренировка");
        }

        // Комментарий — показываем только если есть
        if (workoutComment != null && !workoutComment.isEmpty()) {
            workoutCommentTextView.setText(workoutComment);
            workoutCommentTextView.setVisibility(View.VISIBLE);
            workoutInfoLayout.setVisibility(View.VISIBLE);
        } else {
            workoutInfoLayout.setVisibility(View.GONE);
        }
    }

    private void setupAddExerciseButton() {
        addExerciseButton.setOnClickListener(v -> showExercisePicker());
    }

    private void setupSupersetButton() {
        createSupersetButton.setOnClickListener(v -> showCreateSupersetDialog());
    }

    private void setupToolbar() {
        // Не используем setSupportActionBar — название и back управляем вручную
        // Кнопка назад не нужна — выход через системный Back или "Завершить тренировку"
    }

    private void startSessionTimer() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                long elapsed = System.currentTimeMillis() - sessionStartedAt;
                timerTextView.setText(formatElapsedTime(elapsed));
                timerHandler.postDelayed(this, 1000);
            }
        };
        timerHandler.post(timerRunnable);
    }

    private String formatElapsedTime(long millis) {
        long totalSeconds = millis / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private void stopSessionTimer() {
        if (timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
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

    // ==================== Создание суперсета ====================

    private void showCreateSupersetDialog() {
        List<SessionExerciseResponse> allExercises = adapter.getCurrentExercises();
        // Показываем только упражнения без суперсета
        List<SessionExerciseResponse> standalone = new ArrayList<>();
        for (SessionExerciseResponse ex : allExercises) {
            if (ex.getSupersetGroupNumber() == null || ex.getSupersetGroupNumber() <= 0) {
                standalone.add(ex);
            }
        }
        if (standalone.size() < 2) {
            Toast.makeText(this, "Нужно минимум 2 упражнения без суперсета", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Выберите упражнения для суперсета");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_superset_selection, null);
        builder.setView(dialogView);

        RecyclerView selectionRecyclerView = dialogView.findViewById(R.id.selectionRecyclerView);
        MaterialButton cancelButton = dialogView.findViewById(R.id.dialogCancelButton);
        MaterialButton saveButton = dialogView.findViewById(R.id.dialogSaveButton);
        TextView selectedCountText = dialogView.findViewById(R.id.selectedCountText);

        SessionSupersetSelectionAdapter selectionAdapter = new SessionSupersetSelectionAdapter();
        selectionAdapter.setExercises(standalone);
        selectionRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        selectionRecyclerView.setAdapter(selectionAdapter);

        selectionAdapter.setOnSelectionChangedListener(count -> {
            selectedCountText.setText("Выбрано: " + count);
            runOnUiThread(() -> saveButton.setEnabled(count >= 2));
        });
        saveButton.setEnabled(false);
        selectedCountText.setText("Выбрано: 0");

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        saveButton.setOnClickListener(v -> {
            List<SessionExerciseResponse> selected = selectionAdapter.getSelectedExercises();
            if (selected.size() < 2) return;

            // Определяем новый номер суперсета
            int maxGroup = 0;
            for (SessionExerciseResponse ex : allExercises) {
                Integer g = ex.getSupersetGroupNumber();
                if (g != null && g > maxGroup) maxGroup = g;
            }
            final int newSupersetGroup = maxGroup + 1;

            for (SessionExerciseResponse ex : selected) {
                ex.setSupersetGroupNumber(newSupersetGroup);
            }

            // Пересортируем и обновляем adapter
            sessionExercises = adapter.getCurrentExercises();
            sessionExercises.sort((a, b) -> Integer.compare(
                    a.getExerciseOrder() != null ? a.getExerciseOrder() : 0,
                    b.getExerciseOrder() != null ? b.getExerciseOrder() : 0
            ));
            // Пересчитываем order
            for (int i = 0; i < sessionExercises.size(); i++) {
                sessionExercises.get(i).setExerciseOrder(i + 1);
            }
            adapter.setExercises(sessionExercises);

            Toast.makeText(this, "Суперсет создан", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
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
                        ss.setRestTime(ps.getRestTime());
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
                // Запускаем таймер после загрузки всех данных
                startSessionTimer();
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

            @Override
            public void onDeleteSuperset(int supersetGroup) {
                confirmDeleteSuperset(supersetGroup);
            }
        });

        adapter.setOnExerciseDeleteListener(new SessionExerciseAdapter.OnExerciseDeleteListener() {
            @Override
            public void onDeleteExercise(SessionExerciseResponse exercise) {
                confirmDeleteExercise(exercise);
            }
        });

        // Drag-and-drop для изменения порядка упражнений
        ItemTouchHelper.Callback dragCallback = new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();
                if (adapter.moveDisplayItem(fromPosition, toPosition)) {
                    // Обновляем order в sessionExercises
                    sessionExercises = adapter.getCurrentExercises();
                    for (int i = 0; i < sessionExercises.size(); i++) {
                        sessionExercises.get(i).setExerciseOrder(i + 1);
                    }
                    return true;
                }
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Не поддерживаем свайп — только drag
            }

            @Override
            public boolean isLongPressDragEnabled() {
                return false; // Drag только через drag handle
            }

            @Override
            public float getMoveThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
                return 0.2f;
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(dragCallback);
        itemTouchHelper.attachToRecyclerView(exercisesRecyclerView);
        adapter.setItemTouchHelper(itemTouchHelper);
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

        // Получаем тип упражнения
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

        // Элементы диалога
        TextView setTypeTitle = dialogView.findViewById(R.id.setTypeTitle);
        
        // Кнопки выбора типа подхода
        MaterialButton addRegularButton = dialogView.findViewById(R.id.addRegularSetButton);
        MaterialButton addDropsetButton = dialogView.findViewById(R.id.addDropsetButton);
        
        // Поля для обычного подхода
        com.google.android.material.textfield.TextInputLayout weightInputLayout = dialogView.findViewById(R.id.weightInputLayout);
        com.google.android.material.textfield.TextInputEditText weightInput = dialogView.findViewById(R.id.weightInput);
        com.google.android.material.textfield.TextInputLayout repsInputLayout = dialogView.findViewById(R.id.repsInputLayout);
        com.google.android.material.textfield.TextInputEditText repsInput = dialogView.findViewById(R.id.repsInput);

        // Поля для дропсета
        com.google.android.material.textfield.TextInputLayout restTimeInputLayout = dialogView.findViewById(R.id.restTimeInputLayout);
        com.google.android.material.textfield.TextInputEditText restTimeInput = dialogView.findViewById(R.id.restTimeInput);
        View divider = dialogView.findViewById(R.id.divider);
        TextView dropsetEntriesTitle = dialogView.findViewById(R.id.dropsetEntriesTitle);
        LinearLayout dropsetEntriesContainer = dialogView.findViewById(R.id.dropsetEntriesContainer);
        MaterialButton addDropsetEntryButton = dialogView.findViewById(R.id.addDropsetEntryButton);

        // Кнопки диалога
        MaterialButton dialogCancelButton = dialogView.findViewById(R.id.dialogCancelButton);
        MaterialButton dialogAddButton = dialogView.findViewById(R.id.dialogAddButton);

        // Состояние по умолчанию - обычный подход
        final boolean[] isDropsetMode = {false};
        final List<DropsetEntry> dropsetEntries = new ArrayList<>();

        // Настройка видимости в зависимости от типа упражнения
        if (isRepsWeight) {
            // REPS_WEIGHT - показываем обе кнопки
            addDropsetButton.setVisibility(View.VISIBLE);
        } else {
            // Другие типы - только обычный подход
            addDropsetButton.setVisibility(View.GONE);
        }

        // По умолчанию скрываем поля обычного подхода
        weightInputLayout.setVisibility(View.GONE);
        repsInputLayout.setVisibility(View.GONE);

        // По умолчанию скрываем поля дропсета
        restTimeInputLayout.setVisibility(View.GONE);
        divider.setVisibility(View.GONE);
        dropsetEntriesTitle.setVisibility(View.GONE);
        dropsetEntriesContainer.setVisibility(View.GONE);
        addDropsetEntryButton.setVisibility(View.GONE);

        // Обработчики для поля отдыха
        restTimeInput.setFocusable(false);
        restTimeInput.setClickable(true);
        restTimeInput.setOnClickListener(v -> showRestTimePicker(restTimeInput, null));

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);

        // Обработчик выбора "Обычный"
        addRegularButton.setOnClickListener(v -> {
            isDropsetMode[0] = false;
            addRegularButton.setBackgroundColor(0xFF6200EE);
            addRegularButton.setTextColor(0xFFFFFFFF);
            addDropsetButton.setBackgroundColor(0x00000000);
            addDropsetButton.setTextColor(0xFF6200EE);

            // Показываем поля обычного подхода
            weightInputLayout.setVisibility(View.VISIBLE);
            repsInputLayout.setVisibility(View.VISIBLE);

            // Скрываем поля дропсета
            restTimeInputLayout.setVisibility(View.GONE);
            divider.setVisibility(View.GONE);
            dropsetEntriesTitle.setVisibility(View.GONE);
            dropsetEntriesContainer.setVisibility(View.GONE);
            addDropsetEntryButton.setVisibility(View.GONE);
        });

        // Обработчик выбора "Дропсет"
        addDropsetButton.setOnClickListener(v -> {
            isDropsetMode[0] = true;
            addDropsetButton.setBackgroundColor(0xFFFF9800);
            addDropsetButton.setTextColor(0xFFFFFFFF);
            addRegularButton.setBackgroundColor(0x00000000);
            addRegularButton.setTextColor(0xFF6200EE);

            // Скрываем поля обычного подхода
            weightInputLayout.setVisibility(View.GONE);
            repsInputLayout.setVisibility(View.GONE);

            // Показываем поля дропсета
            restTimeInputLayout.setVisibility(View.VISIBLE);
            divider.setVisibility(View.VISIBLE);
            dropsetEntriesTitle.setVisibility(View.VISIBLE);
            dropsetEntriesContainer.setVisibility(View.VISIBLE);
            addDropsetEntryButton.setVisibility(View.VISIBLE);

            // Добавляем 2 записи дропсета по умолчанию
            dropsetEntries.clear();
            dropsetEntriesContainer.removeAllViews();
            if (dropsetEntries.size() < 2) {
                addDropsetRow(dropsetEntriesContainer, dropsetEntries, 1);
                addDropsetRow(dropsetEntriesContainer, dropsetEntries, 2);
            }
        });

        // Обработчик добавления записи дропсета
        addDropsetEntryButton.setOnClickListener(v -> {
            addDropsetRow(dropsetEntriesContainer, dropsetEntries, dropsetEntries.size() + 1);
        });

        // Обработчик кнопки "Отмена"
        dialogCancelButton.setOnClickListener(v -> dialog.dismiss());

        // Обработчик кнопки "Добавить"
        dialogAddButton.setOnClickListener(v -> {
            if (!isDropsetMode[0]) {
                // Обычный подход
                SessionSetResponse newSet = new SessionSetResponse();
                newSet.setIsWarmup(false);
                newSet.setIsDropset(false);
                newSet.setIsDropsetPart(false);

                String wStr = getInputText(weightInput);
                String rStr = getInputText(repsInput);

                if (!wStr.isEmpty()) {
                    try {
                        newSet.setWeight(Double.parseDouble(wStr));
                    } catch (NumberFormatException e) {
                        weightInputLayout.setError("Неверный формат");
                        return;
                    }
                }

                if (!rStr.isEmpty()) {
                    try {
                        newSet.setReps(Integer.parseInt(rStr));
                    } catch (NumberFormatException e) {
                        repsInputLayout.setError("Неверный формат");
                        return;
                    }
                }

                addSetToExercise(exercise, newSet);
            } else {
                // Дропсет
                Integer restTime = parseRestTime(restTimeInput.getText() != null ? restTimeInput.getText().toString() : "");

                // Собираем данные из записей дропсета
                List<Double> weights = new ArrayList<>();
                List<Integer> repsList = new ArrayList<>();

                boolean hasValidData = false;
                for (DropsetEntry entry : dropsetEntries) {
                    String wStr = entry.getWeight();
                    String rStr = entry.getReps();
                    if (!wStr.isEmpty() && !rStr.isEmpty()) {
                        try {
                            weights.add(Double.parseDouble(wStr));
                            repsList.add(Integer.parseInt(rStr));
                            hasValidData = true;
                        } catch (NumberFormatException e) {
                            // Skip invalid
                        }
                    }
                }

                if (!hasValidData || weights.size() < 2) {
                    Toast.makeText(this, "Заполните минимум 2 записи с весом и повторениями", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Добавляем подходы дропсета
                addDropsetToExercise(exercise, weights, repsList, restTime);
            }

            dialog.dismiss();
        });

        dialog.show();
    }

    private void addDropsetRow(LinearLayout container, List<DropsetEntry> entries, int number) {
        View row = LayoutInflater.from(this).inflate(R.layout.item_dropset_entry, container, false);
        
        TextView numberView = row.findViewById(R.id.dropsetEntryNumber);
        TextView typeView = row.findViewById(R.id.dropsetEntryType);
        com.google.android.material.textfield.TextInputEditText weightInput = row.findViewById(R.id.dropsetWeightInput);
        com.google.android.material.textfield.TextInputEditText repsInput = row.findViewById(R.id.dropsetRepsInput);
        ImageButton removeButton = row.findViewById(R.id.removeDropsetEntryButton);

        numberView.setText(String.valueOf(number));

        // Первая запись - основной, остальные - дропсет
        boolean isFirst = (number == 1);
        if (isFirst) {
            typeView.setText("основной");
            typeView.setTextColor(0xFF6200EE);
        } else {
            typeView.setText("дропсет");
            typeView.setTextColor(0xFFFF9800);
        }

        // Создаём entry перед использованием в callback
        final DropsetEntry entry = new DropsetEntry(weightInput, repsInput);
        entries.add(entry);

        // Кнопка удаления: видна только если записей > 2 (для основного скрыта всегда)
        boolean canRemove = entries.size() > 2;
        removeButton.setVisibility(canRemove ? View.VISIBLE : View.GONE);

        // Обработчик удаления
        removeButton.setOnClickListener(v -> {
            // Нельзя удалить если останется меньше 2 записей
            if (entries.size() <= 2) {
                Toast.makeText(this, "Минимум 2 записи в дропсете", Toast.LENGTH_SHORT).show();
                return;
            }
            container.removeView(row);
            entries.remove(entry);
            // Перенумеровываем оставшиеся и обновляем типы
            renumberDropsetEntries(container);
        });

        container.addView(row);
    }

    private void updateRemoveButtonVisibility(LinearLayout container, ImageButton removeButton, int entryIndex) {
        // Кнопка видна только если записей > 2 И это не первая запись
        boolean canRemove = container.getChildCount() > 2 && entryIndex > 0;
        removeButton.setVisibility(canRemove ? View.VISIBLE : View.GONE);
    }

    private void renumberDropsetEntries(LinearLayout container) {
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            TextView numberView = child.findViewById(R.id.dropsetEntryNumber);
            TextView typeView = child.findViewById(R.id.dropsetEntryType);
            ImageButton removeButton = child.findViewById(R.id.removeDropsetEntryButton);

            if (numberView != null) {
                numberView.setText(String.valueOf(i + 1));
            }

            // Обновляем тип записи
            boolean isFirst = (i == 0);
            if (typeView != null) {
                if (isFirst) {
                    typeView.setText("основной");
                    typeView.setTextColor(0xFF6200EE);
                } else {
                    typeView.setText("дропсет");
                    typeView.setTextColor(0xFFFF9800);
                }
            }

            // Обновляем видимость кнопки удаления
            updateRemoveButtonVisibility(container, removeButton, i);
        }
    }

    private Integer parseRestTime(String text) {
        if (text == null || text.isEmpty()) return 0;
        try {
            if (text.contains(":")) {
                String[] parts = text.split(":");
                return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
            }
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void addDropsetToExercise(SessionExerciseResponse exercise, List<Double> weights, List<Integer> reps, Integer restTime) {
        List<SessionSetResponse> completedSets = exercise.getCompletedSets();
        if (completedSets == null) {
            completedSets = new ArrayList<>();
            exercise.setCompletedSets(completedSets);
        }

        // Генерируем уникальный ID группы для всех подходов дропсета
        final long dropsetGroupId = System.currentTimeMillis();

        // Определяем начальный номер подхода
        int maxSetNumber = 0;
        for (SessionSetResponse s : completedSets) {
            int n = s.getSetNumber() != null ? s.getSetNumber() : 0;
            if (n > maxSetNumber) maxSetNumber = n;
        }

        // Первый подход - ОСНОВНОЙ/ОБЫЧНЫЙ (isDropsetPart=false)
        // Это обычный подход в конвейере дропсета
        SessionSetResponse mainSet = new SessionSetResponse();
        mainSet.setSetNumber(maxSetNumber + 1);
        mainSet.setSetOrder(maxSetNumber);
        mainSet.setWeight(weights.get(0));
        mainSet.setReps(reps.get(0));
        mainSet.setIsDropset(false);
        mainSet.setIsDropsetPart(false); // Основной подход (первый в конвейере)
        mainSet.setRestTime(null);       // У основного подхода нет отдыха
        mainSet.setIsWarmup(false);
        mainSet.setDropsetGroupId(dropsetGroupId); // ID группы для связи
        // Сохраняем все дропсет-подходы в списках для отправки
        if (weights.size() > 1) {
            mainSet.setDropsetWeight(weights.get(1));
            mainSet.setDropsetReps(reps.get(1));
        }
        completedSets.add(mainSet);

        // Остальные подходы - дропсет-подходы (isDropsetPart=true)
        for (int i = 1; i < weights.size(); i++) {
            SessionSetResponse dropSet = new SessionSetResponse();
            dropSet.setSetNumber(maxSetNumber + 1 + i);
            dropSet.setSetOrder(maxSetNumber + i);
            dropSet.setWeight(weights.get(i));
            dropSet.setReps(reps.get(i));
            dropSet.setIsDropset(false);
            dropSet.setIsDropsetPart(true); // Часть дропсета
            dropSet.setRestTime(null);       // Нет своего отдыха
            dropSet.setIsWarmup(false);
            dropSet.setDropsetGroupId(dropsetGroupId); // Тот же ID группы
            completedSets.add(dropSet);
        }

        // Отдых после последнего подхода дропсета
        // Рисуем отдых отдельным элементом (даже если 0) — только после цепочки дропсета
        SessionSetResponse restSet = new SessionSetResponse();
        restSet.setSetNumber(maxSetNumber + weights.size() + 1);
        restSet.setSetOrder(maxSetNumber + weights.size());
        restSet.setWeight(null);
        restSet.setReps(null);
        restSet.setIsDropset(false);
        restSet.setIsDropsetPart(false);
        restSet.setIsRest(true); // Флаг что это элемент отдыха
        restSet.setRestTime(restTime != null ? restTime : 0);
        restSet.setIsWarmup(false);
        restSet.setDropsetGroupId(dropsetGroupId); // Та же группа
        completedSets.add(restSet);

        adapter.notifyDataSetChanged();
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
        String message;
        if (set.isRest()) {
            message = "Удалить отдых?";
        } else if (set.isDropsetPart()) {
            message = "Удалить дропсет-подход?";
        } else {
            message = "Удалить подход?\n(Удалятся все связанные дропсет-подходы)";
        }

        new AlertDialog.Builder(this)
            .setTitle("Удалить подход")
            .setMessage(message)
            .setPositiveButton("Удалить", (dialog, which) -> {
                List<SessionSetResponse> completedSets = exercise.getCompletedSets();
                if (completedSets == null) return;

                Long groupId = set.getDropsetGroupId();

                if (set.isDropsetPart()) {
                    // Удаляем ТОЛЬКО этот дропсет-подход
                    completedSets.remove(set);

                    // Проверяем, остались ли ещё дропсет-подходы у этой группы
                    boolean hasDropsetParts = false;
                    for (SessionSetResponse s : completedSets) {
                        if (s.getDropsetGroupId() != null && s.getDropsetGroupId().equals(groupId) && s.isDropsetPart()) {
                            hasDropsetParts = true;
                            break;
                        }
                    }

                    // Если дропсет-подходов не осталось, основной становится обычным подходом
                    if (!hasDropsetParts) {
                        for (SessionSetResponse s : completedSets) {
                            if (s.getDropsetGroupId() != null && s.getDropsetGroupId().equals(groupId) && !s.isDropsetPart() && !s.isRest()) {
                                s.setIsDropsetPart(false); // Теперь это обычный подход
                                // Очищаем данные дропсета
                                s.setDropsetWeight(null);
                                s.setDropsetReps(null);
                                break;
                            }
                        }
                    }
                } else if (set.isRest()) {
                    // Удаляем ТОЛЬКО элемент отдыха
                    completedSets.remove(set);
                } else {
                    // Удаляем основной подход и ВСЕ связанные подходы (дропсет-подходы + отдых)
                    Iterator<SessionSetResponse> iterator = completedSets.iterator();
                    while (iterator.hasNext()) {
                        SessionSetResponse s = iterator.next();
                        if (s == set || (groupId != null && groupId.equals(s.getDropsetGroupId()))) {
                            iterator.remove();
                        }
                    }
                }

                // Перенумеруем оставшиеся подходы
                for (int i = 0; i < completedSets.size(); i++) {
                    completedSets.get(i).setSetNumber(i + 1);
                    completedSets.get(i).setSetOrder(i);
                }

                // АВТОУДАЛЕНИЕ: если подходов не осталось — удаляем упражнение
                if (completedSets.isEmpty()) {
                    removeExerciseFromSession(exercise);
                    return;
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

    private void confirmDeleteSuperset(int supersetGroup) {
        new AlertDialog.Builder(this)
            .setTitle("Разгруппировать суперсет")
            .setMessage("Разгруппировать упражнения суперсета?")
            .setPositiveButton("Да", (dialog, which) -> {
                // Разгруппировка: просто ставим null для supersetGroupNumber
                for (SessionExerciseResponse ex : sessionExercises) {
                    if (ex.getSupersetGroupNumber() != null && ex.getSupersetGroupNumber() == supersetGroup) {
                        ex.setSupersetGroupNumber(null);
                    }
                }
                adapter.setExercises(sessionExercises);
                updateUI();
                Toast.makeText(this, "Суперсет разгруппирован", Toast.LENGTH_SHORT).show();
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

    private void showRestTimePicker(com.google.android.material.textfield.TextInputEditText restTimeInput, Integer currentValue) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_time_picker, null);
        android.widget.NumberPicker minutesPicker = dialogView.findViewById(R.id.minutesPicker);
        android.widget.NumberPicker secondsPicker = dialogView.findViewById(R.id.secondsPicker);

        minutesPicker.setMinValue(0);
        minutesPicker.setMaxValue(59);
        secondsPicker.setMinValue(0);
        secondsPicker.setMaxValue(59);

        if (currentValue != null && currentValue > 0) {
            minutesPicker.setValue(currentValue / 60);
            secondsPicker.setValue(currentValue % 60);
        } else {
            minutesPicker.setValue(1);
            secondsPicker.setValue(0);
        }

        new AlertDialog.Builder(this)
                .setTitle("Время отдыха")
                .setView(dialogView)
                .setPositiveButton("OK", (d, w) -> {
                    int min = minutesPicker.getValue();
                    int sec = secondsPicker.getValue();
                    restTimeInput.setText(String.format("%02d:%02d", min, sec));
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private String getInputText(com.google.android.material.textfield.TextInputEditText input) {
        return input.getText() != null ? input.getText().toString().trim() : "";
    }

    // Вспомогательный класс для записи дропсета
    private static class DropsetEntry {
        private final com.google.android.material.textfield.TextInputEditText weightInput;
        private final com.google.android.material.textfield.TextInputEditText repsInput;

        DropsetEntry(com.google.android.material.textfield.TextInputEditText weightInput,
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
                // Пропускаем элементы отдыха и дропсет-подходы (они не требуют заполнения)
                if (set.isRest() || set.isDropsetPart()) continue;

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
            // Отправляем номер суперсета (может быть null)
            completion.setSupersetGroupNumber(exercise.getSupersetGroupNumber());

            List<CompleteSessionRequest.CompletedSetData> setData = new ArrayList<>();
            if (exercise.getCompletedSets() != null) {
                List<SessionSetResponse> sortedSets = new ArrayList<>(exercise.getCompletedSets());
                sortedSets.sort((a, b) -> {
                    int oa = a.getSetNumber() != null ? a.getSetNumber() : 0;
                    int ob = b.getSetNumber() != null ? b.getSetNumber() : 0;
                    return Integer.compare(oa, ob);
                });

                // Собираем группы дропсетов
                Map<Long, List<SessionSetResponse>> dropsetGroups = new HashMap<>();
                for (SessionSetResponse set : sortedSets) {
                    if (set.getDropsetGroupId() != null) {
                        dropsetGroups.computeIfAbsent(set.getDropsetGroupId(), k -> new ArrayList<>()).add(set);
                    }
                }

                // Отправляем подходы, пропуская дропсет-подходы и элементы отдыха
                for (SessionSetResponse set : sortedSets) {
                    // Пропускаем элементы отдыха
                    if (set.isRest()) continue;

                    // Пропускаем дропсет-подходы (они уже учтены в основном подходе)
                    if (set.isDropsetPart()) continue;

                    CompleteSessionRequest.CompletedSetData data = new CompleteSessionRequest.CompletedSetData();
                    data.setWeight(set.getWeight());
                    data.setReps(set.getReps());
                    data.setDurationSeconds(set.getDurationSeconds());
                    data.setDistanceMeters(set.getDistanceMeters());
                    data.setSetOrder(set.getSetNumber());
                    data.setIsWarmup(set.isWarmup());

                    // Проверяем, есть ли связанные дропсет-подходы
                    Long groupId = set.getDropsetGroupId();
                    if (groupId != null && dropsetGroups.containsKey(groupId)) {
                        List<SessionSetResponse> groupSets = dropsetGroups.get(groupId);

                        // Собираем все дропсет-подходы (исключая сам основной и rest)
                        List<Double> dropsetWeights = new ArrayList<>();
                        List<Integer> dropsetRepsList = new ArrayList<>();
                        Integer restAfterDropset = null;

                        for (SessionSetResponse gs : groupSets) {
                            if (gs == set) continue; // Пропускаем сам основной
                            if (gs.isDropsetPart()) {
                                dropsetWeights.add(gs.getWeight());
                                dropsetRepsList.add(gs.getReps());
                            } else if (gs.isRest()) {
                                restAfterDropset = gs.getRestTime();
                            }
                        }

                        if (!dropsetWeights.isEmpty()) {
                            data.setIsDropset(true);
                            data.setDropsetWeight(dropsetWeights.get(0));
                            data.setDropsetReps(dropsetRepsList.get(0));
                            data.setDropsetWeights(dropsetWeights.size() > 1 ? dropsetWeights.subList(1, dropsetWeights.size()) : null);
                            data.setDropsetRepsList(dropsetRepsList.size() > 1 ? dropsetRepsList.subList(1, dropsetRepsList.size()) : null);
                        }
                        data.setRestTime(restAfterDropset);
                    } else {
                        // Обычный подход без дропсета
                        data.setIsDropset(false);
                    }

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
                            ", distance=" + sd.getDistanceMeters() +
                            ", isDropset=" + sd.getIsDropset());
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
    public void onBackPressed() {
        new AlertDialog.Builder(this)
            .setTitle("Выйти")
            .setMessage("Вы уверены, что хотите выйти? Данные будут потеряны.")
            .setPositiveButton("Выйти", (dialog, which) -> SessionActivity.super.onBackPressed())
            .setNegativeButton("Отмена", null)
            .show();
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
        stopSessionTimer();
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
            createSupersetButton.setVisibility(View.GONE);
        } else {
            completeSessionButton.setEnabled(true);
            completeSessionButton.setText("Завершить тренировку");
            // Показываем кнопку суперсета только если есть 2+ упражнения без суперсета
            int standaloneCount = 0;
            for (SessionExerciseResponse ex : sessionExercises) {
                if (ex.getSupersetGroupNumber() == null || ex.getSupersetGroupNumber() <= 0) {
                    standaloneCount++;
                }
            }
            createSupersetButton.setVisibility(standaloneCount >= 2 ? View.VISIBLE : View.GONE);
        }
    }
}
