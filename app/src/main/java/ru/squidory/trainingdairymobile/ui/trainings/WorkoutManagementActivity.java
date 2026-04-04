package ru.squidory.trainingdairymobile.ui.trainings;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

import ru.squidory.trainingdairymobile.R;
import ru.squidory.trainingdairymobile.data.model.WorkoutRequest;
import ru.squidory.trainingdairymobile.data.model.WorkoutResponse;
import ru.squidory.trainingdairymobile.data.repository.ProgramRepository;

/**
 * Activity для управления тренировками внутри программы.
 */
public class WorkoutManagementActivity extends AppCompatActivity {

    public static final String EXTRA_PROGRAM_ID = "program_id";
    public static final String EXTRA_PROGRAM_NAME = "program_name";

    private MaterialToolbar toolbar;
    private RecyclerView workoutsRecyclerView;
    private ProgressBar progressBar;
    private TextView emptyText;
    private FloatingActionButton addWorkoutButton;

    private WorkoutManagementAdapter adapter;
    private ProgramRepository repository;
    private long programId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_management);

        programId = getIntent().getLongExtra(EXTRA_PROGRAM_ID, -1);
        String programName = getIntent().getStringExtra(EXTRA_PROGRAM_NAME);

        if (programId == -1) {
            Toast.makeText(this, "Ошибка: программа не найдена", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        repository = ProgramRepository.getInstance();

        initViews();
        setupToolbar(programName);
        setupRecyclerView();
        setupListeners();
        loadWorkouts();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        workoutsRecyclerView = findViewById(R.id.workoutsRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyText = findViewById(R.id.emptyText);
        addWorkoutButton = findViewById(R.id.addWorkoutButton);
    }

    private void setupToolbar(String programName) {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Тренировки: " + (programName != null ? programName : ""));
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new WorkoutManagementAdapter();
        workoutsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        workoutsRecyclerView.setAdapter(adapter);

        adapter.setOnWorkoutActionListener(new WorkoutManagementAdapter.OnWorkoutActionListener() {
            @Override
            public void onManageExercises(WorkoutResponse workout) {
                // TODO: Открыть управление упражнениями тренировки
                Toast.makeText(WorkoutManagementActivity.this,
                    "Управление упражнениями: " + workout.getName(),
                    Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDeleteWorkout(WorkoutResponse workout) {
                showDeleteConfirmation(workout);
            }
        });
    }

    private void setupListeners() {
        addWorkoutButton.setOnClickListener(v -> showCreateWorkoutDialog());
    }

    private void loadWorkouts() {
        showLoading(true);
        repository.getWorkoutsByProgram(programId, new ProgramRepository.WorkoutsCallback() {
            @Override
            public void onSuccess(List<WorkoutResponse> workouts) {
                showLoading(false);
                adapter.setWorkouts(workouts);
                checkEmptyState();
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                Toast.makeText(WorkoutManagementActivity.this,
                    "Ошибка загрузки: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showCreateWorkoutDialog() {
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

            repository.createWorkout(programId, request, new ProgramRepository.WorkoutCallback() {
                @Override
                public void onSuccess(WorkoutResponse workout) {
                    dialog.dismiss();
                    Toast.makeText(WorkoutManagementActivity.this,
                        "Тренировка создана", Toast.LENGTH_SHORT).show();
                    loadWorkouts();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(WorkoutManagementActivity.this,
                        "Ошибка: " + error, Toast.LENGTH_LONG).show();
                }
            });
        });

        dialog.show();
    }

    private void showDeleteConfirmation(WorkoutResponse workout) {
        new AlertDialog.Builder(this)
            .setTitle("Удалить тренировку?")
            .setMessage("Вы уверены, что хотите удалить \"" + workout.getName() + "\"?")
            .setPositiveButton("Удалить", (dialog, which) -> {
                repository.deleteWorkout(workout.getId(), new ProgramRepository.SimpleCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(WorkoutManagementActivity.this,
                            "Тренировка удалена", Toast.LENGTH_SHORT).show();
                        loadWorkouts();
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(WorkoutManagementActivity.this,
                            "Ошибка: " + error, Toast.LENGTH_LONG).show();
                    }
                });
            })
            .setNegativeButton("Отмена", null)
            .show();
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
            emptyText.setVisibility(View.VISIBLE);
        } else {
            workoutsRecyclerView.setVisibility(View.VISIBLE);
            emptyText.setVisibility(View.GONE);
        }
    }
}
