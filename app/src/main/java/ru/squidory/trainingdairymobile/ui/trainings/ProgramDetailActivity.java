package ru.squidory.trainingdairymobile.ui.trainings;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.squidory.trainingdairymobile.R;
import ru.squidory.trainingdairymobile.data.model.ProgramRequest;
import ru.squidory.trainingdairymobile.data.model.WorkoutRequest;
import ru.squidory.trainingdairymobile.data.model.WorkoutResponse;
import ru.squidory.trainingdairymobile.data.repository.ProgramRepository;

/**
 * Activity для просмотра деталей программы и списка тренировок.
 */
public class ProgramDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PROGRAM_ID = "program_id";
    public static final String EXTRA_PROGRAM_NAME = "program_name";
    public static final String EXTRA_PROGRAM_DESCRIPTION = "program_description";

    private MaterialToolbar toolbar;
    private TextView programDescriptionText;
    private RecyclerView workoutsRecyclerView;
    private ProgressBar progressBar;
    private TextView emptyText;
    private ImageButton addWorkoutButton;
    private ImageButton editProgramButton;

    private WorkoutDetailAdapter adapter;
    private ProgramRepository repository;
    private long programId;
    private String programName;
    private String programDescription;
    private boolean editMode = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_program_detail);

        programId = getIntent().getLongExtra(EXTRA_PROGRAM_ID, -1);
        programName = getIntent().getStringExtra(EXTRA_PROGRAM_NAME);
        programDescription = getIntent().getStringExtra(EXTRA_PROGRAM_DESCRIPTION);

        if (programId == -1) {
            Toast.makeText(this, "Ошибка: программа не найдена", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        repository = ProgramRepository.getInstance();

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupDragAndDrop();
        setupListeners();

        // Отображаем описание (или текст-заглушку)
        updateProgramEditHint();

        loadWorkouts();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        programDescriptionText = findViewById(R.id.programDescriptionText);
        workoutsRecyclerView = findViewById(R.id.workoutsRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyText = findViewById(R.id.emptyText);
        addWorkoutButton = findViewById(R.id.addWorkoutButton);
        editProgramButton = findViewById(R.id.editProgramButton);

        // Скрываем кнопку добавления тренировки по умолчанию
        addWorkoutButton.setVisibility(View.GONE);

        // Нажатие на описание открывает диалог редактирования ТОЛЬКО в режиме редактирования
        programDescriptionText.setOnClickListener(v -> {
            if (editMode) {
                showEditProgramDialog();
            }
        });
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(programName != null ? programName : "Программа");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Кнопка редактирования программы
        if (editProgramButton != null) {
            editProgramButton.setOnClickListener(v -> toggleEditMode());
        }
    }

    private void toggleEditMode() {
        editMode = !editMode;

        // Обновить иконку карандаша
        if (editMode) {
            editProgramButton.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
            editProgramButton.setContentDescription("Завершить редактирование");
        } else {
            editProgramButton.setImageResource(android.R.drawable.ic_menu_edit);
            editProgramButton.setContentDescription("Редактировать программу");
        }

        // Обновить видимость кнопки добавления
        addWorkoutButton.setVisibility(editMode ? View.VISIBLE : View.GONE);

        // Обновить адаптер
        adapter.setEditMode(editMode);

        // Обновить видимость подсказки редактирования
        updateProgramEditHint();

        Toast.makeText(this, editMode ? "Режим редактирования" : "Режим просмотра", Toast.LENGTH_SHORT).show();
    }

    private void updateProgramEditHint() {
        if (programDescriptionText == null) {
            return;
        }

        boolean hasDescription = programDescription != null && !programDescription.isEmpty();

        if (hasDescription) {
            programDescriptionText.setText(programDescription);
            programDescriptionText.setTextColor(getResources().getColor(android.R.color.secondary_text_light, null));
            programDescriptionText.setTypeface(null, android.graphics.Typeface.NORMAL);
            programDescriptionText.setVisibility(View.VISIBLE);
        } else {
            programDescriptionText.setText(R.string.no_description);
            programDescriptionText.setTextColor(getResources().getColor(android.R.color.darker_gray, null));
            programDescriptionText.setTypeface(null, android.graphics.Typeface.ITALIC);
            programDescriptionText.setVisibility(View.VISIBLE);
        }
    }

    private void setupRecyclerView() {
        adapter = new WorkoutDetailAdapter();
        workoutsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        workoutsRecyclerView.setAdapter(adapter);

        adapter.setOnWorkoutActionListener(new WorkoutDetailAdapter.OnWorkoutActionListener() {
            @Override
            public void onDeleteWorkout(WorkoutResponse workout, int position) {
                showDeleteWorkoutConfirmation(workout);
            }

            @Override
            public void onManageExercises(WorkoutResponse workout) {
                Toast.makeText(ProgramDetailActivity.this,
                    "Управление упражнениями: " + workout.getName(),
                    Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onWorkoutMoved(WorkoutResponse workout, int fromPosition, int toPosition) {
                // После перемещения сохраняем новый порядок для всех тренировок
                saveAllWorkoutsOrder();
            }
        });
    }

    private void setupDragAndDrop() {
        ItemTouchHelper.Callback callback = new ItemTouchHelper.Callback() {
            @Override
            public boolean isLongPressDragEnabled() {
                // Включаем drag только в режиме редактирования
                return editMode;
            }

            @Override
            public boolean isItemViewSwipeEnabled() {
                return false;
            }

            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                return makeMovementFlags(dragFlags, 0);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();

                adapter.moveItem(fromPosition, toPosition);

                // Уведомить слушателя о перемещении
                WorkoutResponse workout = adapter.getWorkouts().get(toPosition);
                if (adapter.getOnWorkoutActionListener() != null) {
                    adapter.getOnWorkoutActionListener().onWorkoutMoved(workout, fromPosition, toPosition);
                }

                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Не используем swipe
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(workoutsRecyclerView);
    }

    private void saveWorkoutOrder(long workoutId, int newOrder) {
        // Находим тренировку в текущем списке чтобы получить все её данные
        WorkoutResponse workout = null;
        for (WorkoutResponse w : adapter.getWorkouts()) {
            if (w.getId() == workoutId) {
                workout = w;
                break;
            }
        }

        if (workout == null) return;

        // Отправляем все поля, не только порядок
        WorkoutRequest request = new WorkoutRequest();
        request.setName(workout.getName());
        request.setComment(workout.getComment());
        request.setWorkoutOrder(newOrder);

        repository.updateWorkout(workoutId, request, new ProgramRepository.WorkoutCallback() {
            @Override
            public void onSuccess(WorkoutResponse workout) {
                // Порядок обновлен успешно
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ProgramDetailActivity.this,
                    "Ошибка сохранения порядка: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveAllWorkoutsOrder() {
        List<WorkoutResponse> workouts = adapter.getWorkouts();
        if (workouts == null || workouts.isEmpty()) return;

        final int[] savedCount = {0};
        final int totalCount = workouts.size();

        for (int i = 0; i < workouts.size(); i++) {
            WorkoutResponse workout = workouts.get(i);
            final int index = i;

            WorkoutRequest request = new WorkoutRequest();
            request.setName(workout.getName());
            request.setComment(workout.getComment());
            request.setWorkoutOrder(i + 1); // Порядок начинается с 1

            repository.updateWorkout(workout.getId(), request, new ProgramRepository.WorkoutCallback() {
                @Override
                public void onSuccess(WorkoutResponse w) {
                    savedCount[0]++;
                }

                @Override
                public void onError(String error) {
                    savedCount[0]++;
                    Toast.makeText(ProgramDetailActivity.this,
                        "Ошибка сохранения порядка #" + (index + 1) + ": " + error,
                        Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setupListeners() {
        addWorkoutButton.setOnClickListener(v -> showCreateWorkoutDialog());
    }

    private void loadWorkouts() {
        showLoading(true);
        repository.getWorkoutsByProgram(programId, new ProgramRepository.WorkoutsCallback() {
            @Override
            public void onSuccess(List<WorkoutResponse> workoutList) {
                showLoading(false);
                // Сортируем по порядку
                workoutList.sort((w1, w2) -> {
                    Integer order1 = w1.getWorkoutOrder();
                    Integer order2 = w2.getWorkoutOrder();
                    if (order1 == null) order1 = 0;
                    if (order2 == null) order2 = 0;
                    return order1.compareTo(order2);
                });
                adapter.setWorkouts(workoutList);
                checkEmptyState();
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                Toast.makeText(ProgramDetailActivity.this,
                    "Ошибка загрузки: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showCreateWorkoutDialog() {
        if (!editMode) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_workout, null);
        builder.setView(dialogView);

        TextInputLayout nameLayout = dialogView.findViewById(R.id.workoutNameLayout);
        TextInputEditText nameInput = dialogView.findViewById(R.id.workoutNameInput);
        TextInputEditText commentInput = dialogView.findViewById(R.id.workoutCommentInput);
        MaterialButton cancelButton = dialogView.findViewById(R.id.dialogCancelButton);
        MaterialButton saveButton = dialogView.findViewById(R.id.dialogSaveButton);

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        saveButton.setOnClickListener(v -> {
            String name = nameInput.getText() != null ? nameInput.getText().toString().trim() : "";

            if (name.isEmpty()) {
                nameLayout.setError("Введите название тренировки");
                return;
            }

            nameLayout.setError(null);

            String comment = commentInput.getText() != null ? commentInput.getText().toString().trim() : "";

            WorkoutRequest request = new WorkoutRequest();
            request.setName(name);
            request.setComment(comment);
            // Порядок - последний в списке + 1
            List<WorkoutResponse> currentWorkouts = adapter.getWorkouts();
            request.setWorkoutOrder(currentWorkouts != null ? currentWorkouts.size() + 1 : 1);

            repository.createWorkout(programId, request, new ProgramRepository.WorkoutCallback() {
                @Override
                public void onSuccess(WorkoutResponse workout) {
                    dialog.dismiss();
                    Toast.makeText(ProgramDetailActivity.this,
                        "Тренировка создана", Toast.LENGTH_SHORT).show();
                    loadWorkouts(); // Обновляем список
                    setResult(AppCompatActivity.RESULT_OK);
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(ProgramDetailActivity.this,
                        "Ошибка: " + error, Toast.LENGTH_LONG).show();
                }
            });
        });

        dialog.show();
    }

    private void showDeleteWorkoutConfirmation(WorkoutResponse workout) {
        if (!editMode) return;

        new AlertDialog.Builder(this)
            .setTitle("Удалить тренировку?")
            .setMessage("Вы уверены, что хотите удалить \"" + workout.getName() + "\"?")
            .setPositiveButton("Удалить", (dialog, which) -> {
                repository.deleteWorkout(workout.getId(), new ProgramRepository.SimpleCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(ProgramDetailActivity.this,
                            "Тренировка удалена", Toast.LENGTH_SHORT).show();
                        loadWorkouts();
                        setResult(AppCompatActivity.RESULT_OK);
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(ProgramDetailActivity.this,
                            "Ошибка: " + error, Toast.LENGTH_LONG).show();
                    }
                });
            })
            .setNegativeButton("Отмена", null)
            .show();
    }

    private void showEditWorkoutDialog(WorkoutResponse workout) {
        if (!editMode) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_workout, null);
        builder.setView(dialogView);

        TextInputLayout nameLayout = dialogView.findViewById(R.id.workoutNameLayout);
        TextInputEditText nameInput = dialogView.findViewById(R.id.workoutNameInput);
        TextInputEditText commentInput = dialogView.findViewById(R.id.workoutCommentInput);
        MaterialButton cancelButton = dialogView.findViewById(R.id.dialogCancelButton);
        MaterialButton saveButton = dialogView.findViewById(R.id.dialogSaveButton);

        // Заполняем текущими данными
        nameInput.setText(workout.getName());
        commentInput.setText(workout.getComment());

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        saveButton.setOnClickListener(v -> {
            String name = nameInput.getText() != null ? nameInput.getText().toString().trim() : "";

            if (name.isEmpty()) {
                nameLayout.setError("Введите название тренировки");
                return;
            }

            nameLayout.setError(null);

            String comment = commentInput.getText() != null ? commentInput.getText().toString().trim() : "";

            WorkoutRequest request = new WorkoutRequest();
            request.setName(name);
            request.setComment(comment);
            request.setWorkoutOrder(workout.getWorkoutOrder());

            repository.updateWorkout(workout.getId(), request, new ProgramRepository.WorkoutCallback() {
                @Override
                public void onSuccess(WorkoutResponse updatedWorkout) {
                    dialog.dismiss();
                    Toast.makeText(ProgramDetailActivity.this,
                        "Тренировка обновлена", Toast.LENGTH_SHORT).show();
                    loadWorkouts();
                    setResult(AppCompatActivity.RESULT_OK);
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(ProgramDetailActivity.this,
                        "Ошибка: " + error, Toast.LENGTH_LONG).show();
                }
            });
        });

        dialog.show();
    }

    private void showEditProgramDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_program, null);
        builder.setView(dialogView);

        TextInputLayout nameLayout = dialogView.findViewById(R.id.programNameLayout);
        TextInputLayout descriptionLayout = dialogView.findViewById(R.id.programDescriptionLayout);
        TextInputEditText nameInput = dialogView.findViewById(R.id.programNameInput);
        TextInputEditText descriptionInput = dialogView.findViewById(R.id.programDescriptionInput);
        MaterialButton cancelButton = dialogView.findViewById(R.id.dialogCancelButton);
        MaterialButton saveButton = dialogView.findViewById(R.id.dialogSaveButton);

        // Заполняем текущими данными (не показывая заглушку "Описание отсутствует")
        boolean hasName = programName != null && !programName.isEmpty();
        boolean hasDescription = programDescription != null && !programDescription.isEmpty();

        nameInput.setText(hasName ? programName : "");
        descriptionInput.setText(hasDescription ? programDescription : "");

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        saveButton.setOnClickListener(v -> {
            String name = nameInput.getText() != null ? nameInput.getText().toString().trim() : "";
            if (name.isEmpty()) {
                nameLayout.setError("Введите название программы");
                return;
            }
            nameLayout.setError(null);

            String description = descriptionInput.getText() != null ? descriptionInput.getText().toString().trim() : "";

            ProgramRequest request = new ProgramRequest();
            request.setName(name);
            request.setDescription(description);
            request.setIsPublic(false);

            repository.updateProgram(programId, request, new ProgramRepository.ProgramCallback() {
                @Override
                public void onSuccess(ru.squidory.trainingdairymobile.data.model.ProgramResponse program) {
                    dialog.dismiss();
                    programName = name;
                    programDescription = description;
                    setupToolbar(); // Обновляем заголовок

                    // Обновляем отображение
                    updateProgramEditHint();

                    Toast.makeText(ProgramDetailActivity.this,
                        "Программа обновлена", Toast.LENGTH_SHORT).show();

                    setResult(AppCompatActivity.RESULT_OK);
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(ProgramDetailActivity.this,
                        "Ошибка: " + error, Toast.LENGTH_LONG).show();
                }
            });
        });

        dialog.show();
    }

    private void showLoading(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            workoutsRecyclerView.setVisibility(View.GONE);
            emptyText.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            workoutsRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void checkEmptyState() {
        if (adapter.getItemCount() == 0) {
            workoutsRecyclerView.setVisibility(View.GONE);
            emptyText.setVisibility(editMode ? View.VISIBLE : View.GONE);
            emptyText.setText(editMode ? "Нажмите + чтобы добавить тренировку" : "Нет тренировок в программе");
        } else {
            workoutsRecyclerView.setVisibility(View.VISIBLE);
            emptyText.setVisibility(View.GONE);
        }
    }
}
