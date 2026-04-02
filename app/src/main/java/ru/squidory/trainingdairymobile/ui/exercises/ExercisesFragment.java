package ru.squidory.trainingdairymobile.ui.exercises;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ru.squidory.trainingdairymobile.R;
import ru.squidory.trainingdairymobile.data.model.ExerciseResponse;
import ru.squidory.trainingdairymobile.data.repository.ExerciseRepository;
import ru.squidory.trainingdairymobile.ui.main.BaseFragment;

/**
 * Фрагмент раздела "Упражнения".
 * Отображает список упражнений с фильтрацией по мышцам и оборудованию.
 */
public class ExercisesFragment extends BaseFragment {

    private RecyclerView exercisesRecyclerView;
    private ProgressBar progressBar;
    private TextView emptyText;
    private Spinner muscleSpinner;
    private Spinner equipmentSpinner;
    private ImageButton addExerciseButton;

    private ExerciseAdapter adapter;
    private ExerciseRepository repository;

    private String selectedMuscle = null;
    private String selectedEquipment = null;

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
        setupRecyclerView();
        setupSpinners();
        setupListeners();
        loadExercises();
    }

    private void initViews(View view) {
        exercisesRecyclerView = view.findViewById(R.id.exercisesRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyText = view.findViewById(R.id.emptyText);
        muscleSpinner = view.findViewById(R.id.muscleSpinner);
        equipmentSpinner = view.findViewById(R.id.equipmentSpinner);
        addExerciseButton = view.findViewById(R.id.addExerciseButton);
    }

    private void setupRecyclerView() {
        adapter = new ExerciseAdapter();
        exercisesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        exercisesRecyclerView.setAdapter(adapter);

        adapter.setOnExerciseClickListener(new ExerciseAdapter.OnExerciseClickListener() {
            @Override
            public void onExerciseClick(ExerciseResponse exercise) {
                // Открыть детали упражнения
                openExerciseDetail(exercise);
            }

            @Override
            public void onExerciseLongClick(ExerciseResponse exercise) {
                // Показать меню действий (редактировать/удалить)
                showExerciseOptions(exercise);
            }
        });
    }

    private void setupSpinners() {
        // Фильтр по мышцам
        List<String> muscles = new ArrayList<>();
        muscles.add(getString(R.string.all_muscles));
        muscles.add("Грудь");
        muscles.add("Спина");
        muscles.add("Ноги");
        muscles.add("Плечи");
        muscles.add("Бицепсы");
        muscles.add("Трицепсы");
        muscles.add("Пресс");
        muscles.add("Предплечья");
        muscles.add("Икры");
        muscles.add("Ягодицы");

        ArrayAdapter<String> muscleAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, muscles);
        muscleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        muscleSpinner.setAdapter(muscleAdapter);

        muscleSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedMuscle = null;
                } else {
                    selectedMuscle = muscles.get(position);
                }
                applyFilters();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                selectedMuscle = null;
            }
        });

        // Фильтр по оборудованию
        List<String> equipment = new ArrayList<>();
        equipment.add(getString(R.string.all_equipment));
        equipment.add("Штанга");
        equipment.add("Гантели");
        equipment.add("Тренажёр");
        equipment.add("Собственный вес");
        equipment.add("Эспандер");
        equipment.add("Гиря");

        ArrayAdapter<String> equipmentAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, equipment);
        equipmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        equipmentSpinner.setAdapter(equipmentAdapter);

        equipmentSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedEquipment = null;
                } else {
                    selectedEquipment = equipment.get(position);
                }
                applyFilters();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                selectedEquipment = null;
            }
        });
    }

    private void setupListeners() {
        addExerciseButton.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Создание упражнения (в разработке)", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadExercises() {
        showLoading(true);
        repository.getExercises(null, null, null, new ExerciseRepository.ExercisesCallback() {
            @Override
            public void onSuccess(List<ExerciseResponse> exercises) {
                showLoading(false);
                adapter.setExercises(exercises);
                checkEmptyState();
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                String message = error != null && !error.isEmpty() ? error : getString(R.string.error_unknown);
                // Показываем более подробное сообщение об ошибке
                if (message.contains("401") || message.contains("Unauthorized")) {
                    message = "Сессия истекла. Пожалуйста, войдите заново.";
                } else if (message.contains("500") || message.contains("Internal")) {
                    message = "Ошибка сервера. Попробуйте позже.";
                } else if (message.contains("timeout") || message.contains("Unable to resolve host")) {
                    message = "Нет подключения к серверу. Проверьте, запущен ли бэкенд.";
                } else if (message.contains("connection") || message.contains("refused")) {
                    message = "Нет подключения. Выполните: adb reverse tcp:8080 tcp:8080";
                }
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                emptyText.setText(message);
                emptyText.setVisibility(View.VISIBLE);
            }
        });
    }

    private void applyFilters() {
        adapter.filter(selectedMuscle, selectedEquipment);
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

    private void openExerciseDetail(ExerciseResponse exercise) {
        // TODO: Открыть ExerciseDetailActivity
        Toast.makeText(getContext(), "Детали: " + exercise.getName(), Toast.LENGTH_SHORT).show();
    }

    private void showExerciseOptions(ExerciseResponse exercise) {
        // TODO: Показать диалог с опциями
        Toast.makeText(getContext(), "Удержание: " + exercise.getName(), Toast.LENGTH_SHORT).show();
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
