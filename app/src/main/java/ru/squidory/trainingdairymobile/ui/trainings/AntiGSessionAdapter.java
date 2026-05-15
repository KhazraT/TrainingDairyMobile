package ru.squidory.trainingdairymobile.ui.trainings;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import ru.squidory.trainingdairymobile.R;
import ru.squidory.trainingdairymobile.data.model.AntiGSessionResponse;

/**
 * Adapter for displaying anti-G sessions in a RecyclerView.
 */
public class AntiGSessionAdapter extends RecyclerView.Adapter<AntiGSessionAdapter.AntiGSessionViewHolder> {

    private List<AntiGSessionResponse> sessions;
    private OnSessionClickListener listener;
    private OnDeleteClickListener deleteListener;

    public interface OnSessionClickListener {
        void onSessionClick(AntiGSessionResponse session);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(AntiGSessionResponse session);
    }

    public AntiGSessionAdapter(List<AntiGSessionResponse> sessions,
                               OnSessionClickListener listener,
                               OnDeleteClickListener deleteListener) {
        this.sessions = sessions;
        this.listener = listener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public AntiGSessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_anti_g_session, parent, false);
        return new AntiGSessionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AntiGSessionViewHolder holder, int position) {
        AntiGSessionResponse session = sessions.get(position);
        holder.bind(session);
    }

    @Override
    public int getItemCount() {
        return sessions == null ? 0 : sessions.size();
    }

    public void setSessions(List<AntiGSessionResponse> sessions) {
        this.sessions = sessions;
        notifyDataSetChanged();
    }

    class AntiGSessionViewHolder extends RecyclerView.ViewHolder {
        private TextView dateTextView;
        private TextView amountTextView;
        private TextView commentTextView;
        private Button deleteButton;

        AntiGSessionViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            amountTextView = itemView.findViewById(R.id.amountTextView);
            commentTextView = itemView.findViewById(R.id.commentTextView);
            deleteButton = itemView.findViewById(R.id.deleteButton);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onSessionClick(sessions.get(position));
                }
            });

            deleteButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && deleteListener != null) {
                    deleteListener.onDeleteClick(sessions.get(position));
                }
            });
        }

        void bind(AntiGSessionResponse session) {
            // Format date (OffsetDateTime to dd.MM.yyyy HH:mm)
            String dateString = session.getDate()
                    .atZoneSameInstant(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
            dateTextView.setText(dateString);

            amountTextView.setText(String.format(Locale.getDefault(), "%d циклов", session.getAmount()));

            String comment = session.getComment();
            if (comment == null || comment.isEmpty()) {
                commentTextView.setText("Без комментария");
                commentTextView.setTextColor(itemView.getContext().getColor(android.R.color.darker_gray));
            } else {
                commentTextView.setText(comment);
                commentTextView.setTextColor(itemView.getContext().getColor(android.R.color.primary_text_light));
            }
        }
    }
}