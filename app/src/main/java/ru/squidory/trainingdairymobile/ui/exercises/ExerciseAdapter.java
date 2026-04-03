package ru.squidory.trainingdairymobile.ui.exercises;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ru.squidory.trainingdairymobile.R;
import ru.squidory.trainingdairymobile.data.model.ExerciseResponse;

/**
 * Адаптер для списка упражнений.
 */
public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder> {

    private final List<ExerciseResponse> exercises = new ArrayList<>();
    private OnExerciseClickListener listener;

    public interface OnExerciseClickListener {
        void onExerciseClick(ExerciseResponse exercise);
        void onExerciseLongClick(ExerciseResponse exercise);
    }

    public void setOnExerciseClickListener(OnExerciseClickListener listener) {
        this.listener = listener;
    }

    public void setExercises(List<ExerciseResponse> exercises) {
        this.exercises.clear();
        this.exercises.addAll(exercises);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercise, parent, false);
        return new ExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        holder.bind(exercises.get(position));
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    class ExerciseViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameText;
        private final TextView muscleGroupsText;
        private final TextView equipmentText;

        ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.exerciseNameText);
            muscleGroupsText = itemView.findViewById(R.id.muscleGroupsText);
            equipmentText = itemView.findViewById(R.id.equipmentText);
        }

        void bind(ExerciseResponse exercise) {
            nameText.setText(exercise.getName());

            // Целевые мышцы (только isPrimary=true)
            if (exercise.getMuscleGroups() != null) {
                StringBuilder muscles = new StringBuilder();
                boolean hasMuscles = false;
                for (int i = 0; i < exercise.getMuscleGroups().size(); i++) {
                    var muscle = exercise.getMuscleGroups().get(i);
                    if (muscle.getIsPrimary() != null && muscle.getIsPrimary()) {
                        if (hasMuscles) muscles.append(", ");
                        muscles.append(muscle.getName());
                        hasMuscles = true;
                    }
                }
                if (hasMuscles) {
                    muscleGroupsText.setText(muscles.toString());
                    muscleGroupsText.setVisibility(View.VISIBLE);
                } else {
                    muscleGroupsText.setVisibility(View.GONE);
                }
            } else {
                muscleGroupsText.setVisibility(View.GONE);
            }

            // Оборудование
            if (exercise.getEquipment() != null && !exercise.getEquipment().isEmpty()) {
                StringBuilder equipment = new StringBuilder();
                for (int i = 0; i < exercise.getEquipment().size(); i++) {
                    if (i > 0) equipment.append(", ");
                    equipment.append(exercise.getEquipment().get(i).getName());
                }
                equipmentText.setText("🏋️ " + equipment.toString());
                equipmentText.setVisibility(View.VISIBLE);
            } else {
                equipmentText.setVisibility(View.GONE);
            }

            // Обработчики кликов
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onExerciseClick(exercise);
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onExerciseLongClick(exercise);
                    return true;
                }
                return false;
            });
        }
    }
}
