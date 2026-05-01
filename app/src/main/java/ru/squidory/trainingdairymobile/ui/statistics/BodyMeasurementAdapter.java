package ru.squidory.trainingdairymobile.ui.statistics;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ru.squidory.trainingdairymobile.R;
import ru.squidory.trainingdairymobile.data.model.BodyMeasurementResponse;

/**
 * Адаптер для отображения измерений тела.
 */
public class BodyMeasurementAdapter extends RecyclerView.Adapter<BodyMeasurementAdapter.ViewHolder> {

    private List<BodyMeasurementResponse> measurements = new ArrayList<>();
    private OnMeasurementClickListener listener;

    private final SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
    private final SimpleDateFormat outputFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

    public interface OnMeasurementClickListener {
        void onMeasurementClick(BodyMeasurementResponse measurement);
        void onMeasurementLongClick(BodyMeasurementResponse measurement);
    }

    public void setOnMeasurementClickListener(OnMeasurementClickListener listener) {
        this.listener = listener;
    }

    public void setMeasurements(List<BodyMeasurementResponse> measurements) {
        this.measurements = measurements != null ? measurements : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_body_measurement, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BodyMeasurementResponse measurement = measurements.get(position);
        holder.bind(measurement);
    }

    @Override
    public int getItemCount() {
        return measurements.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView typeText;
        private final TextView valueText;
        private final TextView dateText;
        private final TextView notesText;

        ViewHolder(View itemView) {
            super(itemView);
            typeText = itemView.findViewById(R.id.measurementTypeText);
            valueText = itemView.findViewById(R.id.measurementValueText);
            dateText = itemView.findViewById(R.id.measurementDateText);
            notesText = itemView.findViewById(R.id.measurementNotesText);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onMeasurementClick(measurements.get(position));
                }
            });

            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onMeasurementLongClick(measurements.get(position));
                    return true;
                }
                return false;
            });
        }

        void bind(BodyMeasurementResponse measurement) {
            typeText.setText(getMeasurementTypeName(measurement.getMeasurementType()));
            valueText.setText(formatValue(measurement));
            dateText.setText(formatDate(measurement.getMeasuredAt()));

            // Комментарий отображаем на отдельной строке под всем
            if (measurement.getNotes() != null && !measurement.getNotes().isEmpty()) {
                notesText.setText(measurement.getNotes());
                notesText.setVisibility(View.VISIBLE);
            } else {
                notesText.setVisibility(View.GONE);
            }
        }

        private String getMeasurementTypeName(String type) {
            if (type == null) return "Неизвестно";
            switch (type) {
                case BodyMeasurementResponse.BODY_WEIGHT:
                    return "Вес тела";
                case BodyMeasurementResponse.BODY_FAT_PERCENTAGE:
                    return "% жира";
                case BodyMeasurementResponse.CHEST_CIRCUMFERENCE:
                    return "Обхват груди";
                case BodyMeasurementResponse.WAIST_CIRCUMFERENCE:
                    return "Обхват талии";
                case BodyMeasurementResponse.HIP_CIRCUMFERENCE:
                    return "Обхват бёдер";
                case BodyMeasurementResponse.ARM_CIRCUMFERENCE:
                    return "Обхват руки";
                case BodyMeasurementResponse.THIGH_CIRCUMFERENCE:
                    return "Обхват бедра";
                case BodyMeasurementResponse.CALF_CIRCUMFERENCE:
                    return "Обхват голени";
                case BodyMeasurementResponse.NECK_CIRCUMFERENCE:
                    return "Обхват шеи";
                case BodyMeasurementResponse.SHOULDER_CIRCUMFERENCE:
                    return "Обхват плеч";
                default:
                    return type;
            }
        }

        private String formatValue(BodyMeasurementResponse measurement) {
            String unit = measurement.getValueUnit() != null ? measurement.getValueUnit() : "";
            if (measurement.getValue() != null) {
                if ("KG".equals(unit)) {
                    return String.format(Locale.getDefault(), "%.1f кг", measurement.getValue());
                } else if ("PERCENT".equals(unit)) {
                    return String.format(Locale.getDefault(), "%.1f%%", measurement.getValue());
                } else {
                    return String.format(Locale.getDefault(), "%.1f см", measurement.getValue());
                }
            }
            return "-";
        }

        private String formatDate(String dateStr) {
            if (dateStr == null) return "-";
            try {
                Date date = inputFormat.parse(dateStr);
                return outputFormat.format(date);
            } catch (ParseException e) {
                return dateStr;
            }
        }
    }
}
