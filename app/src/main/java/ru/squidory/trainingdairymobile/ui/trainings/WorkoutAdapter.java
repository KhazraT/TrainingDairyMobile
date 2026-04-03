package ru.squidory.trainingdairymobile.ui.trainings;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import ru.squidory.trainingdairymobile.R;
import ru.squidory.trainingdairymobile.data.model.WorkoutResponse;

/**
 * Адаптер для списка тренировок в программе.
 */
public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder> {

    private final List<WorkoutResponse> workouts = new ArrayList<>();
    private OnWorkoutClickListener listener;

    public interface OnWorkoutClickListener {
        void onWorkoutClick(WorkoutResponse workout);
        void onStartWorkout(WorkoutResponse workout);
        void onWorkoutLongClick(WorkoutResponse workout);
    }

    public void setOnWorkoutClickListener(OnWorkoutClickListener listener) {
        this.listener = listener;
    }

    public void setWorkouts(List<WorkoutResponse> workouts) {
        this.workouts.clear();
        this.workouts.addAll(workouts);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workout, parent, false);
        return new WorkoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
        holder.bind(workouts.get(position));
    }

    @Override
    public int getItemCount() {
        return workouts.size();
    }

    class WorkoutViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameText;
        private final TextView commentText;
        private final TextView exercisesCountText;
        private final MaterialButton startButton;

        WorkoutViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.workoutNameText);
            commentText = itemView.findViewById(R.id.workoutCommentText);
            exercisesCountText = itemView.findViewById(R.id.exercisesCountText);
            startButton = itemView.findViewById(R.id.startWorkoutButton);
        }

        void bind(WorkoutResponse workout) {
            nameText.setText(workout.getName());

            if (workout.getComment() != null && !workout.getComment().isEmpty()) {
                commentText.setText(workout.getComment());
                commentText.setVisibility(View.VISIBLE);
            } else {
                commentText.setVisibility(View.GONE);
            }

            // TODO: Загрузить количество упражнений для этой тренировки
            exercisesCountText.setText(itemView.getContext().getString(R.string.exercises_in_workout, 0));

            // Обработчики кликов
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onWorkoutClick(workout);
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onWorkoutLongClick(workout);
                    return true;
                }
                return false;
            });

            startButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onStartWorkout(workout);
                }
            });
        }
    }
}
