package ru.squidory.trainingdairymobile.ui.trainings;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.squidory.trainingdairymobile.R;
import ru.squidory.trainingdairymobile.data.model.ProgramResponse;

/**
 * Адаптер для списка программ тренировок.
 */
public class ProgramAdapter extends RecyclerView.Adapter<ProgramAdapter.ProgramViewHolder> {

    private final List<ProgramResponse> programs = new ArrayList<>();
    private final Map<Long, Integer> workoutCounts = new HashMap<>();
    private OnProgramClickListener listener;

    public interface OnProgramClickListener {
        void onProgramClick(ProgramResponse program);
        void onProgramLongClick(ProgramResponse program);
    }

    public void setOnProgramClickListener(OnProgramClickListener listener) {
        this.listener = listener;
    }

    public void setPrograms(List<ProgramResponse> programs) {
        this.programs.clear();
        this.programs.addAll(programs);
        notifyDataSetChanged();
    }

    public void updateWorkoutCounts(Map<Long, Integer> counts) {
        this.workoutCounts.clear();
        this.workoutCounts.putAll(counts);
        notifyDataSetChanged();
    }

    public List<ProgramResponse> getPrograms() {
        return programs;
    }

    @NonNull
    @Override
    public ProgramViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_program, parent, false);
        return new ProgramViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProgramViewHolder holder, int position) {
        holder.bind(programs.get(position));
    }

    @Override
    public int getItemCount() {
        return programs.size();
    }

    class ProgramViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameText;
        private final TextView descriptionText;
        private final TextView workoutsCountText;
        private final TextView createdAtText;

        ProgramViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.programNameText);
            descriptionText = itemView.findViewById(R.id.programDescriptionText);
            workoutsCountText = itemView.findViewById(R.id.workoutsCountText);
            createdAtText = itemView.findViewById(R.id.createdAtText);
        }

        void bind(ProgramResponse program) {
            nameText.setText(program.getName());

            if (program.getDescription() != null && !program.getDescription().isEmpty()) {
                descriptionText.setText(program.getDescription());
                descriptionText.setVisibility(View.VISIBLE);
            } else {
                descriptionText.setVisibility(View.GONE);
            }

            // Дата создания
            if (program.getCreatedAt() != null) {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault());
                createdAtText.setText(sdf.format(program.getCreatedAt()));
                createdAtText.setVisibility(View.VISIBLE);
            } else {
                createdAtText.setVisibility(View.GONE);
            }

            // Количество тренировок
            Integer count = workoutCounts.get(program.getId());
            if (count != null) {
                workoutsCountText.setText(String.format(java.util.Locale.getDefault(), 
                    itemView.getContext().getString(R.string.workouts_count), count));
                workoutsCountText.setVisibility(View.VISIBLE);
            } else {
                workoutsCountText.setVisibility(View.GONE);
            }

            // Обработчики кликов
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProgramClick(program);
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onProgramLongClick(program);
                    return true;
                }
                return false;
            });
        }
    }
}
