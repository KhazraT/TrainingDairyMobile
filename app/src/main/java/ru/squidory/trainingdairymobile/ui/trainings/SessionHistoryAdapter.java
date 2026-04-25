package ru.squidory.trainingdairymobile.ui.trainings;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import ru.squidory.trainingdairymobile.R;
import ru.squidory.trainingdairymobile.data.model.SessionHistoryResponse;

/**
 * Адаптер для отображения истории сессий по дням.
 * По умолчанию список строк свёрнут; при клике на дату — раскрывается/сворачивается.
 */
public class SessionHistoryAdapter extends RecyclerView.Adapter<SessionHistoryAdapter.DayViewHolder> {

    private final List<SessionHistoryResponse.DaySessions> daySessionsList = new ArrayList<>();
    private final Set<Integer> expandedPositions = new HashSet<>();
    private OnSessionClickListener sessionClickListener;

    public interface OnSessionClickListener {
        void onSessionClick(Long sessionId, Long workoutId);
    }

    public void setOnSessionClickListener(OnSessionClickListener listener) {
        this.sessionClickListener = listener;
    }

    public void setDaySessionsList(List<SessionHistoryResponse.DaySessions> daySessionsList) {
        this.daySessionsList.clear();
        this.expandedPositions.clear();
        if (daySessionsList != null) {
            // Копируем и реверсируем: сверху новые даты
            List<SessionHistoryResponse.DaySessions> reversed = new ArrayList<>(daySessionsList);
            Collections.reverse(reversed);
            this.daySessionsList.addAll(reversed);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history_day, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        boolean isExpanded = expandedPositions.contains(position);
        holder.bind(daySessionsList.get(position), isExpanded, position);
    }

    @Override
    public int getItemCount() {
        return daySessionsList.size();
    }

    class DayViewHolder extends RecyclerView.ViewHolder {
        private final LinearLayout dayHeader;
        private final TextView dateText;
        private final TextView sessionsCountText;
        private final ImageView expandIcon;
        private final RecyclerView sessionsRecyclerView;
        private SessionSessionAdapter sessionAdapter;

        DayViewHolder(@NonNull View itemView) {
            super(itemView);
            dayHeader = itemView.findViewById(R.id.dayHeader);
            dateText = itemView.findViewById(R.id.dateText);
            sessionsCountText = itemView.findViewById(R.id.sessionsCountText);
            expandIcon = itemView.findViewById(R.id.expandIcon);
            sessionsRecyclerView = itemView.findViewById(R.id.sessionsRecyclerView);

            sessionAdapter = new SessionSessionAdapter();
            sessionsRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            sessionsRecyclerView.setAdapter(sessionAdapter);
        }

        void bind(SessionHistoryResponse.DaySessions daySessions, boolean isExpanded, int position) {
            dateText.setText(formatDate(daySessions.getDate()));
            sessionsCountText.setText(getSessionsCountText(daySessions.getSessions().size()));
            sessionAdapter.setSessions(daySessions.getSessions());
            sessionAdapter.setOnSessionClickListener(sessionClickListener);

            if (isExpanded) {
                sessionsRecyclerView.setVisibility(View.VISIBLE);
                expandIcon.setImageResource(R.drawable.ic_expand_less);
            } else {
                sessionsRecyclerView.setVisibility(View.GONE);
                expandIcon.setImageResource(R.drawable.ic_expand_more);
            }

            dayHeader.setOnClickListener(v -> {
                if (expandedPositions.contains(position)) {
                    expandedPositions.remove(position);
                    sessionsRecyclerView.setVisibility(View.GONE);
                    expandIcon.setImageResource(R.drawable.ic_expand_more);
                } else {
                    expandedPositions.add(position);
                    sessionsRecyclerView.setVisibility(View.VISIBLE);
                    expandIcon.setImageResource(R.drawable.ic_expand_less);
                }
            });
        }

        private String formatDate(String dateStr) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMMM yyyy", new Locale("ru"));
                return inputFormat.parse(dateStr) != null
                        ? outputFormat.format(inputFormat.parse(dateStr)).toLowerCase()
                        : dateStr;
            } catch (Exception e) {
                return dateStr;
            }
        }

        private String getSessionsCountText(int count) {
            if (count == 1) return "1 тренировка";
            if (count >= 2 && count <= 4) return count + " тренировки";
            return count + " тренировок";
        }
    }
}
