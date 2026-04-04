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
import java.util.Locale;
import java.util.Set;

import ru.squidory.trainingdairymobile.R;
import ru.squidory.trainingdairymobile.data.model.ExerciseResponse;

/**
 * Адаптер выбора упражнений — как ExerciseAdapter но с чекбоксами для мультивыбора.
 */
public class ExercisePickerAdapter extends RecyclerView.Adapter<ExercisePickerAdapter.PickerViewHolder> {

    private final List<ExerciseResponse> exercises = new ArrayList<>();
    private final Set<Long> selectedIds = new HashSet<>();
    private OnSelectionChangeListener selectionChangeListener;

    public interface OnSelectionChangeListener {
        void onSelectionChanged(int count);
    }

    public void setOnSelectionChangeListener(OnSelectionChangeListener listener) {
        this.selectionChangeListener = listener;
    }

    public void setExercises(List<ExerciseResponse> exercises, List<Long> excludeIds) {
        this.exercises.clear();
        if (exercises != null) {
            for (ExerciseResponse ex : exercises) {
                if (excludeIds == null || !excludeIds.contains(ex.getId())) {
                    this.exercises.add(ex);
                }
            }
        }
        notifyDataSetChanged();
        notifySelectionChanged();
    }

    public void clearSelection() {
        selectedIds.clear();
        notifyDataSetChanged();
        notifySelectionChanged();
    }

    public List<Long> getSelectedExerciseIds() {
        return new ArrayList<>(selectedIds);
    }

    public int getSelectedCount() {
        return selectedIds.size();
    }

    @NonNull
    @Override
    public PickerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercise_picker, parent, false);
        return new PickerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PickerViewHolder holder, int position) {
        holder.bind(exercises.get(position));
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    private void notifySelectionChanged() {
        if (selectionChangeListener != null) {
            selectionChangeListener.onSelectionChanged(selectedIds.size());
        }
    }

    class PickerViewHolder extends RecyclerView.ViewHolder {
        private final TextView exerciseNameText;
        private final TextView musclesText;
        private final TextView equipmentText;
        private final CheckBox checkBox;

        PickerViewHolder(@NonNull View itemView) {
            super(itemView);
            exerciseNameText = itemView.findViewById(R.id.exerciseNameText);
            musclesText = itemView.findViewById(R.id.musclesText);
            equipmentText = itemView.findViewById(R.id.equipmentText);
            checkBox = itemView.findViewById(R.id.exerciseCheckBox);
        }

        void bind(ExerciseResponse exercise) {
            exerciseNameText.setText(exercise.getName());

            // Целевые мышцы
            if (exercise.getMuscleGroups() != null && !exercise.getMuscleGroups().isEmpty()) {
                List<String> primaryMuscles = new ArrayList<>();
                for (ru.squidory.trainingdairymobile.data.model.MuscleGroupResponse mg : exercise.getMuscleGroups()) {
                    if (mg.getIsPrimary() != null && mg.getIsPrimary()) {
                        primaryMuscles.add(mg.getName());
                    }
                }
                if (!primaryMuscles.isEmpty()) {
                    musclesText.setText(String.join(", ", primaryMuscles));
                    musclesText.setVisibility(View.VISIBLE);
                } else {
                    musclesText.setVisibility(View.GONE);
                }
            } else {
                musclesText.setVisibility(View.GONE);
            }

            // Оборудование
            if (exercise.getEquipment() != null && !exercise.getEquipment().isEmpty()) {
                List<String> equipNames = new ArrayList<>();
                for (ru.squidory.trainingdairymobile.data.model.EquipmentResponse eq : exercise.getEquipment()) {
                    equipNames.add(eq.getName());
                }
                if (!equipNames.isEmpty()) {
                    equipmentText.setText(String.join(", ", equipNames));
                    equipmentText.setVisibility(View.VISIBLE);
                } else {
                    equipmentText.setVisibility(View.GONE);
                }
            } else {
                equipmentText.setVisibility(View.GONE);
            }

            // Чекбокс
            long exId = exercise.getId();
            checkBox.setChecked(selectedIds.contains(exId));
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedIds.add(exId);
                } else {
                    selectedIds.remove(exId);
                }
                notifySelectionChanged();
            });

            // Клик по всему item
            itemView.setOnClickListener(v -> checkBox.setChecked(!checkBox.isChecked()));
        }
    }
}
