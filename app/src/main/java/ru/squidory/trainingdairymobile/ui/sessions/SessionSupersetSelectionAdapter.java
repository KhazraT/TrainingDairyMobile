package ru.squidory.trainingdairymobile.ui.sessions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.squidory.trainingdairymobile.R;
import ru.squidory.trainingdairymobile.data.model.SessionExerciseResponse;

/**
 * Адаптер мультивыбора упражнений для создания суперсета в сессии.
 */
class SessionSupersetSelectionAdapter extends RecyclerView.Adapter<SessionSupersetSelectionAdapter.ViewHolder> {

    private final List<SessionExerciseResponse> exercises = new ArrayList<>();
    private final Set<Integer> selectedPositions = new HashSet<>();
    private OnSelectionChangedListener selectionListener;

    interface OnSelectionChangedListener {
        void onSelectionChanged(int count);
    }

    void setOnSelectionChangedListener(OnSelectionChangedListener listener) {
        this.selectionListener = listener;
    }

    void setExercises(List<SessionExerciseResponse> list) {
        exercises.clear();
        selectedPositions.clear();
        if (list != null) exercises.addAll(list);
        notifyDataSetChanged();
    }

    List<SessionExerciseResponse> getSelectedExercises() {
        List<SessionExerciseResponse> result = new ArrayList<>();
        for (Integer pos : selectedPositions) {
            if (pos >= 0 && pos < exercises.size()) {
                result.add(exercises.get(pos));
            }
        }
        return result;
    }

    int getSelectedCount() {
        return selectedPositions.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_session_superset_selection, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(exercises.get(position), position);
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final CheckBox checkBox;
        private final TextView exerciseNameText;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkBox);
            exerciseNameText = itemView.findViewById(R.id.exerciseNameText);
        }

        void bind(SessionExerciseResponse exercise, int position) {
            checkBox.setOnCheckedChangeListener(null);
            checkBox.setChecked(selectedPositions.contains(position));

            String name = exercise.getExerciseName();
            if (name == null || name.isEmpty()) {
                name = "Упражнение";
            }
            exerciseNameText.setText(name);

            checkBox.setOnCheckedChangeListener((b, isChecked) -> {
                if (isChecked) {
                    selectedPositions.add(position);
                } else {
                    selectedPositions.remove(position);
                }
                if (selectionListener != null) {
                    selectionListener.onSelectionChanged(selectedPositions.size());
                }
            });

            itemView.setOnClickListener(v -> checkBox.setChecked(!checkBox.isChecked()));
        }
    }
}
