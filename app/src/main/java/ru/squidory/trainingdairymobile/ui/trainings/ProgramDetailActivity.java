package ru.squidory.trainingdairymobile.ui.trainings;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

import ru.squidory.trainingdairymobile.R;
import ru.squidory.trainingdairymobile.data.model.ProgramResponse;
import ru.squidory.trainingdairymobile.data.model.WorkoutResponse;
import ru.squidory.trainingdairymobile.data.repository.ProgramRepository;

/**
 * Activity для просмотра деталей программы и списка тренировок.
 */
public class ProgramDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PROGRAM_ID = "program_id";
    public static final String EXTRA_PROGRAM_NAME = "program_name";

    private MaterialToolbar toolbar;
    private TextView programNameText;
    private TextView programDescriptionText;
    private RecyclerView workoutsRecyclerView;
    private ImageButton addWorkoutButton;

    private WorkoutAdapter adapter;
    private ProgramRepository repository;
    private long programId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_program_detail);

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
        programNameText = findViewById(R.id.programNameText);
        programDescriptionText = findViewById(R.id.programDescriptionText);
        workoutsRecyclerView = findViewById(R.id.workoutsRecyclerView);
        addWorkoutButton = findViewById(R.id.addWorkoutButton);
    }

    private void setupToolbar(String programName) {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(programName != null ? programName : "Программа");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new WorkoutAdapter();
        workoutsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        workoutsRecyclerView.setAdapter(adapter);

        adapter.setOnWorkoutClickListener(new WorkoutAdapter.OnWorkoutClickListener() {
            @Override
            public void onWorkoutClick(WorkoutResponse workout) {
                // Открыть детали тренировки
                Toast.makeText(ProgramDetailActivity.this, 
                    "Тренировка: " + workout.getName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStartWorkout(WorkoutResponse workout) {
                // Начать тренировку - переход на экран сессии
                Toast.makeText(ProgramDetailActivity.this, 
                    "Начало тренировки: " + workout.getName(), Toast.LENGTH_SHORT).show();
                // TODO: Запустить SessionActivity
            }

            @Override
            public void onWorkoutLongClick(WorkoutResponse workout) {
                // Показать меню действий
                Toast.makeText(ProgramDetailActivity.this, 
                    "Долгий клик: " + workout.getName(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        addWorkoutButton.setOnClickListener(v -> {
            // Создать новую тренировку
            Toast.makeText(this, "Создание тренировки (в разработке)", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadWorkouts() {
        repository.getWorkoutsByProgram(programId, new ProgramRepository.WorkoutsCallback() {
            @Override
            public void onSuccess(List<WorkoutResponse> workouts) {
                adapter.setWorkouts(workouts);
                
                // Обновить заголовок с количеством тренировок
                if (programNameText != null) {
                    programNameText.setText(String.format("Тренировок: %d", workouts.size()));
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ProgramDetailActivity.this, 
                    "Ошибка загрузки: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }
}
