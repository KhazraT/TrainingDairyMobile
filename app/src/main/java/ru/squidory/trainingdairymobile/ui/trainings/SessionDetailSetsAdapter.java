package ru.squidory.trainingdairymobile.ui.trainings;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ru.squidory.trainingdairymobile.R;
import ru.squidory.trainingdairymobile.data.model.SessionExerciseResponse;
import ru.squidory.trainingdairymobile.data.model.SessionSetResponse;

/**
 * Readonly адаптер для отображения подходов в деталях сессии.
 */
public class SessionDetailSetsAdapter extends RecyclerView.Adapter<SessionDetailSetsAdapter.SetViewHolder> {

    private final SessionExerciseResponse exercise;
    private List<SessionSetResponse> sortedSets = new ArrayList<>();

    public SessionDetailSetsAdapter(SessionExerciseResponse exercise) {
        this.exercise = exercise;
        updateSortedSets();
    }

    private void updateSortedSets() {
        List<SessionSetResponse> sets = exercise != null ? exercise.getCompletedSets() : null;
        if (sets == null) {
            sortedSets = new ArrayList<>();
        } else {
            sortedSets = new ArrayList<>(sets);
            sortedSets.sort((a, b) -> {
                int oa = a.getSetOrder() != null ? a.getSetOrder() : (a.getSetNumber() != null ? a.getSetNumber() : 0);
                int ob = b.getSetOrder() != null ? b.getSetOrder() : (b.getSetNumber() != null ? b.getSetNumber() : 0);
                return Integer.compare(oa, ob);
            });
        }
    }

    @NonNull
    @Override
    public SetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_session_detail_set, parent, false);
        return new SetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SetViewHolder holder, int position) {
        if (position >= sortedSets.size()) return;
        SessionSetResponse set = sortedSets.get(position);
        holder.bind(set, exercise, position);
    }

    @Override
    public int getItemCount() {
        return sortedSets.size();
    }

    static class SetViewHolder extends RecyclerView.ViewHolder {
        private final LinearLayout setItemRoot;
        private final TextView setNumberText;
        private final TextView setTypeLabel;
        private final LinearLayout weightColumn;
        private final TextView weightText;
        private final LinearLayout repsColumn;
        private final TextView repsText;
        private final LinearLayout timeColumn;
        private final TextView timeText;
        private final LinearLayout distanceColumn;
        private final TextView distanceText;
        private final LinearLayout restColumn;
        private final TextView restText;

        SetViewHolder(@NonNull View itemView) {
            super(itemView);
            setItemRoot = itemView.findViewById(R.id.setItemRoot);
            setNumberText = itemView.findViewById(R.id.setNumberText);
            setTypeLabel = itemView.findViewById(R.id.setTypeLabel);
            weightColumn = itemView.findViewById(R.id.weightColumn);
            weightText = itemView.findViewById(R.id.weightText);
            repsColumn = itemView.findViewById(R.id.repsColumn);
            repsText = itemView.findViewById(R.id.repsText);
            timeColumn = itemView.findViewById(R.id.timeColumn);
            timeText = itemView.findViewById(R.id.timeText);
            distanceColumn = itemView.findViewById(R.id.distanceColumn);
            distanceText = itemView.findViewById(R.id.distanceText);
            restColumn = itemView.findViewById(R.id.restColumn);
            restText = itemView.findViewById(R.id.restText);
        }

        void bind(SessionSetResponse set, SessionExerciseResponse exercise, int position) {
            setNumberText.setText(String.valueOf(position + 1));

            boolean isDropset = set.isDropset();
            boolean isDropsetPart = set.isDropsetPart();
            boolean isRestItem = set.isRest();

            // Дропсет - оранжевый фон
            if (isDropset || isDropsetPart) {
                setItemRoot.setBackgroundColor(0x1AFF9800);
                setTypeLabel.setVisibility(View.VISIBLE);
                setTypeLabel.setText(isDropset ? "дропсет" : "дроп");
            } else if (isRestItem) {
                setItemRoot.setBackgroundColor(0x00000000);
                setTypeLabel.setVisibility(View.GONE);
                setNumberText.setVisibility(View.INVISIBLE);
            } else {
                setItemRoot.setBackgroundResource(R.drawable.bg_session_detail_set);
                setTypeLabel.setVisibility(View.GONE);
            }

            String exerciseType = exercise.getExerciseType();
            if (exerciseType == null || exerciseType.isEmpty()) exerciseType = "REPS_WEIGHT";

            boolean isRepsWeight = "REPS_WEIGHT".equalsIgnoreCase(exerciseType);
            boolean isTimeWeight = "TIME_WEIGHT".equalsIgnoreCase(exerciseType);
            boolean isTimeDistance = "TIME_DISTANCE".equalsIgnoreCase(exerciseType);
            boolean isTimeWeightDistance = "TIME_WEIGHT_DISTANCE".equalsIgnoreCase(exerciseType);

            weightColumn.setVisibility(isRepsWeight || isTimeWeight || isTimeWeightDistance ? View.VISIBLE : View.GONE);
            repsColumn.setVisibility(isRepsWeight ? View.VISIBLE : View.GONE);
            timeColumn.setVisibility(isTimeWeight || isTimeDistance || isTimeWeightDistance ? View.VISIBLE : View.GONE);
            distanceColumn.setVisibility(isTimeDistance || isTimeWeightDistance ? View.VISIBLE : View.GONE);

            // Вес
            if (set.getWeight() != null) {
                weightText.setText(set.getWeight() + " кг");
            } else {
                weightText.setText("—");
            }

            // Повторения
            if (set.getReps() != null) {
                repsText.setText(String.valueOf(set.getReps()));
            } else {
                repsText.setText("—");
            }

            // Время
            if (set.getDurationSeconds() != null && set.getDurationSeconds() > 0) {
                int sec = set.getDurationSeconds();
                timeText.setText(String.format("%02d:%02d", sec / 60, sec % 60));
            } else {
                timeText.setText("—");
            }

            // Дистанция
            if (set.getDistanceMeters() != null && set.getDistanceMeters() > 0) {
                double km = set.getDistanceMeters() / 1000.0;
                distanceText.setText(String.format("%.2f км", km));
            } else {
                distanceText.setText("—");
            }

            // Отдых
            if (!isRestItem && set.getRestTime() != null && set.getRestTime() > 0) {
                restColumn.setVisibility(View.VISIBLE);
                int rest = set.getRestTime();
                restText.setText(String.format("%02d:%02d", rest / 60, rest % 60));
            } else {
                restColumn.setVisibility(View.GONE);
            }
        }
    }
}