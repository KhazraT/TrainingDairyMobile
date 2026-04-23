package ru.squidory.trainingdairymobile.ui.trainings;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ru.squidory.trainingdairymobile.R;
import ru.squidory.trainingdairymobile.data.model.SessionHistoryResponse;

/**
 * Адаптер для отображения сессий внутри дня.
 */
public class SessionSessionAdapter extends RecyclerView.Adapter<SessionSessionAdapter.SessionViewHolder> {

    private final List<SessionHistoryResponse.SessionInHistory> sessions = new ArrayList<>();
    private SessionHistoryAdapter.OnSessionClickListener clickListener;

    public void setSessions(List<SessionHistoryResponse.SessionInHistory> sessions) {
        this.sessions.clear();
        if (sessions != null) {
            this.sessions.addAll(sessions);
        }
        notifyDataSetChanged();
    }

    public void setOnSessionClickListener(SessionHistoryAdapter.OnSessionClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public SessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history_session, parent, false);
        return new SessionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SessionViewHolder holder, int position) {
        holder.bind(sessions.get(position));
    }

    @Override
    public int getItemCount() {
        return sessions.size();
    }

    class SessionViewHolder extends RecyclerView.ViewHolder {
        private final TextView workoutNameText;
        private final TextView programNameText;
        private final TextView timeText;
        private final TextView durationText;
        private final TextView tonnageText;
        private final TextView setsCountText;
        private final TextView exercisesText;

        SessionViewHolder(@NonNull View itemView) {
            super(itemView);
            workoutNameText = itemView.findViewById(R.id.workoutNameText);
            programNameText = itemView.findViewById(R.id.programNameText);
            timeText = itemView.findViewById(R.id.timeText);
            durationText = itemView.findViewById(R.id.durationText);
            tonnageText = itemView.findViewById(R.id.tonnageText);
            setsCountText = itemView.findViewById(R.id.setsCountText);
            exercisesText = itemView.findViewById(R.id.exercisesText);
        }

        void bind(SessionHistoryResponse.SessionInHistory session) {
            workoutNameText.setText(session.getWorkoutName() != null ? session.getWorkoutName() : "Без названия");
            programNameText.setText(session.getProgramName() != null ? session.getProgramName() : "");

            // Форматируем время
            timeText.setText(formatTime(session.getStartedAt()));
            durationText.setText(session.getDurationMinutes() != null ? session.getDurationMinutes() + " мин" : "0 мин");

            // Тоннаж
            Double tonnage = session.getTotalTonnage();
            if (tonnage != null && tonnage > 0) {
                tonnageText.setText("Тоннаж: " + String.format(Locale.getDefault(), "%.1f", tonnage / 1000) + " т");
            } else {
                tonnageText.setText("Тоннаж: 0 т");
            }

            // Подходы
            Integer totalSets = session.getTotalSets();
            setsCountText.setText(totalSets + " подходов");

            // Упражнения
            if (session.getExercises() != null && !session.getExercises().isEmpty()) {
                StringBuilder exercises = new StringBuilder();
                for (int i = 0; i < session.getExercises().size(); i++) {
                    if (i > 0) {
                        exercises.append(", ");
                    }
                    exercises.append(session.getExercises().get(i).getExerciseName());
                    if (i >= 2) {
                        exercises.append("...");
                        break;
                    }
                }
                exercisesText.setText(exercises);
            } else {
                exercisesText.setText("");
            }

            // Клик
            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onSessionClick(session.getSessionId(), session.getWorkoutId());
                }
            });
        }

        private String formatTime(String timeStr) {
            if (timeStr == null) {
                return "";
            }
            try {
                // Try ISO 8601 format first
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                return outputFormat.format(inputFormat.parse(timeStr));
            } catch (Exception e) {
                // Try simple format
                try {
                    SimpleDateFormat simpleFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                    return simpleFormat.format(simpleFormat.parse(timeStr));
                } catch (Exception ex) {
                    return timeStr.length() > 5 ? timeStr.substring(0, 5) : timeStr;
                }
            }
        }
    }
}
