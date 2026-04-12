package ru.squidory.trainingdairymobile.ui.trainings;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.squidory.trainingdairymobile.R;
import ru.squidory.trainingdairymobile.data.model.ExerciseResponse;
import ru.squidory.trainingdairymobile.data.model.PlannedSetResponse;
import ru.squidory.trainingdairymobile.data.model.WorkoutExerciseResponse;
import ru.squidory.trainingdairymobile.data.repository.ExerciseRepository;
import ru.squidory.trainingdairymobile.data.repository.ProgramRepository;
import ru.squidory.trainingdairymobile.ui.sessions.SessionActivity;

/**
 * Activity для просмотра содержимого тренировки — упражнения с подходами.
 */
public class WorkoutContentActivity extends AppCompatActivity {

    public static final String EXTRA_WORKOUT_ID = "workout_id";
    public static final String EXTRA_WORKOUT_NAME = "workout_name";
    public static final String EXTRA_WORKOUT_COMMENT = "workout_comment";

    private MaterialToolbar toolbar;
    private TextView workoutCommentText;
    private RecyclerView exercisesRecyclerView;
    private ProgressBar progressBar;
    private TextView emptyText;
    private MaterialButton startSessionButton;

    private WorkoutContentAdapter adapter;
    private ProgramRepository programRepository;
    private ExerciseRepository exerciseRepository;
    private long workoutId;
    private String workoutName;
    private final Map<Long, ExerciseResponse> exerciseMap = new HashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_content);

        workoutId = getIntent().getLongExtra(EXTRA_WORKOUT_ID, -1);
        workoutName = getIntent().getStringExtra(EXTRA_WORKOUT_NAME);

        if (workoutId == -1) {
            finish();
            return;
        }

        programRepository = ProgramRepository.getInstance();
        exerciseRepository = ExerciseRepository.getInstance();

        initViews();
        setupToolbar();
        setupRecyclerView();
        loadWorkoutContent();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        workoutCommentText = findViewById(R.id.workoutCommentText);
        exercisesRecyclerView = findViewById(R.id.exercisesRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyText = findViewById(R.id.emptyText);
        startSessionButton = findViewById(R.id.startSessionButton);

        // Отображаем комментарий если есть
        String comment = getIntent().getStringExtra(EXTRA_WORKOUT_COMMENT);
        if (comment != null && !comment.isEmpty()) {
            workoutCommentText.setText(comment);
            workoutCommentText.setVisibility(View.VISIBLE);
        }

        // Кнопка "Начать тренировку"
        startSessionButton.setOnClickListener(v -> showStartSessionDialog());
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(workoutName != null ? workoutName : "Тренировка");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new WorkoutContentAdapter();
        adapter.setContext(this);
        exercisesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        exercisesRecyclerView.setAdapter(adapter);
    }

    private void loadWorkoutContent() {
        // Сначала загружаем все упражнения для мапы имён
        exerciseRepository.getExercises(null, null, null, new ru.squidory.trainingdairymobile.data.repository.ExerciseRepository.ExercisesCallback() {
            @Override
            public void onSuccess(List<ExerciseResponse> exercises) {
                exerciseMap.clear();
                for (ExerciseResponse ex : exercises) {
                    exerciseMap.put(ex.getId(), ex);
                }
                adapter.setExerciseMap(exerciseMap);
                loadWorkoutExercises();
            }

            @Override
            public void onError(String error) {
                // Даже если не удалось загрузить упражнения, пробуем загрузить тренировку
                loadWorkoutExercises();
            }
        });
    }

    private void loadWorkoutExercises() {
        showLoading(true);
        programRepository.getWorkoutExercises(workoutId, new ProgramRepository.WorkoutExercisesCallback() {
            @Override
            public void onSuccess(List<WorkoutExerciseResponse> exercises) {
                adapter.setExercises(exercises);

                // Загружаем подходы для каждого упражнения
                Map<Long, List<PlannedSetResponse>> setsByExerciseId = new HashMap<>();
                final int totalCount = exercises.size();
                final int[] completed = {0};

                if (totalCount == 0) {
                    showLoading(false);
                    checkEmptyState();
                    return;
                }

                for (WorkoutExerciseResponse ex : exercises) {
                    programRepository.getPlannedSets(ex.getId(), new ProgramRepository.PlannedSetsCallback() {
                        @Override
                        public void onSuccess(List<PlannedSetResponse> sets) {
                            setsByExerciseId.put(ex.getId(), sets);
                            completed[0]++;
                            if (completed[0] == totalCount) {
                                adapter.setSetsByExerciseId(setsByExerciseId);
                                showLoading(false);
                                checkEmptyState();
                            }
                        }

                        @Override
                        public void onError(String error) {
                            setsByExerciseId.put(ex.getId(), null);
                            completed[0]++;
                            if (completed[0] == totalCount) {
                                adapter.setSetsByExerciseId(setsByExerciseId);
                                showLoading(false);
                                checkEmptyState();
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                checkEmptyState();
            }
        });
    }

    private void showLoading(boolean show) {
        if (show) {
            progressBar.setVisibility(android.view.View.VISIBLE);
            exercisesRecyclerView.setVisibility(android.view.View.GONE);
            emptyText.setVisibility(android.view.View.GONE);
        } else {
            progressBar.setVisibility(android.view.View.GONE);
            exercisesRecyclerView.setVisibility(android.view.View.VISIBLE);
        }
    }

    private void checkEmptyState() {
        if (adapter.getItemCount() == 0) {
            exercisesRecyclerView.setVisibility(android.view.View.GONE);
            emptyText.setVisibility(android.view.View.VISIBLE);
        } else {
            exercisesRecyclerView.setVisibility(android.view.View.VISIBLE);
            emptyText.setVisibility(android.view.View.GONE);
        }
    }

    private void showStartSessionDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Начать тренировку")
                .setMessage("Вы готовы начать тренировку \"" + workoutName + "\"?")
                .setPositiveButton("Начать", (dialog, which) -> startSession())
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void startSession() {
        try {
            Log.d("WorkoutContent", "Starting session for workoutId=" + workoutId + ", workoutName=" + workoutName);
            Intent intent = new Intent(this, SessionActivity.class);
            intent.putExtra(SessionActivity.EXTRA_WORKOUT_ID, workoutId);
            intent.putExtra(SessionActivity.EXTRA_WORKOUT_NAME, workoutName);
            startActivity(intent);
        } catch (Exception e) {
            Log.e("WorkoutContent", "Failed to start SessionActivity", e);
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Ошибка")
                    .setMessage("Не удалось открыть сессию: " + e.getMessage())
                    .setPositiveButton("OK", null)
                    .show();
        }
    }
}
