package ru.squidory.trainingdairymobile.ui.trainings;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ru.squidory.trainingdairymobile.R;
import ru.squidory.trainingdairymobile.data.model.SessionHistoryResponse;

/**
 * Адаптер для отображения истории сессий по дням.
 */
public class SessionHistoryAdapter extends RecyclerView.Adapter<SessionHistoryAdapter.DayViewHolder> {

    private final List<SessionHistoryResponse.DaySessions> daySessionsList = new ArrayList<>();
    private OnSessionClickListener sessionClickListener;

    public interface OnSessionClickListener {
        void onSessionClick(Long sessionId, Long workoutId);
    }

    public void setOnSessionClickListener(OnSessionClickListener listener) {
        this.sessionClickListener = listener;
    }

    public void setDaySessionsList(List<SessionHistoryResponse.DaySessions> daySessionsList) {
        this.daySessionsList.clear();
        if (daySessionsList != null) {
            this.daySessionsList.addAll(daySessionsList);
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
        holder.bind(daySessionsList.get(position));
    }

    @Override
    public int getItemCount() {
        return daySessionsList.size();
    }

    class DayViewHolder extends RecyclerView.ViewHolder {
        private final TextView dateText;
        private final TextView sessionsCountText;
        private final RecyclerView sessionsRecyclerView;
        private SessionSessionAdapter sessionAdapter;

        DayViewHolder(@NonNull View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.dateText);
            sessionsCountText = itemView.findViewById(R.id.sessionsCountText);
            sessionsRecyclerView = itemView.findViewById(R.id.sessionsRecyclerView);

            sessionAdapter = new SessionSessionAdapter();
            sessionsRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            sessionsRecyclerView.setAdapter(sessionAdapter);
        }

        void bind(SessionHistoryResponse.DaySessions daySessions) {
            // Форматируем дату
            dateText.setText(formatDate(daySessions.getDate()));
            sessionsCountText.setText(getSessionsCountText(daySessions.getSessions().size()));
            sessionAdapter.setSessions(daySessions.getSessions());
            sessionAdapter.setOnSessionClickListener(sessionClickListener);
        }

        private String formatDate(String dateStr) {
            try {
                // Parse ISO 8601 date
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
            if (count == 1) {
                return "1 тренировка";
            } else if (count >= 2 && count <= 4) {
                return count + " тренировки";
            } else {
                return count + " тренировок";
            }
        }
    }
}
