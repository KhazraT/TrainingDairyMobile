package ru.squidory.trainingdairymobile.ui.trainings;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ru.squidory.trainingdairymobile.R;
import ru.squidory.trainingdairymobile.data.model.ExerciseResponse;
import ru.squidory.trainingdairymobile.data.model.WorkoutExerciseResponse;

/**
 * Вложенный адаптер для упражнений внутри суперсета.
 */
class SupersetExercisesAdapter extends RecyclerView.Adapter<SupersetExercisesAdapter.ViewHolder> {

    private final List<WorkoutExerciseResponse> exercises;
    private final WorkoutExerciseAdapter.OnExerciseActionListener listener;
    private final Map<Long, ExerciseResponse> exerciseMap;
    private final WorkoutExerciseAdapter.OnSupersetExerciseMoved movedListener;
    private final int supersetGroup;

    SupersetExercisesAdapter(List<WorkoutExerciseResponse> exercises,
                             WorkoutExerciseAdapter.OnExerciseActionListener listener,
                             Map<Long, ExerciseResponse> exerciseMap,
                             WorkoutExerciseAdapter.OnSupersetExerciseMoved movedListener,
                             int supersetGroup) {
        this.exercises = new ArrayList<>(exercises);
        this.listener = listener;
        this.exerciseMap = exerciseMap;
        this.movedListener = movedListener;
        this.supersetGroup = supersetGroup;
    }

    /**
     * Переместить упражнение внутри суперсета.
     */
    public void moveItem(int fromPos, int toPos) {
        if (fromPos < 0 || fromPos >= exercises.size() || toPos < 0 || toPos >= exercises.size()) return;
        if (fromPos == toPos) return;

        WorkoutExerciseResponse moved = exercises.remove(fromPos);
        int insertPos = toPos;
        if (fromPos < toPos) insertPos--;
        insertPos = Math.max(0, Math.min(insertPos, exercises.size()));
        exercises.add(insertPos, moved);

        // Пересчитываем order
        for (int i = 0; i < exercises.size(); i++) {
            exercises.get(i).setExerciseOrder(i + 1);
        }

        if (movedListener != null) {
            movedListener.onExerciseMovedWithinSuperset(supersetGroup, fromPos, toPos);
        }

        // Используем notifyItemMoved для плавной анимации
        notifyItemMoved(fromPos, insertPos);
    }

    public List<WorkoutExerciseResponse> getExercises() { return exercises; }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_superset_exercise_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(exercises.get(position));
    }

    @Override
    public int getItemCount() { return exercises.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView exerciseNameText;
        private final TextView musclesText;
        private final TextView equipmentText;
        private final TextView exerciseSetsText;
        private final ImageButton manageSetsButton;
        private final ImageButton deleteExerciseButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            exerciseNameText = itemView.findViewById(R.id.exerciseNameText);
            musclesText = itemView.findViewById(R.id.musclesText);
            equipmentText = itemView.findViewById(R.id.equipmentText);
            exerciseSetsText = itemView.findViewById(R.id.exerciseSetsText);
            manageSetsButton = itemView.findViewById(R.id.manageSetsButton);
            deleteExerciseButton = itemView.findViewById(R.id.deleteExerciseButton);
        }

        void bind(WorkoutExerciseResponse exercise) {
            ExerciseResponse full = (exercise.getExercise() != null) ? exercise.getExercise() :
                    (exerciseMap != null ? exerciseMap.get(exercise.getExerciseId()) : null);
            exerciseNameText.setText(full != null ? full.getName() : "Упражнение");

            if (full != null && full.getMuscleGroups() != null) {
                List<String> primary = new ArrayList<>();
                for (ru.squidory.trainingdairymobile.data.model.MuscleGroupResponse mg : full.getMuscleGroups()) {
                    if (mg.getIsPrimary() != null && mg.getIsPrimary()) primary.add(mg.getName());
                }
                if (!primary.isEmpty()) {
                    musclesText.setText(String.join(", ", primary));
                    musclesText.setVisibility(View.VISIBLE);
                } else { musclesText.setVisibility(View.GONE); }
            } else { musclesText.setVisibility(View.GONE); }

            if (full != null && full.getEquipment() != null) {
                List<String> equip = new ArrayList<>();
                for (ru.squidory.trainingdairymobile.data.model.EquipmentResponse eq : full.getEquipment()) {
                    equip.add(eq.getName());
                }
                if (!equip.isEmpty()) {
                    equipmentText.setText(String.join(", ", equip));
                    equipmentText.setVisibility(View.VISIBLE);
                } else { equipmentText.setVisibility(View.GONE); }
            } else { equipmentText.setVisibility(View.GONE); }

            exerciseSetsText.setText("Подходов: " + (exercise.getSetsCount() != null ? exercise.getSetsCount() : 0));

            itemView.setOnClickListener(v -> { if (listener != null) listener.onEditExercise(exercise); });
            manageSetsButton.setOnClickListener(v -> { if (listener != null) listener.onManageSets(exercise); });
            deleteExerciseButton.setOnClickListener(v -> { if (listener != null) listener.onDeleteExercise(exercise); });
        }
    }
}
