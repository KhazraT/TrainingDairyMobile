package ru.squidory.trainingdairymobile.ui.trainings;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.ArrayList;
import java.util.List;
import ru.squidory.trainingdairymobile.R;
import ru.squidory.trainingdairymobile.data.model.ExerciseResponse;
import ru.squidory.trainingdairymobile.data.model.PlannedSetRequest;
import ru.squidory.trainingdairymobile.data.model.PlannedSetResponse;
import ru.squidory.trainingdairymobile.data.model.WorkoutExerciseRequest;
import ru.squidory.trainingdairymobile.data.model.WorkoutExerciseResponse;
import ru.squidory.trainingdairymobile.data.repository.ExerciseRepository;
import ru.squidory.trainingdairymobile.data.repository.ProgramRepository;

/**
 * Activity для управления упражнениями в тренировке.
 * - Добавление упражнений через диалог с чекбоксами
 * - Объединение упражнений в суперсеты
 * - Планирование подходов (вес, повторения, время, дистанция)
 * - Дропсеты с объединением записей
 */
public class ExerciseManagementActivity extends AppCompatActivity {

    public static final String EXTRA_WORKOUT_ID = "workout_id";
    public static final String EXTRA_WORKOUT_NAME = "workout_name";

    private MaterialToolbar toolbar;
    private RecyclerView exercisesRecyclerView;
    private ProgressBar progressBar;
    private TextView emptyExercisesText;
    private ImageButton addExerciseButton;
    private MaterialButton createSupersetButton;

    private WorkoutExerciseAdapter exerciseAdapter;
    private ProgramRepository programRepository;
    private ExerciseRepository exerciseRepository;
    private long workoutId;
    private String workoutName;

    // Все доступные упражнения
    private java.util.Map<Long, ExerciseResponse> exerciseMap = new java.util.HashMap<>();

    private ActivityResultLauncher<Intent> exercisePickerLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_management);

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

        workoutId = getIntent().getLongExtra(EXTRA_WORKOUT_ID, -1);
        workoutName = getIntent().getStringExtra(EXTRA_WORKOUT_NAME);

        if (workoutId == -1) {
            Toast.makeText(this, "Ошибка: тренировка не найдена", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        programRepository = ProgramRepository.getInstance();
        exerciseRepository = ExerciseRepository.getInstance();

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupListeners();

        // Сначала загружаем все упражнения (для названий), потом — упражнения тренировки
        loadAllExercisesAndThen(this::loadWorkoutExercises);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        exercisesRecyclerView = findViewById(R.id.exercisesRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyExercisesText = findViewById(R.id.emptyExercisesText);
        addExerciseButton = findViewById(R.id.addExerciseButton);
        createSupersetButton = findViewById(R.id.createSupersetButton);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(workoutName != null ? workoutName : "Упражнения");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        exerciseAdapter = new WorkoutExerciseAdapter();
        exercisesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        exercisesRecyclerView.setAdapter(exerciseAdapter);

        exerciseAdapter.setOnExerciseActionListener(new WorkoutExerciseAdapter.OnExerciseActionListener() {
            @Override
            public void onManageSets(WorkoutExerciseResponse exercise) {
                openSetsManagementDialog(exercise);
            }

            @Override
            public void onDeleteExercise(WorkoutExerciseResponse exercise) {
                showDeleteExerciseConfirmation(exercise);
            }

            @Override
            public void onEditExercise(WorkoutExerciseResponse exercise) {
                openSetsManagementDialog(exercise);
            }

            @Override
            public void onDeleteSuperset(int supersetGroup) {
                showDeleteSupersetConfirmation(supersetGroup);
            }
        });
        exerciseAdapter.setOnSupersetExerciseMoved(new WorkoutExerciseAdapter.OnSupersetExerciseMoved() {
            @Override
            public void onExerciseMovedWithinSuperset(int supersetGroup, int fromOrder, int toOrder) {
                List<WorkoutExerciseResponse> currentExercises = exerciseAdapter.getAllExercises();
                queueOrderUpdate(currentExercises);
            }
        });

        // Drag-and-drop на уровне групп
        ItemTouchHelper.Callback dragCallback = new ItemTouchHelper.Callback() {
            private boolean hasMoved = false;

            @Override
            public boolean isLongPressDragEnabled() { return true; }

            @Override
            public boolean isItemViewSwipeEnabled() { return false; }

            @Override
            public int getMovementFlags(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh) {
                return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh, @NonNull RecyclerView.ViewHolder target) {
                int fromPos = vh.getAdapterPosition();
                int toPos = target.getAdapterPosition();
                exerciseAdapter.moveDisplayItem(fromPos, toPos);
                hasMoved = true;
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int direction) {}

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                // Вызывается когда пользователь отпустил элемент — сохраняем порядок
                if (hasMoved) {
                    List<WorkoutExerciseResponse> currentExercises = exerciseAdapter.getAllExercises();
                    queueOrderUpdate(currentExercises);
                    hasMoved = false;
                }
                // Отключаем авто-скролл после завершения drag
                recyclerView.stopScroll();
            }

            @Override
            public float getMoveThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
                return 0.2f; // Увеличиваем порог для более стабильного drag
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(dragCallback);
        itemTouchHelper.attachToRecyclerView(exercisesRecyclerView);
    }

    private void setupListeners() {
        addExerciseButton.setOnClickListener(v -> showSelectExercisesDialog());
        createSupersetButton.setOnClickListener(v -> showCreateSupersetDialog());
    }

    // ==================== Загрузка данных ====================

    private java.util.concurrent.atomic.AtomicBoolean orderUpdateInProgress = new java.util.concurrent.atomic.AtomicBoolean(false);
    private List<WorkoutExerciseResponse> pendingOrderUpdate = null;

    /**
     * Поставить обновление order в очередь.
     * Если уже идёт обновление — данные будут использованы после завершения текущего.
     */
    private void queueOrderUpdate(List<WorkoutExerciseResponse> exercises) {
        pendingOrderUpdate = new ArrayList<>(exercises);
        if (orderUpdateInProgress.compareAndSet(false, true)) {
            // Запускаем обновление
            runOrderUpdate();
        }
    }

    private void runOrderUpdate() {
        List<WorkoutExerciseResponse> toUpdate = pendingOrderUpdate;
        pendingOrderUpdate = null;
        updateExerciseOrdersAllPhase1(toUpdate, 0, new int[]{0}, new int[]{0}, new Runnable() {
            @Override
            public void run() {
                orderUpdateInProgress.set(false);
                if (pendingOrderUpdate != null) {
                    queueOrderUpdate(pendingOrderUpdate);
                }
            }
        });
    }

    /** Фаза 1: ВСЕ упражнения → временные order */
    private void updateExerciseOrdersAllPhase1(List<WorkoutExerciseResponse> exercises, int index,
                                               int[] completed, int[] errorCount, Runnable onDone) {
        if (index >= exercises.size()) {
            // Все Phase 1 завершены → запускаем Phase 2
            Log.d("ExerciseMgmt", "All Phase 1 done, starting Phase 2");
            updateExerciseOrdersAllPhase2(exercises, 0, completed, errorCount, onDone);
            return;
        }

        final WorkoutExerciseResponse ex = exercises.get(index);
        int tempOrder = 100000 + (int) ex.getId();

        WorkoutExerciseRequest request = new WorkoutExerciseRequest();
        request.setExerciseId(ex.getExerciseId());
        request.setSetsCount(ex.getSetsCount());
        request.setSetType(ex.getSetType());
        request.setSupersetGroupNumber(ex.getSupersetGroupNumber());
        request.setExerciseOrder(tempOrder);

        Log.d("ExerciseMgmt", "  P1[" + index + "]: id=" + ex.getId() + " → order=" + tempOrder);

        programRepository.updateWorkoutExercise(ex.getId(), request,
                new ProgramRepository.WorkoutExerciseCallback() {
                    @Override
                    public void onSuccess(WorkoutExerciseResponse updated) {
                        Log.d("ExerciseMgmt", "  P1[" + index + "] OK");
                        updateExerciseOrdersAllPhase1(exercises, index + 1, completed, errorCount, onDone);
                    }

                    @Override
                    public void onError(String error) {
                        completed[0]++;
                        errorCount[0]++;
                        Log.e("ExerciseMgmt", "  P1[" + index + "] FAIL: " + error.substring(0, Math.min(150, error.length())));
                        updateExerciseOrdersAllPhase1(exercises, index + 1, completed, errorCount, onDone);
                    }
                });
    }

    /** Фаза 2: ВСЕ упражнения → финальные order */
    private void updateExerciseOrdersAllPhase2(List<WorkoutExerciseResponse> exercises, int index,
                                               int[] completed, int[] errorCount, Runnable onDone) {
        if (index >= exercises.size()) {
            Log.d("ExerciseMgmt", "updateExerciseOrders done: completed=" + completed[0] + ", errors=" + errorCount[0]);
            if (onDone != null) onDone.run();
            return;
        }

        final WorkoutExerciseResponse ex = exercises.get(index);
        int targetOrder = ex.getExerciseOrder() != null ? ex.getExerciseOrder() : index + 1;

        WorkoutExerciseRequest request = new WorkoutExerciseRequest();
        request.setExerciseId(ex.getExerciseId());
        request.setSetsCount(ex.getSetsCount());
        request.setSetType(ex.getSetType());
        request.setSupersetGroupNumber(ex.getSupersetGroupNumber());
        request.setExerciseOrder(targetOrder);

        Log.d("ExerciseMgmt", "  P2[" + index + "]: id=" + ex.getId() + " → order=" + targetOrder);

        programRepository.updateWorkoutExercise(ex.getId(), request,
                new ProgramRepository.WorkoutExerciseCallback() {
                    @Override
                    public void onSuccess(WorkoutExerciseResponse updated) {
                        Log.d("ExerciseMgmt", "  P2[" + index + "] OK");
                        completed[0]++;
                        updateExerciseOrdersAllPhase2(exercises, index + 1, completed, errorCount, onDone);
                    }

                    @Override
                    public void onError(String error) {
                        completed[0]++;
                        errorCount[0]++;
                        Log.e("ExerciseMgmt", "  P2[" + index + "] FAIL: " + error.substring(0, Math.min(150, error.length())));
                        updateExerciseOrdersAllPhase2(exercises, index + 1, completed, errorCount, onDone);
                    }
                });
    }

    /**
     * Загрузить все упражнения и затем вызвать callback.
     */
    private void loadAllExercisesAndThen(Runnable then) {
        exerciseRepository.getExercises(null, null, null, new ExerciseRepository.ExercisesCallback() {
            @Override
            public void onSuccess(List<ExerciseResponse> exercises) {
                exerciseMap.clear();
                for (ExerciseResponse ex : exercises) {
                    exerciseMap.put(ex.getId(), ex);
                }
                exerciseAdapter.setExerciseMap(exerciseMap);
                then.run();
            }

            @Override
            public void onError(String error) {
                // Продолжаем даже без мапы — будут "Упражнение" вместо названий
                then.run();
            }
        });
    }

    /**
     * Найти тип упражнения по exerciseId из мапы.
     */
    private String getExerciseTypeById(long exerciseId) {
        ExerciseResponse ex = exerciseMap.get(exerciseId);
        return ex != null ? ex.getExerciseType() : null;
    }

    /**
     * Получить список ID уже добавленных упражнений.
     */
    private ArrayList<Long> getExistingExerciseIds() {
        return new ArrayList<>(exerciseAdapter.getExistingExerciseIds());
    }

    private void loadWorkoutExercises() {
        showLoading(true);
        programRepository.getWorkoutExercises(workoutId, new ProgramRepository.WorkoutExercisesCallback() {
            @Override
            public void onSuccess(List<WorkoutExerciseResponse> exercises) {
                // Загружаем planned sets для каждого упражнения чтобы получить реальное количество
                loadSetsCountForExercises(exercises);
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                Toast.makeText(ExerciseManagementActivity.this,
                        "Ошибка загрузки: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Загрузить planned sets для каждого упражнения и обновить setsCount.
     */
    private void loadSetsCountForExercises(List<WorkoutExerciseResponse> exercises) {
        if (exercises.isEmpty()) {
            showLoading(false);
            exerciseAdapter.setExercises(exercises);
            checkEmptyState();
            updateSupersetButton(exercises);
            return;
        }

        final int totalCount = exercises.size();
        final int[] completed = {0};

        for (final WorkoutExerciseResponse ex : exercises) {
            programRepository.getPlannedSets(ex.getId(), new ProgramRepository.PlannedSetsCallback() {
                @Override
                public void onSuccess(List<PlannedSetResponse> sets) {
                    // Считаем количество подходов
                    ex.setSetsCount(sets.size());
                    completed[0]++;
                    if (completed[0] == totalCount) {
                        showLoading(false);
                        exerciseAdapter.setExercises(exercises);
                        checkEmptyState();
                        updateSupersetButton(exercises);
                    }
                }

                @Override
                public void onError(String error) {
                    ex.setSetsCount(0);
                    completed[0]++;
                    if (completed[0] == totalCount) {
                        showLoading(false);
                        exerciseAdapter.setExercises(exercises);
                        checkEmptyState();
                        updateSupersetButton(exercises);
                    }
                }
            });
        }
    }

    private void updateSupersetButton(List<WorkoutExerciseResponse> exercises) {
        // Показываем кнопку только если есть 2+ упражнения без суперсета
        int standaloneCount = 0;
        for (WorkoutExerciseResponse ex : exercises) {
            if (ex.getSupersetGroupNumber() == null || ex.getSupersetGroupNumber() <= 0) {
                standaloneCount++;
            }
        }
        createSupersetButton.setVisibility(standaloneCount >= 2 ? View.VISIBLE : View.GONE);
    }

    // ==================== Выбор упражнений через ExercisePickerActivity ====================

    private void showSelectExercisesDialog() {
        Intent intent = new Intent(this, ExercisePickerActivity.class);
        exercisePickerLauncher.launch(intent);
    }

    private void addSelectedExercises(List<Long> exerciseIds) {
        List<WorkoutExerciseResponse> currentExercises = exerciseAdapter.getAllExercises();
        int startOrder = currentExercises.size() + 1;
        final int totalCount = exerciseIds.size();
        final int[] completed = {0};
        final boolean[] hasError = {false};

        for (int i = 0; i < exerciseIds.size(); i++) {
            long exerciseId = exerciseIds.get(i);
            final int index = i;

            WorkoutExerciseRequest request = new WorkoutExerciseRequest();
            request.setExerciseId(exerciseId);
            request.setSetsCount(3);
            request.setSetType("REGULAR");
            request.setSupersetGroupNumber(null);
            request.setExerciseOrder(startOrder + i);

            programRepository.addExerciseToWorkout(workoutId, request,
                    new ProgramRepository.WorkoutExerciseCallback() {
                        @Override
                        public void onSuccess(WorkoutExerciseResponse exercise) {
                            completed[0]++;
                            if (completed[0] == totalCount && !hasError[0]) {
                                Toast.makeText(ExerciseManagementActivity.this,
                                        R.string.exercise_added, Toast.LENGTH_SHORT).show();
                                loadWorkoutExercises();
                            }
                        }

                        @Override
                        public void onError(String error) {
                            completed[0]++;
                            hasError[0] = true;
                            if (completed[0] == totalCount) {
                                Toast.makeText(ExerciseManagementActivity.this,
                                        "Часть упражнений не добавлена: " + error, Toast.LENGTH_LONG).show();
                                loadWorkoutExercises();
                            }
                        }
                    });
        }
    }

    // ==================== Создание суперсета ====================

    private void showCreateSupersetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_superset, null);
        builder.setView(dialogView);

        RecyclerView supersetSelectionRecyclerView = dialogView.findViewById(R.id.supersetSelectionRecyclerView);
        MaterialButton cancelButton = dialogView.findViewById(R.id.dialogCancelButton);
        MaterialButton saveButton = dialogView.findViewById(R.id.dialogSaveButton);

        // Показываем только упражнения без суперсета
        List<WorkoutExerciseResponse> currentExercises = exerciseAdapter.getAllExercises();
        List<WorkoutExerciseResponse> standaloneExercises = new ArrayList<>();
        for (WorkoutExerciseResponse ex : currentExercises) {
            if (ex.getSupersetGroupNumber() == null || ex.getSupersetGroupNumber() <= 0) {
                standaloneExercises.add(ex);
            }
        }

        SupersetExerciseAdapter selectionAdapter = new SupersetExerciseAdapter();
        selectionAdapter.setExerciseMap(exerciseMap);
        supersetSelectionRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        supersetSelectionRecyclerView.setAdapter(selectionAdapter);

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        // Сначала устанавливаем слушатель, потом упражнения — чтобы кнопка сразу стала disabled
        selectionAdapter.setOnSelectionChanged(() -> {
            runOnUiThread(() -> saveButton.setEnabled(selectionAdapter.getSelectedCount() >= 2));
        });
        selectionAdapter.setExercisesAndNotifySelection(standaloneExercises);

        saveButton.setOnClickListener(v -> {
            List<WorkoutExerciseResponse> selected = selectionAdapter.getSelectedExercises();
            if (selected.size() < 2) return;

            // Определяем новый номер суперсета
            int maxGroup = 0;
            for (WorkoutExerciseResponse ex : currentExercises) {
                Integer g = ex.getSupersetGroupNumber();
                if (g != null && g > maxGroup) maxGroup = g;
            }
            final int newSupersetGroup = maxGroup + 1;

            updateExercisesToSuperset(selected, newSupersetGroup, dialog);
        });

        dialog.show();
    }

    private void updateExercisesToSuperset(List<WorkoutExerciseResponse> exercises, int supersetGroup,
                                           AlertDialog dialog) {
        final int[] completed = {0};
        final int totalCount = exercises.size();
        final boolean[] hasError = {false};

        for (WorkoutExerciseResponse ex : exercises) {
            WorkoutExerciseRequest request = new WorkoutExerciseRequest();
            request.setExerciseId(ex.getExerciseId());
            request.setSetsCount(ex.getSetsCount());
            request.setSetType(ex.getSetType());
            request.setSupersetGroupNumber(supersetGroup);
            request.setExerciseOrder(ex.getExerciseOrder());

            programRepository.updateWorkoutExercise(ex.getId(), request,
                    new ProgramRepository.WorkoutExerciseCallback() {
                        @Override
                        public void onSuccess(WorkoutExerciseResponse updated) {
                            completed[0]++;
                            if (completed[0] == totalCount && !hasError[0]) {
                                dialog.dismiss();
                                Toast.makeText(ExerciseManagementActivity.this,
                                        "Суперсет создан", Toast.LENGTH_SHORT).show();
                                loadWorkoutExercises();
                            }
                        }

                        @Override
                        public void onError(String error) {
                            completed[0]++;
                            hasError[0] = true;
                            if (completed[0] == totalCount) {
                                Toast.makeText(ExerciseManagementActivity.this,
                                        "Ошибка: " + error, Toast.LENGTH_LONG).show();
                                loadWorkoutExercises();
                            }
                        }
                    });
        }
    }

    // ==================== Удаление суперсета ====================

    private void showDeleteSupersetConfirmation(int supersetGroup) {
        List<WorkoutExerciseResponse> supersetExercises = exerciseAdapter.getSupersetExercises(supersetGroup);
        new AlertDialog.Builder(this)
                .setTitle("Разгруппировать суперсет?")
                .setMessage("Упражнения останутся в тренировке, но будут разгруппированы. (" + supersetExercises.size() + " упражнений)")
                .setPositiveButton("Разгруппировать", (dialog, which) -> {
                    removeSupersetGroup(supersetGroup);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void removeSupersetGroup(int supersetGroup) {
        List<WorkoutExerciseResponse> supersetExercises = exerciseAdapter.getSupersetExercises(supersetGroup);
        if (supersetExercises.isEmpty()) {
            Log.w("ExerciseMgmt", "removeSupersetGroup: no exercises found for group " + supersetGroup);
            Toast.makeText(this, "Ошибка: упражнения не найдены", Toast.LENGTH_SHORT).show();
            loadWorkoutExercises();
            return;
        }

        final int totalCount = supersetExercises.size();
        final int[] completed = {0};
        final int[] errorCount = {0};
        final String[] lastError = {""};

        Log.d("ExerciseMgmt", "removeSupersetGroup: group=" + supersetGroup + ", count=" + totalCount);

        // Обрабатываем последовательно через Handler
        processUnsupersetSequentially(new ArrayList<>(supersetExercises), 0, completed, errorCount, lastError,
                new Runnable() {
                    @Override
                    public void run() {
                        if (errorCount[0] == 0) {
                            Toast.makeText(ExerciseManagementActivity.this,
                                    "Суперсет разгруппирован", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ExerciseManagementActivity.this,
                                    "Разгруппировано с ошибками: " + errorCount[0] + "/" + totalCount
                                            + "\n" + lastError[0], Toast.LENGTH_LONG).show();
                        }
                        Log.d("ExerciseMgmt", "Done: completed=" + completed[0] + ", errors=" + errorCount[0]);
                        loadWorkoutExercises();
                    }
                });
    }

    /**
     * Последовательно разгруппировывает каждое упражнение:
     * 1. Phase 1: supersetGroupNumber=null, order=временный
     * 2. Phase 2: supersetGroupNumber=null, order=оригинальный
     */
    private void processUnsupersetSequentially(List<WorkoutExerciseResponse> exercises, final int index,
                                               final int[] completed, final int[] errorCount,
                                               final String[] lastError, final Runnable onDone) {
        if (index >= exercises.size()) {
            onDone.run();
            return;
        }

        final WorkoutExerciseResponse ex = exercises.get(index);
        final int originalOrder = ex.getExerciseOrder() != null ? ex.getExerciseOrder() : 0;

        Log.d("ExerciseMgmt", "[" + index + "] Phase 1: id=" + ex.getId()
                + ", order=" + originalOrder + ", setGroup=null, tempOrder=" + (10000 + ex.getId()));

        // Phase 1
        WorkoutExerciseRequest req1 = new WorkoutExerciseRequest();
        req1.setExerciseId(ex.getExerciseId());
        req1.setSetsCount(ex.getSetsCount());
        req1.setSetType(ex.getSetType());
        req1.setSupersetGroupNumber(null);
        req1.setExerciseOrder(10000 + (int) ex.getId());

        programRepository.updateWorkoutExercise(ex.getId(), req1,
                new ProgramRepository.WorkoutExerciseCallback() {
                    @Override
                    public void onSuccess(WorkoutExerciseResponse updated) {
                        Log.d("ExerciseMgmt", "[" + index + "] Phase 1 OK");

                        // Phase 2
                        WorkoutExerciseRequest req2 = new WorkoutExerciseRequest();
                        req2.setExerciseId(ex.getExerciseId());
                        req2.setSetsCount(ex.getSetsCount());
                        req2.setSetType(ex.getSetType());
                        req2.setSupersetGroupNumber(null);
                        req2.setExerciseOrder(originalOrder);

                        Log.d("ExerciseMgmt", "[" + index + "] Phase 2: order=" + originalOrder);

                        programRepository.updateWorkoutExercise(ex.getId(), req2,
                                new ProgramRepository.WorkoutExerciseCallback() {
                                    @Override
                                    public void onSuccess(WorkoutExerciseResponse updated2) {
                                        Log.d("ExerciseMgmt", "[" + index + "] Phase 2 OK");
                                        completed[0]++;
                                        processUnsupersetSequentially(exercises, index + 1, completed, errorCount, lastError, onDone);
                                    }

                                    @Override
                                    public void onError(String error) {
                                        Log.e("ExerciseMgmt", "[" + index + "] Phase 2 FAIL: " + error);
                                        completed[0]++;
                                        errorCount[0]++;
                                        lastError[0] = error.substring(0, Math.min(200, error.length()));
                                        processUnsupersetSequentially(exercises, index + 1, completed, errorCount, lastError, onDone);
                                    }
                                });
                    }

                    @Override
                    public void onError(String error) {
                        Log.e("ExerciseMgmt", "[" + index + "] Phase 1 FAIL: " + error);
                        completed[0]++;
                        errorCount[0]++;
                        lastError[0] = error.substring(0, Math.min(200, error.length()));
                        processUnsupersetSequentially(exercises, index + 1, completed, errorCount, lastError, onDone);
                    }
                });
    }

    // ==================== Удаление упражнения ====================

    private void showDeleteExerciseConfirmation(WorkoutExerciseResponse exercise) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_exercise)
                .setMessage(R.string.delete_exercise_confirm_workout)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    programRepository.deleteWorkoutExercise(exercise.getId(),
                            new ProgramRepository.SimpleCallback() {
                                @Override
                                public void onSuccess() {
                                    Toast.makeText(ExerciseManagementActivity.this,
                                            R.string.exercise_deleted, Toast.LENGTH_SHORT).show();
                                    loadWorkoutExercises();
                                }

                                @Override
                                public void onError(String error) {
                                    Toast.makeText(ExerciseManagementActivity.this,
                                            "Ошибка: " + error, Toast.LENGTH_LONG).show();
                                }
                            });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    // ==================== Управление подходами ====================

    private void openSetsManagementDialog(WorkoutExerciseResponse exercise) {
        String exerciseType = getExerciseTypeById(exercise.getExerciseId());
        boolean isRepsWeight = "REPS_WEIGHT".equals(exerciseType);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_manage_sets, null);
        builder.setView(dialogView);

        RecyclerView setsRecyclerView = dialogView.findViewById(R.id.setsRecyclerView);
        MaterialButton addRegularSetButton = dialogView.findViewById(R.id.addRegularSetButton);
        MaterialButton addDropsetButton = dialogView.findViewById(R.id.addDropsetButton);
        MaterialButton saveSetsButton = dialogView.findViewById(R.id.saveSetsButton);
        MaterialButton closeSetsButton = dialogView.findViewById(R.id.closeSetsButton);

        // Для не-REPS_WEIGHT упражнений — только одна кнопка, переименовываем
        if (!isRepsWeight) {
            addRegularSetButton.setText("Добавить подход");
            addDropsetButton.setVisibility(View.GONE);
        } else {
            addRegularSetButton.setText("+ Обычный");
            addDropsetButton.setVisibility(View.VISIBLE);
        }

        // Локальный буфер — изменения не отправляются на сервер до нажатия "Сохранить"
        final List<PlannedSetResponse> localSets = new ArrayList<>();
        // ID подходов, которые были на сервере но удалены локально
        final List<Long> deletedSetIds = new ArrayList<>();
        // Флаг что данные загружены
        final boolean[] loaded = {false};

        PlannedSetAdapter setAdapter = new PlannedSetAdapter();
        setsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        setsRecyclerView.setAdapter(setAdapter);

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);

        // Загружаем подходы в локальный буфер
        programRepository.getPlannedSets(exercise.getId(), new ProgramRepository.PlannedSetsCallback() {
            @Override
            public void onSuccess(List<PlannedSetResponse> sets) {
                sets.sort((a, b) -> {
                    Integer numA = a.getSetNumber();
                    Integer numB = b.getSetNumber();
                    if (numA == null) numA = 0;
                    if (numB == null) numB = 0;
                    return numA.compareTo(numB);
                });
                localSets.clear();
                localSets.addAll(sets);
                setAdapter.setSets(new ArrayList<>(localSets));
                loaded[0] = true;
            }

            @Override
            public void onError(String error) {
                localSets.clear();
                setAdapter.setSets(new ArrayList<>());
                loaded[0] = true;
            }
        });

        setAdapter.setOnSetActionListener(new PlannedSetAdapter.OnSetActionListener() {
            @Override
            public void onEditSet(PlannedSetResponse set) {
                boolean isDropset = "DROPSET".equalsIgnoreCase(set.getSetType());
                // Передаём локальный буфер для изменений, а не адаптер сервера
                SetsDialogManager.showForEdit(ExerciseManagementActivity.this, exercise, exerciseType,
                        set, localSets, setAdapter, isDropset);
            }

            @Override
            public void onDeleteSet(PlannedSetResponse set) {
                // Удаляем только из локального буфера
                localSets.remove(set);
                if (set.getId() > 0) {
                    deletedSetIds.add(set.getId());
                }
                setAdapter.setSets(new ArrayList<>(localSets));
                Toast.makeText(ExerciseManagementActivity.this, "Подход удалён (локально)", Toast.LENGTH_SHORT).show();
            }
        });

        addRegularSetButton.setOnClickListener(v -> {
            SetsDialogManager.showForAdd(ExerciseManagementActivity.this, exercise, exerciseType,
                    localSets, setAdapter, false);
        });
        if (isRepsWeight) {
            addDropsetButton.setOnClickListener(v -> {
                SetsDialogManager.showForAdd(ExerciseManagementActivity.this, exercise, exerciseType,
                        localSets, setAdapter, true);
            });
        }

        saveSetsButton.setOnClickListener(v -> {
            if (!loaded[0]) {
                dialog.dismiss();
                return;
            }
            // Синхронизируем локальные изменения с сервером
            syncSetsWithServer(exercise, localSets, deletedSetIds, new Runnable() {
                @Override
                public void run() {
                    dialog.dismiss();
                    Toast.makeText(ExerciseManagementActivity.this, R.string.sets_saved, Toast.LENGTH_SHORT).show();
                    loadWorkoutExercises();
                }
            });
        });

        closeSetsButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    /**
     * Синхронизирует локальный список подходов с сервером:
     * 1. Удаляет подходы из deletedSetIds
     * 2. Обновляет существующие подходы (id > 0)
     * 3. Создаёт новые подходы (id == 0)
     */
    private void syncSetsWithServer(WorkoutExerciseResponse exercise,
                                    List<PlannedSetResponse> localSets,
                                    List<Long> deletedSetIds,
                                    Runnable onDone) {
        final int totalDelete = deletedSetIds.size();
        final int totalUpdate = (int) localSets.stream().filter(s -> s.getId() > 0).count();
        final int totalCreate = (int) localSets.stream().filter(s -> s.getId() == 0).count();
        final int totalOps = totalDelete + totalUpdate + totalCreate;

        if (totalOps == 0) {
            onDone.run();
            return;
        }

        final int[] completed = {0};
        final int[] errors = {0};

        // Фаза 1: Удаление
        for (Long setId : deletedSetIds) {
            programRepository.deletePlannedSet(setId, new ProgramRepository.SimpleCallback() {
                @Override
                public void onSuccess() {
                    completed[0]++;
                    if (completed[0] == totalOps && errors[0] == 0) onDone.run();
                }

                @Override
                public void onError(String error) {
                    completed[0]++;
                    errors[0]++;
                    if (completed[0] == totalOps) onDone.run();
                }
            });
        }

        // Фаза 2: Обновление существующих и создание новых
        for (final PlannedSetResponse localSet : localSets) {
            PlannedSetRequest request = new PlannedSetRequest();
            request.setSetType(localSet.getSetType());
            request.setSetNumber(localSet.getSetNumber());
            request.setTargetWeight(localSet.getTargetWeight());
            request.setTargetReps(localSet.getTargetReps());
            request.setTargetTime(localSet.getTargetTime());
            request.setTargetDistance(localSet.getTargetDistance());
            request.setDropsetEntries(localSet.getDropsetEntries() != null ?
                    convertDropsetEntries(localSet.getDropsetEntries()) : null);

            if (localSet.getId() > 0) {
                // Обновление
                programRepository.updatePlannedSet(localSet.getId(), request,
                        new retrofit2.Callback<PlannedSetResponse>() {
                            @Override
                            public void onResponse(retrofit2.Call<PlannedSetResponse> call, retrofit2.Response<PlannedSetResponse> response) {
                                completed[0]++;
                                if (completed[0] == totalOps && errors[0] == 0) onDone.run();
                            }

                            @Override
                            public void onFailure(retrofit2.Call<PlannedSetResponse> call, Throwable t) {
                                completed[0]++;
                                errors[0]++;
                                if (completed[0] == totalOps) onDone.run();
                            }
                        });
            } else {
                // Создание
                programRepository.createPlannedSet(exercise.getId(), request,
                        new retrofit2.Callback<PlannedSetResponse>() {
                            @Override
                            public void onResponse(retrofit2.Call<PlannedSetResponse> call, retrofit2.Response<PlannedSetResponse> response) {
                                completed[0]++;
                                if (completed[0] == totalOps && errors[0] == 0) onDone.run();
                            }

                            @Override
                            public void onFailure(retrofit2.Call<PlannedSetResponse> call, Throwable t) {
                                completed[0]++;
                                errors[0]++;
                                if (completed[0] == totalOps) onDone.run();
                            }
                        });
            }
        }
    }

    private List<PlannedSetRequest.DropsetEntry> convertDropsetEntries(List<PlannedSetResponse.DropsetEntry> entries) {
        List<PlannedSetRequest.DropsetEntry> result = new ArrayList<>();
        for (PlannedSetResponse.DropsetEntry e : entries) {
            PlannedSetRequest.DropsetEntry req = new PlannedSetRequest.DropsetEntry();
            req.setWeight(e.getWeight());
            req.setReps(e.getReps());
            result.add(req);
        }
        return result;
    }

    // ==================== Утилиты ====================

    private void showLoading(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            exercisesRecyclerView.setVisibility(View.GONE);
            emptyExercisesText.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            exercisesRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void checkEmptyState() {
        if (exerciseAdapter.getItemCount() == 0) {
            exercisesRecyclerView.setVisibility(View.GONE);
            emptyExercisesText.setVisibility(View.VISIBLE);
        } else {
            exercisesRecyclerView.setVisibility(View.VISIBLE);
            emptyExercisesText.setVisibility(View.GONE);
        }
    }
}
