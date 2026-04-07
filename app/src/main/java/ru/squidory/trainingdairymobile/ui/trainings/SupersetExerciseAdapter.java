package ru.squidory.trainingdairymobile.ui.trainings;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.squidory.trainingdairymobile.R;
import ru.squidory.trainingdairymobile.data.model.ExerciseResponse;
import ru.squidory.trainingdairymobile.data.model.WorkoutExerciseResponse;

import java.util.Map;

/**
 * Простой адаптер для мультивыбора упражнений из тренировки (суперсет).
 */
public class SupersetExerciseAdapter extends RecyclerView.Adapter<SupersetExerciseAdapter.ViewHolder> {

    private final List<WorkoutExerciseResponse> exercises = new ArrayList<>();
    private final Set<Long> selectedIds = new HashSet<>();
    private Map<Long, ExerciseResponse> exerciseMap;
    private Runnable onSelectionChanged;

    public void setExerciseMap(Map<Long, ExerciseResponse> map) {
        this.exerciseMap = map;
        notifyDataSetChanged();
    }

    public void setOnSelectionChanged(Runnable onSelectionChanged) {
        this.onSelectionChanged = onSelectionChanged;
    }

    public void setExercises(List<WorkoutExerciseResponse> exercises) {
        this.exercises.clear();
        this.selectedIds.clear();
        if (exercises != null) this.exercises.addAll(exercises);
        notifyDataSetChanged();
    }

    /** Установить упражнения и уведомить об изменении выбора. */
    public void setExercisesAndNotifySelection(List<WorkoutExerciseResponse> exercises) {
        setExercises(exercises);
        if (onSelectionChanged != null) onSelectionChanged.run();
    }

    public List<WorkoutExerciseResponse> getSelectedExercises() {
        List<WorkoutExerciseResponse> result = new ArrayList<>();
        for (WorkoutExerciseResponse ex : exercises) {
            if (selectedIds.contains(ex.getId())) result.add(ex);
        }
        return result;
    }

    public int getSelectedCount() { return selectedIds.size(); }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercise_picker, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(exercises.get(position));
    }

    @Override
    public int getItemCount() { return exercises.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final CheckBox checkBox;
        private final TextView exerciseNameText;
        private final TextView musclesText;
        private final TextView equipmentText;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.exerciseCheckBox);
            exerciseNameText = itemView.findViewById(R.id.exerciseNameText);
            musclesText = itemView.findViewById(R.id.musclesText);
            equipmentText = itemView.findViewById(R.id.equipmentText);
        }

        void bind(WorkoutExerciseResponse exercise) {
            // Ищем название: сначала из вложенного объекта, потом из мапы
            String name = "Упражнение #" + exercise.getExerciseId();
            if (exercise.getExercise() != null && exercise.getExercise().getName() != null) {
                name = exercise.getExercise().getName();
            } else if (exerciseMap != null) {
                ExerciseResponse full = exerciseMap.get(exercise.getExerciseId());
                if (full != null && full.getName() != null) {
                    name = full.getName();
                }
            }
            exerciseNameText.setText(name);
            musclesText.setVisibility(View.GONE);
            equipmentText.setVisibility(View.GONE);

            long id = exercise.getId();
            checkBox.setChecked(selectedIds.contains(id));
            checkBox.setOnCheckedChangeListener((v, checked) -> {
                if (checked) selectedIds.add(id);
                else selectedIds.remove(id);
                if (onSelectionChanged != null) onSelectionChanged.run();
            });
            itemView.setOnClickListener(v -> checkBox.setChecked(!checkBox.isChecked()));
        }
    }
}
