package ru.squidory.trainingdairymobile.ui.exercises;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

import ru.squidory.trainingdairymobile.R;
import ru.squidory.trainingdairymobile.data.model.ExerciseResponse;
import ru.squidory.trainingdairymobile.data.model.MuscleGroupResponse;
import ru.squidory.trainingdairymobile.data.repository.ExerciseRepository;

/**
 * Activity для просмотра деталей упражнения.
 */
public class ExerciseDetailActivity extends AppCompatActivity {

    public static final String EXTRA_EXERCISE_ID = "exercise_id";
    public static final String EXTRA_EXERCISE_NAME = "exercise_name";

    private MaterialToolbar toolbar;
    private AppBarLayout appBarLayout;
    private TextView exerciseNameText;
    private TextView exerciseTypeText;
    private View descriptionLayout;
    private TextView descriptionText;
    private View techniqueLayout;
    private TextView techniqueText;
    private FrameLayout videoContainer;
    private View musclesLayout;
    private ChipGroup musclesChipGroup;
    private View targetMusclesLayout;
    private ChipGroup targetMusclesChipGroup;
    private View secondaryMusclesLayout;
    private ChipGroup secondaryMusclesChipGroup;
    private View equipmentLayout;
    private ChipGroup equipmentChipGroup;

    private ExerciseRepository repository;
    private long exerciseId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_detail);

        exerciseId = getIntent().getLongExtra(EXTRA_EXERCISE_ID, -1);
        String exerciseName = getIntent().getStringExtra(EXTRA_EXERCISE_NAME);

        if (exerciseId == -1) {
            Toast.makeText(this, "Ошибка: упражнение не найдено", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        repository = ExerciseRepository.getInstance();

        initViews();
        setupToolbar(exerciseName);
        loadExerciseDetails();
    }

    private void initViews() {
        appBarLayout = findViewById(R.id.appBarLayout);
        toolbar = findViewById(R.id.toolbar);
        exerciseNameText = findViewById(R.id.exerciseNameText);
        exerciseTypeText = findViewById(R.id.exerciseTypeText);
        descriptionLayout = findViewById(R.id.descriptionLayout);
        descriptionText = findViewById(R.id.descriptionText);
        techniqueLayout = findViewById(R.id.techniqueLayout);
        techniqueText = findViewById(R.id.techniqueText);
        videoContainer = findViewById(R.id.videoContainer);
        targetMusclesLayout = findViewById(R.id.targetMusclesLayout);
        targetMusclesChipGroup = findViewById(R.id.targetMusclesChipGroup);
        secondaryMusclesLayout = findViewById(R.id.secondaryMusclesLayout);
        secondaryMusclesChipGroup = findViewById(R.id.secondaryMusclesChipGroup);
        equipmentLayout = findViewById(R.id.equipmentLayout);
        equipmentChipGroup = findViewById(R.id.equipmentChipGroup);
    }

    private void setupToolbar(String exerciseName) {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(exerciseName != null ? exerciseName : "Упражнение");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadExerciseDetails() {
        repository.getExerciseById(exerciseId, new ExerciseRepository.ExerciseCallback() {
            @Override
            public void onSuccess(ExerciseResponse exercise) {
                displayExercise(exercise);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ExerciseDetailActivity.this,
                    "Ошибка загрузки: " + error, Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void displayExercise(ExerciseResponse exercise) {
        // Название
        exerciseNameText.setText(exercise.getName());

        // Тип упражнения (скрыт, не показываем)
        exerciseTypeText.setVisibility(View.GONE);

        // Описание
        if (exercise.getDescription() != null && !exercise.getDescription().isEmpty()) {
            descriptionText.setText(exercise.getDescription());
            descriptionLayout.setVisibility(View.VISIBLE);
        } else {
            descriptionLayout.setVisibility(View.GONE);
        }

        // Техника
        if (exercise.getTechnique() != null && !exercise.getTechnique().isEmpty()) {
            techniqueText.setText(exercise.getTechnique());
            techniqueLayout.setVisibility(View.VISIBLE);
        } else {
            techniqueLayout.setVisibility(View.GONE);
        }

        // Видео
        if (exercise.getVideo() != null && !exercise.getVideo().isEmpty()) {
            videoContainer.setVisibility(View.VISIBLE);
            videoContainer.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(exercise.getVideo()));
                startActivity(intent);
            });
        } else {
            videoContainer.setVisibility(View.GONE);
        }

        // Целевые мышцы
        targetMusclesChipGroup.removeAllViews();
        List<MuscleGroupResponse> targetMuscles = new ArrayList<>();
        List<MuscleGroupResponse> secondaryMuscles = new ArrayList<>();

        if (exercise.getMuscleGroups() != null) {
            for (var muscle : exercise.getMuscleGroups()) {
                if (muscle.getIsPrimary() != null && muscle.getIsPrimary()) {
                    targetMuscles.add(muscle);
                } else {
                    secondaryMuscles.add(muscle);
                }
            }
        }

        if (!targetMuscles.isEmpty()) {
            for (var muscle : targetMuscles) {
                Chip chip = new Chip(this);
                chip.setText(muscle.getName());
                chip.setClickable(false);
                chip.setCheckable(false);
                targetMusclesChipGroup.addView(chip);
            }
            targetMusclesLayout.setVisibility(View.VISIBLE);
        } else {
            targetMusclesLayout.setVisibility(View.GONE);
        }

        // Нецелевые мышцы
        secondaryMusclesChipGroup.removeAllViews();
        if (!secondaryMuscles.isEmpty()) {
            for (var muscle : secondaryMuscles) {
                Chip chip = new Chip(this);
                chip.setText(muscle.getName());
                chip.setClickable(false);
                chip.setCheckable(false);
                secondaryMusclesChipGroup.addView(chip);
            }
            secondaryMusclesLayout.setVisibility(View.VISIBLE);
        } else {
            secondaryMusclesLayout.setVisibility(View.GONE);
        }

        // Оборудование
        equipmentChipGroup.removeAllViews();
        if (exercise.getEquipment() != null && !exercise.getEquipment().isEmpty()) {
            for (var equipment : exercise.getEquipment()) {
                Chip chip = new Chip(this);
                chip.setText(equipment.getName());
                chip.setClickable(false);
                chip.setCheckable(false);
                equipmentChipGroup.addView(chip);
            }
            equipmentLayout.setVisibility(View.VISIBLE);
        } else {
            equipmentLayout.setVisibility(View.GONE);
        }
    }
}
