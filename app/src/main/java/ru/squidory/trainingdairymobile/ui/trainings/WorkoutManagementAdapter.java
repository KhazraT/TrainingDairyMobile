package ru.squidory.trainingdairymobile.ui.trainings;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import ru.squidory.trainingdairymobile.R;
import ru.squidory.trainingdairymobile.data.model.WorkoutResponse;

/**
 * Адаптер для списка тренировок в режиме управления.
 */
public class WorkoutManagementAdapter extends RecyclerView.Adapter<WorkoutManagementAdapter.WorkoutViewHolder> {

    private final List<WorkoutResponse> workouts = new ArrayList<>();
    private OnWorkoutActionListener listener;

    public interface OnWorkoutActionListener {
        void onManageExercises(WorkoutResponse workout);
        void onDeleteWorkout(WorkoutResponse workout);
    }

    public void setOnWorkoutActionListener(OnWorkoutActionListener listener) {
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
                .inflate(R.layout.item_workout_management, parent, false);
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
        private final MaterialButton manageButton;
        private final ImageButton deleteButton;

        WorkoutViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.workoutNameText);
            commentText = itemView.findViewById(R.id.workoutCommentText);
            manageButton = itemView.findViewById(R.id.manageExercisesButton);
            deleteButton = itemView.findViewById(R.id.deleteWorkoutButton);
        }

        void bind(WorkoutResponse workout) {
            nameText.setText(workout.getName());

            if (workout.getComment() != null && !workout.getComment().isEmpty()) {
                commentText.setText(workout.getComment());
                commentText.setVisibility(View.VISIBLE);
            } else {
                commentText.setVisibility(View.GONE);
            }

            manageButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onManageExercises(workout);
                }
            });

            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteWorkout(workout);
                }
            });
        }
    }
}
