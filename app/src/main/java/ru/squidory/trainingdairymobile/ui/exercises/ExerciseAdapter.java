package ru.squidory.trainingdairymobile.ui.exercises;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ru.squidory.trainingdairymobile.R;
import ru.squidory.trainingdairymobile.data.model.ExerciseResponse;

/**
 * Адаптер для списка упражнений.
 */
public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder> {

    private final List<ExerciseResponse> exercises = new ArrayList<>();
    private final List<ExerciseResponse> allExercises = new ArrayList<>();
    private OnExerciseClickListener listener;

    public interface OnExerciseClickListener {
        void onExerciseClick(ExerciseResponse exercise);
        void onExerciseLongClick(ExerciseResponse exercise);
    }

    public void setOnExerciseClickListener(OnExerciseClickListener listener) {
        this.listener = listener;
    }

    public void setExercises(List<ExerciseResponse> exercises) {
        this.allExercises.clear();
        this.allExercises.addAll(exercises);
        this.exercises.clear();
        this.exercises.addAll(exercises);
        notifyDataSetChanged();
    }

    public void filter(String muscle, String equipment) {
        exercises.clear();
        
        for (ExerciseResponse exercise : allExercises) {
            boolean matchesMuscle = muscle == null || muscle.isEmpty() || 
                    exercise.getMuscleGroups() != null && exercise.getMuscleGroups().stream()
                            .anyMatch(m -> m.getName().equals(muscle));
            
            boolean matchesEquipment = equipment == null || equipment.isEmpty() ||
                    exercise.getEquipment() != null && exercise.getEquipment().stream()
                            .anyMatch(e -> e.getName().equals(equipment));
            
            if (matchesMuscle && matchesEquipment) {
                exercises.add(exercise);
            }
        }
        
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
        private final TextView descriptionText;
        private final TextView muscleGroupsText;
        private final TextView typeText;

        ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.exerciseNameText);
            descriptionText = itemView.findViewById(R.id.exerciseDescriptionText);
            muscleGroupsText = itemView.findViewById(R.id.muscleGroupsText);
            typeText = itemView.findViewById(R.id.exerciseTypeText);
        }

        void bind(ExerciseResponse exercise) {
            nameText.setText(exercise.getName());
            
            if (exercise.getDescription() != null && !exercise.getDescription().isEmpty()) {
                descriptionText.setText(exercise.getDescription());
                descriptionText.setVisibility(View.VISIBLE);
            } else {
                descriptionText.setVisibility(View.GONE);
            }
            
            // Группы мышц
            if (exercise.getMuscleGroups() != null && !exercise.getMuscleGroups().isEmpty()) {
                StringBuilder muscles = new StringBuilder();
                for (int i = 0; i < exercise.getMuscleGroups().size(); i++) {
                    if (i > 0) muscles.append(", ");
                    muscles.append(exercise.getMuscleGroups().get(i).getName());
                }
                muscleGroupsText.setText(muscles.toString());
                muscleGroupsText.setVisibility(View.VISIBLE);
            } else {
                muscleGroupsText.setVisibility(View.GONE);
            }
            
            // Тип упражнения
            if (exercise.getExerciseType() != null) {
                typeText.setText(getExerciseTypeDisplayName(exercise.getExerciseType()));
                typeText.setVisibility(View.VISIBLE);
            } else {
                typeText.setVisibility(View.GONE);
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
        
        private String getExerciseTypeDisplayName(String type) {
            switch (type) {
                case "reps_weight":
                    return itemView.getContext().getString(R.string.exercise_type_reps_weight);
                case "time_weight":
                    return itemView.getContext().getString(R.string.exercise_type_time_weight);
                case "time_distance":
                    return itemView.getContext().getString(R.string.exercise_type_time_distance);
                case "time_weight_distance":
                    return itemView.getContext().getString(R.string.exercise_type_time_weight_distance);
                default:
                    return type;
            }
        }
    }
}
