package ru.squidory.trainingdairymobile.ui.trainings;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ru.squidory.trainingdairymobile.R;
import ru.squidory.trainingdairymobile.data.model.EquipmentResponse;
import ru.squidory.trainingdairymobile.data.model.ExerciseResponse;
import ru.squidory.trainingdairymobile.data.model.MuscleGroupResponse;
import ru.squidory.trainingdairymobile.data.repository.ExerciseRepository;

/**
 * Activity для выбора упражнений (как ExercisesFragment но с мультивыбором).
 */
public class ExercisePickerActivity extends AppCompatActivity {

    public static final String EXTRA_EXCLUDE_IDS = "exclude_ids";
    public static final String EXTRA_SELECTED_IDS = "selected_ids";

    private MaterialToolbar toolbar;
    private EditText searchEditText;
    private ImageButton clearSearchButton;
    private MaterialButton allExercisesButton;
    private ChipGroup musclesChipGroup;
    private ChipGroup equipmentChipGroup;
    private RecyclerView exercisesRecyclerView;
    private NestedScrollView contentScrollView;
    private ProgressBar progressBar;
    private TextView emptyText;
    private TextView selectedCountText;
    private MaterialButton cancelButton;
    private MaterialButton addSelectedButton;

    private ExercisePickerAdapter adapter;
    private ExerciseRepository repository;

    private final List<ExerciseResponse> allExercises = new ArrayList<>();
    private final Map<String, Long> muscleGroupIds = new HashMap<>();
    private final Map<String, Long> equipmentIds = new HashMap<>();
    private String selectedMuscle = null;
    private String selectedEquipment = null;
    private String searchQuery = "";
    private List<Long> excludeIds = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_picker);

        Serializable serializable = getIntent().getSerializableExtra(EXTRA_EXCLUDE_IDS);
        if (serializable instanceof ArrayList) {
            //noinspection unchecked
            excludeIds = new ArrayList<>((ArrayList<Long>) serializable);
        }

        repository = ExerciseRepository.getInstance();

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupSearch();
        setupListeners();
        loadMuscleGroupsAndEquipment();
        loadExercises();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        searchEditText = findViewById(R.id.searchEditText);
        clearSearchButton = findViewById(R.id.clearSearchButton);
        allExercisesButton = findViewById(R.id.allExercisesButton);
        musclesChipGroup = findViewById(R.id.musclesChipGroup);
        equipmentChipGroup = findViewById(R.id.equipmentChipGroup);
        exercisesRecyclerView = findViewById(R.id.exercisesRecyclerView);
        contentScrollView = findViewById(R.id.contentScrollView);
        progressBar = findViewById(R.id.progressBar);
        emptyText = findViewById(R.id.emptyText);
        selectedCountText = findViewById(R.id.selectedCountText);
        cancelButton = findViewById(R.id.cancelButton);
        addSelectedButton = findViewById(R.id.addSelectedButton);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Выберите упражнения");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new ExercisePickerAdapter();
        exercisesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        exercisesRecyclerView.setAdapter(adapter);

        adapter.setOnSelectionChangeListener(count -> {
            selectedCountText.setText("Выбрано: " + count);
            addSelectedButton.setEnabled(count > 0);
        });
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().trim();
                clearSearchButton.setVisibility(searchQuery.isEmpty() ? View.GONE : View.VISIBLE);
                applyFilters();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        clearSearchButton.setOnClickListener(v -> {
            searchEditText.setText("");
            searchQuery = "";
            clearSearchButton.setVisibility(View.GONE);
            applyFilters();
        });
    }

    private void setupListeners() {
        allExercisesButton.setOnClickListener(v -> {
            selectedMuscle = null;
            selectedEquipment = null;
            musclesChipGroup.clearCheck();
            equipmentChipGroup.clearCheck();
            applyFilters();
        });

        cancelButton.setOnClickListener(v -> finish());

        addSelectedButton.setOnClickListener(v -> {
            List<Long> selectedIds = adapter.getSelectedExerciseIds();
            if (selectedIds.isEmpty()) return;
            Intent result = new Intent();
            ArrayList<Long> list = new ArrayList<>(selectedIds);
            result.putExtra(EXTRA_SELECTED_IDS, list);
            setResult(RESULT_OK, result);
            finish();
        });
    }

    private void loadMuscleGroupsAndEquipment() {
        repository.getMuscleGroups(new ExerciseRepository.MuscleGroupsCallback() {
            @Override
            public void onSuccess(List<MuscleGroupResponse> muscleGroups) {
                runOnUiThread(() -> {
                    musclesChipGroup.removeAllViews();
                    muscleGroupIds.clear();
                    for (MuscleGroupResponse mg : muscleGroups) {
                        Chip chip = new Chip(ExercisePickerActivity.this);
                        chip.setText(mg.getName());
                        chip.setCheckable(true);
                        chip.setId(View.generateViewId());
                        muscleGroupIds.put(mg.getName(), mg.getId());
                        chip.setOnCheckedChangeListener((b, isChecked) -> {
                            if (isChecked) { selectedMuscle = mg.getName(); applyFilters(); }
                        });
                        musclesChipGroup.addView(chip);
                    }
                });
            }
            @Override public void onError(String error) { setupMusclesFallback(); }
        });

        repository.getEquipment(new ExerciseRepository.EquipmentCallback() {
            @Override
            public void onSuccess(List<EquipmentResponse> list) {
                runOnUiThread(() -> {
                    equipmentChipGroup.removeAllViews();
                    equipmentIds.clear();
                    for (EquipmentResponse eq : list) {
                        Chip chip = new Chip(ExercisePickerActivity.this);
                        chip.setText(eq.getName());
                        chip.setCheckable(true);
                        chip.setId(View.generateViewId());
                        equipmentIds.put(eq.getName(), eq.getId());
                        chip.setOnCheckedChangeListener((b, isChecked) -> {
                            if (isChecked) { selectedEquipment = eq.getName(); applyFilters(); }
                        });
                        equipmentChipGroup.addView(chip);
                    }
                });
            }
            @Override public void onError(String error) { setupEquipmentFallback(); }
        });
    }

    private void setupMusclesFallback() {
        String[] muscles = {"Грудь","Спина","Ноги","Плечи","Бицепсы","Трицепсы","Пресс","Предплечья","Икры","Ягодицы"};
        long[] ids = {1,2,3,4,5,6,7,8,9,10};
        for (int i = 0; i < muscles.length; i++) {
            final int idx = i;
            Chip chip = new Chip(this);
            chip.setText(muscles[idx]);
            chip.setCheckable(true);
            chip.setId(View.generateViewId());
            muscleGroupIds.put(muscles[idx], ids[idx]);
            chip.setOnCheckedChangeListener((b, checked) -> { if (checked) { selectedMuscle = muscles[idx]; applyFilters(); }});
            musclesChipGroup.addView(chip);
        }
    }

    private void setupEquipmentFallback() {
        String[] eq = {"Штанга","Гантели","Тренажёр","Собственный вес","Эспандер","Гиря","Тросовый тренажёр","Машина Смита"};
        long[] ids = {1,2,3,4,5,6,7,8};
        for (int i = 0; i < eq.length; i++) {
            final int idx = i;
            Chip chip = new Chip(this);
            chip.setText(eq[idx]);
            chip.setCheckable(true);
            chip.setId(View.generateViewId());
            equipmentIds.put(eq[idx], ids[idx]);
            chip.setOnCheckedChangeListener((b, checked) -> { if (checked) { selectedEquipment = eq[idx]; applyFilters(); }});
            equipmentChipGroup.addView(chip);
        }
    }

    private void loadExercises() {
        showLoading(true);
        repository.getExercises(null, null, null, new ExerciseRepository.ExercisesCallback() {
            @Override
            public void onSuccess(List<ExerciseResponse> exercises) {
                allExercises.clear();
                allExercises.addAll(exercises);
                showLoading(false);
                applyFilters();
            }
            @Override
            public void onError(String error) {
                showLoading(false);
                emptyText.setText(error != null ? error : getString(R.string.error_unknown));
                emptyText.setVisibility(View.VISIBLE);
            }
        });
    }

    private void applyFilters() {
        List<ExerciseResponse> filtered = allExercises.stream().filter(ex -> {
            if (selectedMuscle != null && !selectedMuscle.isEmpty()) {
                boolean match = ex.getMuscleGroups() != null && ex.getMuscleGroups().stream()
                    .anyMatch(m -> m.getName().equals(selectedMuscle) && m.getIsPrimary() != null && m.getIsPrimary());
                if (!match) return false;
            }
            if (selectedEquipment != null && !selectedEquipment.isEmpty()) {
                boolean match = ex.getEquipment() != null && ex.getEquipment().stream()
                    .anyMatch(e -> e.getName().equals(selectedEquipment));
                if (!match) return false;
            }
            if (!searchQuery.isEmpty()) {
                String q = searchQuery.toLowerCase();
                boolean nameMatch = ex.getName() != null && ex.getName().toLowerCase().contains(q);
                boolean descMatch = ex.getDescription() != null && ex.getDescription().toLowerCase().contains(q);
                if (!nameMatch && !descMatch) return false;
            }
            return true;
        }).collect(Collectors.toList());

        adapter.setExercises(filtered, excludeIds);
        checkEmptyState();
    }

    private void showLoading(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            exercisesRecyclerView.setVisibility(View.GONE);
            emptyText.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            exercisesRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void checkEmptyState() {
        if (adapter.getItemCount() == 0) {
            exercisesRecyclerView.setVisibility(View.GONE);
            emptyText.setVisibility(View.VISIBLE);
        } else {
            exercisesRecyclerView.setVisibility(View.VISIBLE);
            emptyText.setVisibility(View.GONE);
        }
    }
}
