package ru.squidory.trainingdairymobile.ui.trainings;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.squidory.trainingdairymobile.R;
import ru.squidory.trainingdairymobile.data.model.WorkoutResponse;

/**
 * Адаптер для списка тренировок с поддержкой drag-and-drop и режима редактирования.
 */
public class WorkoutDetailAdapter extends RecyclerView.Adapter<WorkoutDetailAdapter.WorkoutViewHolder> {

    private final List<WorkoutResponse> workouts = new ArrayList<>();
    private final Map<Long, Integer> exerciseCounts = new HashMap<>();
    private OnWorkoutActionListener listener;
    private boolean editMode = false;
    private ItemTouchHelper itemTouchHelper;

    public void setItemTouchHelper(ItemTouchHelper helper) {
        this.itemTouchHelper = helper;
    }

    public interface OnWorkoutActionListener {
        void onDeleteWorkout(WorkoutResponse workout, int position);
        void onManageExercises(WorkoutResponse workout);
        void onEditWorkout(WorkoutResponse workout);
        void onWorkoutMoved(WorkoutResponse workout, int fromPosition, int toPosition);
    }

    public void setOnWorkoutActionListener(OnWorkoutActionListener listener) {
        this.listener = listener;
    }

    public OnWorkoutActionListener getOnWorkoutActionListener() {
        return listener;
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
        notifyDataSetChanged();
    }

    public void setWorkouts(List<WorkoutResponse> workouts) {
        this.workouts.clear();
        if (workouts != null) {
            this.workouts.addAll(workouts);
        }
        notifyDataSetChanged();
    }

    public void setExerciseCounts(Map<Long, Integer> counts) {
        this.exerciseCounts.clear();
        if (counts != null) {
            this.exerciseCounts.putAll(counts);
        }
        notifyDataSetChanged();
    }

    public List<WorkoutResponse> getWorkouts() {
        return workouts;
    }

    public void moveItem(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                java.util.Collections.swap(workouts, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                java.util.Collections.swap(workouts, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
        // Обновить номера
        notifyItemRangeChanged(Math.min(fromPosition, toPosition), Math.abs(toPosition - fromPosition) + 1);
    }

    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workout_detail, parent, false);
        return new WorkoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
        holder.bind(workouts.get(position), position);
    }

    @Override
    public int getItemCount() {
        return workouts.size();
    }

    class WorkoutViewHolder extends RecyclerView.ViewHolder {
        private final TextView orderText;
        private final TextView nameText;
        private final TextView commentText;
        private final TextView exerciseCountText;
        private final ImageView dragHandle;
        private final LinearLayout actionButtonsLayout;
        private final MaterialButton manageButton;
        private final ImageButton deleteButton;

        WorkoutViewHolder(@NonNull View itemView) {
            super(itemView);
            orderText = itemView.findViewById(R.id.workoutOrderText);
            nameText = itemView.findViewById(R.id.workoutNameText);
            commentText = itemView.findViewById(R.id.workoutCommentText);
            exerciseCountText = itemView.findViewById(R.id.exerciseCountText);
            dragHandle = itemView.findViewById(R.id.dragHandle);
            actionButtonsLayout = itemView.findViewById(R.id.actionButtonsLayout);
            manageButton = itemView.findViewById(R.id.manageExercisesButton);
            deleteButton = itemView.findViewById(R.id.deleteWorkoutButton);
        }

        void bind(WorkoutResponse workout, int position) {
            orderText.setText("#" + (position + 1));
            nameText.setText(workout.getName());

            if (workout.getComment() != null && !workout.getComment().isEmpty()) {
                commentText.setText(workout.getComment());
                commentText.setVisibility(View.VISIBLE);
            } else {
                commentText.setVisibility(View.GONE);
            }

            // Количество упражнений
            Integer count = exerciseCounts.get(workout.getId());
            if (count != null) {
                exerciseCountText.setText("Упражнений: " + count);
                exerciseCountText.setVisibility(View.VISIBLE);
            } else {
                exerciseCountText.setVisibility(View.GONE);
            }

            // Показываем элементы управления только в режиме редактирования
            int visibility = editMode ? View.VISIBLE : View.GONE;
            dragHandle.setVisibility(visibility);
            actionButtonsLayout.setVisibility(visibility);

            // Клик по всему элементу
            itemView.setOnClickListener(v -> {
                if (editMode) {
                    // В режиме редактирования — открываем диалог редактирования
                    if (listener != null) {
                        listener.onEditWorkout(workout);
                    }
                } else {
                    // Вне режима редактирования — открываем содержимое тренировки
                    Context context = v.getContext();
                    Intent intent = new Intent(context, WorkoutContentActivity.class);
                    intent.putExtra(WorkoutContentActivity.EXTRA_WORKOUT_ID, workout.getId());
                    intent.putExtra(WorkoutContentActivity.EXTRA_WORKOUT_NAME, workout.getName());
                    intent.putExtra(WorkoutContentActivity.EXTRA_WORKOUT_COMMENT, workout.getComment());
                    context.startActivity(intent);
                }
            });

            manageButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onManageExercises(workout);
                }
                // Запускаем ExerciseManagementActivity
                Context context = v.getContext();
                Intent intent = new Intent(context, ExerciseManagementActivity.class);
                intent.putExtra(ExerciseManagementActivity.EXTRA_WORKOUT_ID, workout.getId());
                intent.putExtra(ExerciseManagementActivity.EXTRA_WORKOUT_NAME, workout.getName());
                context.startActivity(intent);
            });

            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteWorkout(workout, getAdapterPosition());
                }
            });

            // Drag handle — начать перетаскивание
            dragHandle.setOnTouchListener((v, event) -> {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN && itemTouchHelper != null && editMode) {
                    itemTouchHelper.startDrag(this);
                    return true;
                }
                return false;
            });
        }
    }
}
