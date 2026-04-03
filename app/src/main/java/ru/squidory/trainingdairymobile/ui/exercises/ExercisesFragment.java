package ru.squidory.trainingdairymobile.ui.exercises;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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
import ru.squidory.trainingdairymobile.ui.main.BaseFragment;

/**
 * Фрагмент раздела "Упражнения".
 * Отображает список упражнений с фильтрацией по мышцам и поиском.
 */
public class ExercisesFragment extends BaseFragment {

    private Toolbar toolbar;
    private EditText searchEditText;
    private ImageButton clearSearchButton;
    private MaterialButton allExercisesButton;
    private ChipGroup musclesChipGroup;
    private ChipGroup equipmentChipGroup;
    private RecyclerView exercisesRecyclerView;
    private ProgressBar progressBar;
    private TextView emptyText;
    private FloatingActionButton addExerciseButton;

    private ExerciseAdapter adapter;
    private ExerciseRepository repository;

    private final List<ExerciseResponse> allExercises = new ArrayList<>();
    private final Map<String, Long> muscleGroupIds = new HashMap<>();
    private final Map<String, Long> equipmentIds = new HashMap<>();
    private String selectedMuscle = null;
    private String selectedEquipment = null;
    private String searchQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_exercises, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repository = ExerciseRepository.getInstance();

        initViews(view);
        setupToolbar();
        setupRecyclerView();
        setupSearch();
        setupListeners();
        loadMuscleGroupsAndEquipment();
        loadExercises();
    }

    private void initViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        searchEditText = view.findViewById(R.id.searchEditText);
        clearSearchButton = view.findViewById(R.id.clearSearchButton);
        allExercisesButton = view.findViewById(R.id.allExercisesButton);
        musclesChipGroup = view.findViewById(R.id.musclesChipGroup);
        equipmentChipGroup = view.findViewById(R.id.equipmentChipGroup);
        exercisesRecyclerView = view.findViewById(R.id.exercisesRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyText = view.findViewById(R.id.emptyText);
        addExerciseButton = view.findViewById(R.id.addExerciseButton);
    }

    private void setupToolbar() {
        toolbar.setTitle(R.string.fragment_exercises);
    }

    private void loadMuscleGroupsAndEquipment() {
        // Загрузка групп мышц
        repository.getMuscleGroups(new ExerciseRepository.MuscleGroupsCallback() {
            @Override
            public void onSuccess(List<MuscleGroupResponse> muscleGroups) {
                if (isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        musclesChipGroup.removeAllViews();
                        muscleGroupIds.clear();

                        for (MuscleGroupResponse mg : muscleGroups) {
                            final String name = mg.getName();
                            final long id = mg.getId();

                            Chip chip = new Chip(getContext());
                            chip.setText(name);
                            chip.setCheckable(true);
                            chip.setId(View.generateViewId());

                            muscleGroupIds.put(name, id);

                            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                                if (isChecked) {
                                    selectedMuscle = name;
                                    applyFilters();
                                }
                            });

                            musclesChipGroup.addView(chip);
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                // Использовать заглушку
                setupMusclesChipsFallback();
            }
        });

        // Загрузка оборудования
        repository.getEquipment(new ExerciseRepository.EquipmentCallback() {
            @Override
            public void onSuccess(List<EquipmentResponse> equipmentList) {
                if (isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        equipmentChipGroup.removeAllViews();
                        equipmentIds.clear();

                        for (EquipmentResponse eq : equipmentList) {
                            final String name = eq.getName();
                            final long id = eq.getId();

                            Chip chip = new Chip(getContext());
                            chip.setText(name);
                            chip.setCheckable(true);
                            chip.setId(View.generateViewId());

                            equipmentIds.put(name, id);

                            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                                if (isChecked) {
                                    selectedEquipment = name;
                                    applyFilters();
                                }
                            });

                            equipmentChipGroup.addView(chip);
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                // Использовать заглушку
                setupEquipmentChipsFallback();
            }
        });
    }

    private void setupMusclesChipsFallback() {
        // Заглушка на случай ошибки API
        String[] muscles = {"Грудь", "Спина", "Ноги", "Плечи", "Бицепсы", "Трицепсы", "Пресс", "Предплечья", "Икры", "Ягодицы"};
        long[] ids = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

        if (isAdded()) {
            for (int i = 0; i < muscles.length; i++) {
                final int index = i;
                Chip chip = new Chip(getContext());
                chip.setText(muscles[index]);
                chip.setCheckable(true);
                chip.setId(View.generateViewId());

                muscleGroupIds.put(muscles[index], ids[index]);

                chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        selectedMuscle = muscles[index];
                        applyFilters();
                    }
                });

                musclesChipGroup.addView(chip);
            }
        }
    }

    private void setupEquipmentChipsFallback() {
        // Заглушка на случай ошибки API
        String[] equipment = {"Штанга", "Гантели", "Тренажёр", "Собственный вес", "Эспандер", "Гиря", "Тросовый тренажёр", "Smith machine"};
        long[] ids = {1, 2, 3, 4, 5, 6, 7, 8};

        if (isAdded()) {
            for (int i = 0; i < equipment.length; i++) {
                final int index = i;
                Chip chip = new Chip(getContext());
                chip.setText(equipment[index]);
                chip.setCheckable(true);
                chip.setId(View.generateViewId());

                equipmentIds.put(equipment[index], ids[index]);

                chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        selectedEquipment = equipment[index];
                        applyFilters();
                    }
                });

                equipmentChipGroup.addView(chip);
            }
        }
    }

    private void setupRecyclerView() {
        adapter = new ExerciseAdapter();
        exercisesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        exercisesRecyclerView.setAdapter(adapter);

        adapter.setOnExerciseClickListener(new ExerciseAdapter.OnExerciseClickListener() {
            @Override
            public void onExerciseClick(ExerciseResponse exercise) {
                Intent intent = new Intent(getContext(), ExerciseDetailActivity.class);
                intent.putExtra(ExerciseDetailActivity.EXTRA_EXERCISE_ID, exercise.getId());
                intent.putExtra(ExerciseDetailActivity.EXTRA_EXERCISE_NAME, exercise.getName());
                startActivity(intent);
            }

            @Override
            public void onExerciseLongClick(ExerciseResponse exercise) {
                Toast.makeText(getContext(), "Долгий клик: " + exercise.getName(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().trim();
                clearSearchButton.setVisibility(searchQuery.isEmpty() ? View.GONE : View.VISIBLE);
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
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

        addExerciseButton.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), CreateExerciseActivity.class);
            startActivityForResult(intent, REQUEST_CREATE_EXERCISE);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CREATE_EXERCISE && resultCode == AppCompatActivity.RESULT_OK) {
            // Перезагрузить список упражнений
            loadExercises();
        }
    }

    private static final int REQUEST_CREATE_EXERCISE = 1;

    private void loadExercises() {
        showLoading(true);
        repository.getExercises(null, null, null, new ExerciseRepository.ExercisesCallback() {
            @Override
            public void onSuccess(List<ExerciseResponse> exercises) {
                showLoading(false);
                allExercises.clear();
                allExercises.addAll(exercises);
                applyFilters();
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                String message = error != null && !error.isEmpty() ? error : getString(R.string.error_unknown);
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                emptyText.setText(message);
                emptyText.setVisibility(View.VISIBLE);
            }
        });
    }

    private void applyFilters() {
        List<ExerciseResponse> filtered = allExercises.stream()
            .filter(exercise -> {
                // Фильтр по мышцам (только целевые)
                if (selectedMuscle != null && !selectedMuscle.isEmpty()) {
                    boolean hasTargetMuscle = exercise.getMuscleGroups() != null &&
                        exercise.getMuscleGroups().stream()
                            .anyMatch(m -> m.getName().equals(selectedMuscle) && 
                                         m.getIsPrimary() != null && m.getIsPrimary());
                    if (!hasTargetMuscle) return false;
                }

                // Фильтр по оборудованию
                if (selectedEquipment != null && !selectedEquipment.isEmpty()) {
                    boolean hasEquipment = exercise.getEquipment() != null &&
                        exercise.getEquipment().stream()
                            .anyMatch(e -> e.getName().equals(selectedEquipment));
                    if (!hasEquipment) return false;
                }

                // Фильтр по поиску
                if (!searchQuery.isEmpty()) {
                    String query = searchQuery.toLowerCase();
                    boolean matchesName = exercise.getName() != null &&
                        exercise.getName().toLowerCase().contains(query);
                    boolean matchesDescription = exercise.getDescription() != null &&
                        exercise.getDescription().toLowerCase().contains(query);
                    if (!matchesName && !matchesDescription) return false;
                }

                return true;
            })
            .collect(Collectors.toList());

        adapter.setExercises(filtered);
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
            emptyText.setText(R.string.no_exercises);
        } else {
            exercisesRecyclerView.setVisibility(View.VISIBLE);
            emptyText.setVisibility(View.GONE);
        }
    }

    @Override
    public int getMenuItemId() {
        return R.id.navigation_exercises;
    }

    @Override
    public String getTitle() {
        return getString(R.string.fragment_exercises);
    }
}
