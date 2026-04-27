package ru.squidory.trainingdairymobile.ui.statistics;

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
import ru.squidory.trainingdairymobile.data.model.ExerciseStatsResponse;

/**
 * Адаптер для отображения статистики по упражнениям.
 */
public class ExerciseStatsAdapter extends RecyclerView.Adapter<ExerciseStatsAdapter.ViewHolder> {

    private List<ExerciseStatsResponse> exercises = new ArrayList<>();
    private OnExerciseClickListener listener;

    public interface OnExerciseClickListener {
        void onExerciseClick(ExerciseStatsResponse exercise);
    }

    public void setOnExerciseClickListener(OnExerciseClickListener listener) {
        this.listener = listener;
    }

    public void setExercises(List<ExerciseStatsResponse> exercises) {
        this.exercises = exercises != null ? exercises : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercise_stat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(exercises.get(position));
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameText;
        private final TextView volumeText;
        private final TextView setsText;
        private final TextView maxWeightText;

        ViewHolder(View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.exerciseNameText);
            volumeText = itemView.findViewById(R.id.totalVolumeText);
            setsText = itemView.findViewById(R.id.totalSetsText);
            maxWeightText = itemView.findViewById(R.id.maxWeightText);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onExerciseClick(exercises.get(position));
                }
            });
        }

        void bind(ExerciseStatsResponse exercise) {
            nameText.setText(exercise.getExerciseName() != null ? exercise.getExerciseName() : "Без названия");

            double vol = exercise.getTotalVolume() != null ? exercise.getTotalVolume() : 0;
            if (vol >= 1000) {
                volumeText.setText(String.format(Locale.getDefault(), "%.1f т", vol / 1000.0));
            } else {
                volumeText.setText(String.format(Locale.getDefault(), "%.0f кг", vol));
            }

            int sets = exercise.getTotalSets() != null ? exercise.getTotalSets() : 0;
            setsText.setText(String.valueOf(sets));

            double maxW = exercise.getMaxWeight() != null ? exercise.getMaxWeight() : 0;
            maxWeightText.setText(String.format(Locale.getDefault(), "%.1f кг", maxW));
        }
    }
}