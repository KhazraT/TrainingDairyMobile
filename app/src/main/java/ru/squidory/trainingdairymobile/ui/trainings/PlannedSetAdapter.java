package ru.squidory.trainingdairymobile.ui.trainings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ru.squidory.trainingdairymobile.R;
import ru.squidory.trainingdairymobile.data.model.PlannedSetResponse;

/**
 * Адаптер для списка планируемых подходов.
 * Дропсеты объединяют все записи в один элемент.
 */
public class PlannedSetAdapter extends RecyclerView.Adapter<PlannedSetAdapter.SetViewHolder> {

    private final List<PlannedSetResponse> sets = new ArrayList<>();
    private OnSetActionListener listener;

    public interface OnSetActionListener {
        void onEditSet(PlannedSetResponse set);
        void onDeleteSet(PlannedSetResponse set);
    }

    public void setOnSetActionListener(OnSetActionListener listener) {
        this.listener = listener;
    }

    public void setSets(List<PlannedSetResponse> sets) {
        this.sets.clear();
        if (sets != null) {
            this.sets.addAll(sets);
        }
        notifyDataSetChanged();
    }

    public List<PlannedSetResponse> getSets() {
        return sets;
    }

    @NonNull
    @Override
    public SetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_planned_set, parent, false);
        return new SetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SetViewHolder holder, int position) {
        holder.bind(sets.get(position), position);
    }

    @Override
    public int getItemCount() {
        return sets.size();
    }

    class SetViewHolder extends RecyclerView.ViewHolder {
        private final TextView setNumberText;
        private final TextView setTypeBadge;
        private final TextView weightText;
        private final TextView repsText;
        private final TextView timeText;
        private final TextView distanceText;
        private final TextView restTimeText;
        private final LinearLayout dropsetEntriesLayout;
        private final ImageButton editSetButton;
        private final ImageButton deleteSetButton;

        SetViewHolder(@NonNull View itemView) {
            super(itemView);
            setNumberText = itemView.findViewById(R.id.setNumberText);
            setTypeBadge = itemView.findViewById(R.id.setTypeBadge);
            weightText = itemView.findViewById(R.id.weightText);
            repsText = itemView.findViewById(R.id.repsText);
            timeText = itemView.findViewById(R.id.timeText);
            distanceText = itemView.findViewById(R.id.distanceText);
            restTimeText = itemView.findViewById(R.id.restTimeText);
            dropsetEntriesLayout = itemView.findViewById(R.id.dropsetEntriesLayout);
            editSetButton = itemView.findViewById(R.id.editSetButton);
            deleteSetButton = itemView.findViewById(R.id.deleteSetButton);
        }

        void bind(PlannedSetResponse set, int position) {
            Context ctx = itemView.getContext();

            // Номер подхода
            Integer setNum = set.getSetNumber();
            setNumberText.setText(ctx.getString(R.string.set_number, setNum != null ? setNum : position + 1));

            // Тип подхода
            String setType = set.getSetType() != null ? set.getSetType() : "REGULAR";
            boolean isDropset = "DROPSET".equalsIgnoreCase(setType);
            if (isDropset) {
                setTypeBadge.setText(R.string.dropset_label);
                setTypeBadge.setBackgroundColor(ctx.getResources().getColor(android.R.color.holo_orange_dark, null));
            } else {
                setTypeBadge.setText(R.string.regular_label);
                setTypeBadge.setBackgroundColor(ctx.getResources().getColor(android.R.color.darker_gray, null));
            }
            setTypeBadge.setVisibility(View.VISIBLE);

            // Параметры для обычного подхода
            weightText.setVisibility(isDropset ? View.GONE : View.VISIBLE);
            repsText.setVisibility(isDropset ? View.GONE : View.VISIBLE);
            timeText.setVisibility(isDropset ? View.GONE : View.VISIBLE);
            distanceText.setVisibility(isDropset ? View.GONE : View.VISIBLE);
            restTimeText.setVisibility(isDropset ? View.GONE : View.VISIBLE);

            if (!isDropset) {
                if (set.getTargetWeight() != null) {
                    weightText.setText(String.format(Locale.getDefault(), "%.1f кг", set.getTargetWeight()));
                    weightText.setVisibility(View.VISIBLE);
                } else {
                    weightText.setVisibility(View.GONE);
                }
                if (set.getTargetReps() != null) {
                    repsText.setText(String.format(Locale.getDefault(), "%d повт.", set.getTargetReps()));
                    repsText.setVisibility(View.VISIBLE);
                } else {
                    repsText.setVisibility(View.GONE);
                }
                if (set.getTargetTime() != null) {
                    int sec = set.getTargetTime();
                    timeText.setText(String.format(Locale.getDefault(), "%02d:%02d", sec / 60, sec % 60));
                    timeText.setVisibility(View.VISIBLE);
                } else {
                    timeText.setVisibility(View.GONE);
                }
                if (set.getTargetDistance() != null) {
                    double km = set.getTargetDistance() / 1000.0;
                    distanceText.setText(String.format(Locale.getDefault(), "%.2f км", km));
                    distanceText.setVisibility(View.VISIBLE);
                } else {
                    distanceText.setVisibility(View.GONE);
                }
                // Время отдыха
                if (set.getRestTime() != null) {
                    int restSec = set.getRestTime();
                    restTimeText.setText(String.format(Locale.getDefault(), "Отдых: %02d:%02d", restSec / 60, restSec % 60));
                    restTimeText.setVisibility(View.VISIBLE);
                } else {
                    restTimeText.setVisibility(View.GONE);
                }
            }

            // Дропсет записи — объединены в один элемент
            if (isDropset && set.getDropsetEntries() != null && !set.getDropsetEntries().isEmpty()) {
                dropsetEntriesLayout.setVisibility(View.VISIBLE);
                dropsetEntriesLayout.removeAllViews();

                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < set.getDropsetEntries().size(); i++) {
                    PlannedSetResponse.DropsetEntry entry = set.getDropsetEntries().get(i);
                    if (i > 0) sb.append("  →  ");
                    sb.append(String.format(Locale.getDefault(), "%.1f кг × %d", entry.getWeight(), entry.getReps()));
                }

                TextView dropsetSummary = new TextView(ctx);
                dropsetSummary.setText(sb.toString());
                dropsetSummary.setTextSize(13);
                dropsetSummary.setTextColor(ctx.getResources().getColor(android.R.color.holo_orange_dark, null));
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.topMargin = 4;
                dropsetSummary.setLayoutParams(params);
                dropsetEntriesLayout.addView(dropsetSummary);
            } else {
                dropsetEntriesLayout.setVisibility(View.GONE);
            }

            // Кнопки
            editSetButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditSet(set);
                }
            });

            deleteSetButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteSet(set);
                }
            });
        }
    }
}
