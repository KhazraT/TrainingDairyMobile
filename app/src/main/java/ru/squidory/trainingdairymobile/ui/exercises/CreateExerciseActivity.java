package ru.squidory.trainingdairymobile.ui.exercises;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.squidory.trainingdairymobile.R;
import ru.squidory.trainingdairymobile.data.model.EquipmentResponse;
import ru.squidory.trainingdairymobile.data.model.ExerciseRequest;
import ru.squidory.trainingdairymobile.data.model.MuscleGroupResponse;
import ru.squidory.trainingdairymobile.data.repository.ExerciseRepository;

/**
 * Activity для создания нового упражнения.
 */
public class CreateExerciseActivity extends AppCompatActivity {

    private TextInputEditText nameInput;
    private TextInputEditText descriptionInput;
    private TextInputEditText techniqueInput;
    private TextInputEditText videoInput;
    private TextInputLayout nameInputLayout;
    private AutoCompleteTextView typeSpinner;

    private ChipGroup targetMusclesChipGroup;
    private ChipGroup secondaryMusclesChipGroup;
    private ChipGroup equipmentChipGroup;

    private MaterialButton cancelButton;
    private MaterialButton saveButton;

    private ExerciseRepository repository;

    // Данные для выбора
    private final Map<String, Long> muscleGroupIds = new HashMap<>();
    private final Map<String, Long> equipmentIds = new HashMap<>();
    private final List<Long> targetMuscleIds = new ArrayList<>();
    private final List<Long> secondaryMuscleIds = new ArrayList<>();
    private final List<Long> selectedEquipmentIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_exercise);

        repository = ExerciseRepository.getInstance();

        initViews();
        setupTypeSpinner();
        loadMuscleGroups();
        loadEquipment();
        setupListeners();
    }

    private void initViews() {
        nameInput = findViewById(R.id.nameInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        techniqueInput = findViewById(R.id.techniqueInput);
        videoInput = findViewById(R.id.videoInput);
        nameInputLayout = findViewById(R.id.nameInputLayout);
        typeSpinner = findViewById(R.id.typeSpinner);

        targetMusclesChipGroup = findViewById(R.id.targetMusclesChipGroup);
        secondaryMusclesChipGroup = findViewById(R.id.secondaryMusclesChipGroup);
        equipmentChipGroup = findViewById(R.id.equipmentChipGroup);

        cancelButton = findViewById(R.id.cancelButton);
        saveButton = findViewById(R.id.saveButton);
    }

    private void setupTypeSpinner() {
        String[] types = {
            getString(R.string.type_reps_weight),
            getString(R.string.type_time_weight),
            getString(R.string.type_time_distance),
            getString(R.string.type_time_weight_distance)
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
            android.R.layout.simple_dropdown_item_1line, types);
        typeSpinner.setAdapter(adapter);
        typeSpinner.setText(types[0], false);
    }

    private int getColorCompat(int colorId) {
        return ContextCompat.getColor(this, colorId);
    }

    private void loadMuscleGroups() {
        repository.getMuscleGroups(new ExerciseRepository.MuscleGroupsCallback() {
            @Override
            public void onSuccess(List<MuscleGroupResponse> muscleGroups) {
                runOnUiThread(() -> {
                    targetMusclesChipGroup.removeAllViews();
                    secondaryMusclesChipGroup.removeAllViews();
                    muscleGroupIds.clear();
                    targetMuscleIds.clear();
                    secondaryMuscleIds.clear();

                    for (MuscleGroupResponse mg : muscleGroups) {
                        final String name = mg.getName();
                        final long id = mg.getId();
                        muscleGroupIds.put(name, id);

                        // Чип для целевых мышц
                        Chip targetChip = new Chip(CreateExerciseActivity.this);
                        targetChip.setText(name);
                        targetChip.setCheckable(true);
                        targetChip.setId(View.generateViewId());
                        targetChip.setTag(id);

                        targetChip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                            if (isChecked) {
                                // Валидация: мышца не может быть и целевой и дополнительной
                                if (secondaryMuscleIds.contains(id)) {
                                    targetChip.setChecked(false);
                                    Toast.makeText(CreateExerciseActivity.this,
                                        name + " уже выбрана как дополнительная",
                                        Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                targetMuscleIds.add(id);
                                targetChip.setChipBackgroundColor(ColorStateList.valueOf(getColorCompat(R.color.primary_chip)));
                            } else {
                                targetMuscleIds.remove(Long.valueOf(id));
                                targetChip.setChipBackgroundColor(ColorStateList.valueOf(getColorCompat(android.R.color.transparent)));
                            }
                        });

                        targetMusclesChipGroup.addView(targetChip);

                        // Чип для дополнительных мышц
                        Chip secondaryChip = new Chip(CreateExerciseActivity.this);
                        secondaryChip.setText(name);
                        secondaryChip.setCheckable(true);
                        secondaryChip.setId(View.generateViewId());
                        secondaryChip.setTag(id);

                        secondaryChip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                            if (isChecked) {
                                // Валидация: мышца не может быть и целевой и дополнительной
                                if (targetMuscleIds.contains(id)) {
                                    secondaryChip.setChecked(false);
                                    Toast.makeText(CreateExerciseActivity.this,
                                        name + " уже выбрана как целевая",
                                        Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                secondaryMuscleIds.add(id);
                                secondaryChip.setChipBackgroundColor(ColorStateList.valueOf(getColorCompat(R.color.secondary_chip)));
                            } else {
                                secondaryMuscleIds.remove(Long.valueOf(id));
                                secondaryChip.setChipBackgroundColor(ColorStateList.valueOf(getColorCompat(android.R.color.transparent)));
                            }
                        });

                        secondaryMusclesChipGroup.addView(secondaryChip);
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(CreateExerciseActivity.this,
                        "Ошибка загрузки мышц: " + error, Toast.LENGTH_LONG).show();
                    setupMusclesFallback();
                });
            }
        });
    }

    private void setupMusclesFallback() {
        String[] muscles = {"Грудь", "Спина", "Ноги", "Плечи", "Бицепсы", "Трицепсы", "Пресс", "Предплечья", "Икры", "Ягодицы"};
        long[] ids = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

        targetMusclesChipGroup.removeAllViews();
        secondaryMusclesChipGroup.removeAllViews();
        targetMuscleIds.clear();
        secondaryMuscleIds.clear();

        for (int i = 0; i < muscles.length; i++) {
            final int index = i;

            Chip targetChip = new Chip(this);
            targetChip.setText(muscles[index]);
            targetChip.setCheckable(true);
            targetChip.setId(View.generateViewId());
            targetChip.setTag(ids[index]);

            targetChip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (secondaryMuscleIds.contains(ids[index])) {
                        targetChip.setChecked(false);
                        Toast.makeText(CreateExerciseActivity.this,
                            muscles[index] + " уже выбрана как дополнительная",
                            Toast.LENGTH_SHORT).show();
                        return;
                    }
                    targetMuscleIds.add(ids[index]);
                    targetChip.setChipBackgroundColor(ColorStateList.valueOf(getColorCompat(R.color.primary_chip)));
                } else {
                    targetMuscleIds.remove(Long.valueOf(ids[index]));
                    targetChip.setChipBackgroundColor(ColorStateList.valueOf(getColorCompat(android.R.color.transparent)));
                }
            });

            targetMusclesChipGroup.addView(targetChip);

            Chip secondaryChip = new Chip(this);
            secondaryChip.setText(muscles[index]);
            secondaryChip.setCheckable(true);
            secondaryChip.setId(View.generateViewId());
            secondaryChip.setTag(ids[index]);

            secondaryChip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (targetMuscleIds.contains(ids[index])) {
                        secondaryChip.setChecked(false);
                        Toast.makeText(CreateExerciseActivity.this,
                            muscles[index] + " уже выбрана как целевая",
                            Toast.LENGTH_SHORT).show();
                        return;
                    }
                    secondaryMuscleIds.add(ids[index]);
                    secondaryChip.setChipBackgroundColor(ColorStateList.valueOf(getColorCompat(R.color.secondary_chip)));
                } else {
                    secondaryMuscleIds.remove(Long.valueOf(ids[index]));
                    secondaryChip.setChipBackgroundColor(ColorStateList.valueOf(getColorCompat(android.R.color.transparent)));
                }
            });

            secondaryMusclesChipGroup.addView(secondaryChip);
        }
    }

    private void loadEquipment() {
        repository.getEquipment(new ExerciseRepository.EquipmentCallback() {
            @Override
            public void onSuccess(List<EquipmentResponse> equipmentList) {
                runOnUiThread(() -> {
                    equipmentChipGroup.removeAllViews();
                    equipmentIds.clear();

                    for (EquipmentResponse eq : equipmentList) {
                        final String name = eq.getName();
                        final long id = eq.getId();

                        Chip chip = new Chip(CreateExerciseActivity.this);
                        chip.setText(name);
                        chip.setCheckable(true);
                        chip.setId(View.generateViewId());

                        equipmentIds.put(name, id);

                        chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                            if (isChecked) {
                                selectedEquipmentIds.add(id);
                                chip.setChipBackgroundColor(ColorStateList.valueOf(getColorCompat(R.color.equipment_chip)));
                            } else {
                                selectedEquipmentIds.remove(Long.valueOf(id));
                                chip.setChipBackgroundColor(ColorStateList.valueOf(getColorCompat(android.R.color.transparent)));
                            }
                        });

                        equipmentChipGroup.addView(chip);
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(CreateExerciseActivity.this,
                        "Ошибка загрузки оборудования: " + error, Toast.LENGTH_LONG).show();
                    setupEquipmentFallback();
                });
            }
        });
    }

    private void setupEquipmentFallback() {
        String[] equipment = {"Штанга", "Гантели", "Тренажёр", "Собственный вес", "Эспандер", "Гиря", "Тросовый тренажёр", "Smith machine"};
        long[] ids = {1, 2, 3, 4, 5, 6, 7, 8};

        equipmentChipGroup.removeAllViews();
        for (int i = 0; i < equipment.length; i++) {
            final int index = i;
            Chip chip = new Chip(this);
            chip.setText(equipment[index]);
            chip.setCheckable(true);
            chip.setId(View.generateViewId());

            equipmentIds.put(equipment[index], ids[index]);

            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedEquipmentIds.add(ids[index]);
                    chip.setChipBackgroundColor(ColorStateList.valueOf(getColorCompat(R.color.equipment_chip)));
                } else {
                    selectedEquipmentIds.remove(Long.valueOf(ids[index]));
                    chip.setChipBackgroundColor(ColorStateList.valueOf(getColorCompat(android.R.color.transparent)));
                }
            });

            equipmentChipGroup.addView(chip);
        }
    }

    private void setupListeners() {
        cancelButton.setOnClickListener(v -> finish());

        saveButton.setOnClickListener(v -> {
            if (validateInputs()) {
                createExercise();
            }
        });
    }

    private boolean validateInputs() {
        String name = getText(nameInput);

        if (TextUtils.isEmpty(name)) {
            nameInputLayout.setError(getString(R.string.error_fill_required));
            return false;
        }

        if (targetMuscleIds.isEmpty() && secondaryMuscleIds.isEmpty()) {
            Toast.makeText(this, "Выберите хотя бы одну группу мышц", Toast.LENGTH_LONG).show();
            return false;
        }

        if (selectedEquipmentIds.isEmpty()) {
            Toast.makeText(this, "Выберите оборудование", Toast.LENGTH_LONG).show();
            return false;
        }

        nameInputLayout.setError(null);
        return true;
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }

    private void createExercise() {
        ExerciseRequest request = new ExerciseRequest();
        request.setName(getText(nameInput));
        request.setDescription(getText(descriptionInput));
        request.setTechnique(getText(techniqueInput));
        request.setVideo(getText(videoInput));

        // Определяем тип упражнения
        String typeText = typeSpinner.getText().toString();
        String exerciseType;
        switch (typeText) {
            case "Время + Вес":
                exerciseType = "time_weight";
                break;
            case "Время + Дистанция":
                exerciseType = "time_distance";
                break;
            case "Время + Вес + Дистанция":
                exerciseType = "time_weight_distance";
                break;
            default:
                exerciseType = "reps_weight";
        }
        request.setExerciseType(exerciseType);

        // Формируем список целевых мышц
        List<ExerciseRequest.MuscleRequest> muscles = new ArrayList<>();
        for (Long id : targetMuscleIds) {
            muscles.add(new ExerciseRequest.MuscleRequest(id, true));
        }
        // Формируем список дополнительных мышц
        for (Long id : secondaryMuscleIds) {
            muscles.add(new ExerciseRequest.MuscleRequest(id, false));
        }
        request.setMuscles(muscles);

        request.setEquipmentIds(selectedEquipmentIds);

        saveButton.setEnabled(false);

        repository.createExercise(request, new ExerciseRepository.ExerciseCallback() {
            @Override
            public void onSuccess(ru.squidory.trainingdairymobile.data.model.ExerciseResponse exercise) {
                Toast.makeText(CreateExerciseActivity.this,
                    getString(R.string.exercise_created), Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onError(String error) {
                saveButton.setEnabled(true);
                Toast.makeText(CreateExerciseActivity.this,
                    "Ошибка: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }
}
