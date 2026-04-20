package ru.squidory.trainingdairymobile.ui.trainings;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ru.squidory.trainingdairymobile.R;
import ru.squidory.trainingdairymobile.data.model.ExerciseResponse;
import ru.squidory.trainingdairymobile.data.model.PlannedSetResponse;
import ru.squidory.trainingdairymobile.data.model.WorkoutExerciseResponse;
import ru.squidory.trainingdairymobile.ui.exercises.ExerciseDetailActivity;

/**
 * Адаптер для отображения содержимого тренировки — упражнения с их планируемыми подходами.
 */
public class WorkoutContentAdapter extends RecyclerView.Adapter<WorkoutContentAdapter.ViewHolder> {

    private final List<WorkoutExerciseResponse> exercises = new ArrayList<>();
    private Map<Long, ExerciseResponse> exerciseMap;
    private Map<Long, List<PlannedSetResponse>> setsByExerciseId;
    private Context context;

    public void setContext(Context ctx) {
        this.context = ctx;
    }

    public void setExerciseMap(Map<Long, ExerciseResponse> map) {
        this.exerciseMap = map;
    }

    public void setSetsByExerciseId(Map<Long, List<PlannedSetResponse>> map) {
        this.setsByExerciseId = map;
    }

    public void setExercises(List<WorkoutExerciseResponse> exercises) {
        this.exercises.clear();
        if (exercises != null) {
            List<WorkoutExerciseResponse> sorted = new ArrayList<>(exercises);
            sorted.sort((a, b) -> {
                int oa = a.getExerciseOrder() != null ? a.getExerciseOrder() : 0;
                int ob = b.getExerciseOrder() != null ? b.getExerciseOrder() : 0;
                return Integer.compare(oa, ob);
            });
            this.exercises.addAll(sorted);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workout_content_exercise, parent, false);
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
        private final TextView exerciseNameText;
        private final LinearLayout setsContainer;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            exerciseNameText = itemView.findViewById(R.id.exerciseNameText);
            setsContainer = itemView.findViewById(R.id.setsContainer);
        }

        void bind(WorkoutExerciseResponse ex) {
            setsContainer.removeAllViews();

            // Проверяем суперсет
            Integer group = ex.getSupersetGroupNumber();
            String name = getExerciseName(ex);

            if (group != null && group > 0) {
                exerciseNameText.setText(name + " (Суперсет #" + group + ")");
                exerciseNameText.setBackgroundColor(0xFFFF9800); // Оранжевый для суперсетов
            } else {
                exerciseNameText.setText(name);
                exerciseNameText.setBackgroundColor(0xFF6200EE); // Фиолетовый для обычных
            }

            // Клик — открыть детали упражнения
            final long exerciseId = ex.getExerciseId();
            exerciseNameText.setOnClickListener(v -> {
                if (context != null) {
                    Intent intent = new Intent(context, ExerciseDetailActivity.class);
                    intent.putExtra(ExerciseDetailActivity.EXTRA_EXERCISE_ID, exerciseId);
                    intent.putExtra(ExerciseDetailActivity.EXTRA_EXERCISE_NAME, name);
                    context.startActivity(intent);
                }
            });

            // Подходы
            List<PlannedSetResponse> sets = setsByExerciseId != null ?
                    setsByExerciseId.get(ex.getId()) : null;

            if (sets != null && !sets.isEmpty()) {
                sets.sort((a, b) -> {
                    Integer numA = a.getSetNumber();
                    Integer numB = b.getSetNumber();
                    if (numA == null) numA = 0;
                    if (numB == null) numB = 0;
                    return numA.compareTo(numB);
                });

                for (PlannedSetResponse set : sets) {
                    String setText = formatSet(set);
                    TextView setView = new TextView(itemView.getContext());
                    setView.setText(setText);
                    setView.setTextSize(13);
                    setView.setTextColor(0xFF9E9E9E); // Серый для текста подходов
                    setView.setPadding(24, 4, 16, 4);
                    setsContainer.addView(setView);
                }
            } else {
                TextView noSets = new TextView(itemView.getContext());
                noSets.setText("Подходы не запланированы");
                noSets.setTextSize(12);
                noSets.setTextColor(0xFF9E9E9E); // Серый
                noSets.setPadding(24, 4, 16, 4);
                setsContainer.addView(noSets);
            }
        }

        private String getExerciseName(WorkoutExerciseResponse ex) {
            if (ex.getExercise() != null && ex.getExercise().getName() != null) {
                return ex.getExercise().getName();
            }
            if (exerciseMap != null) {
                ExerciseResponse full = exerciseMap.get(ex.getExerciseId());
                if (full != null && full.getName() != null) {
                    return full.getName();
                }
            }
            return "Упражнение";
        }

        private String formatSet(PlannedSetResponse set) {
            StringBuilder sb = new StringBuilder();
            int setNum = set.getSetNumber() != null ? set.getSetNumber() : 1;
            sb.append("Подход ").append(setNum);

            if ("DROPSET".equalsIgnoreCase(set.getSetType())) {
                sb.append(" [Дропсет]");
                // 1. Добавляем основной подход как первую ступень
                if (set.getTargetWeight() != null && set.getTargetReps() != null) {
                    sb.append(String.format(Locale.getDefault(), " %.1f кг × %d повт.",
                            set.getTargetWeight(), set.getTargetReps()));
                }

                // 2. Добавляем записи дропсета
                if (set.getDropsetEntries() != null) {
                    for (PlannedSetResponse.DropsetEntry entry : set.getDropsetEntries()) {
                        sb.append(" → ").append(String.format(Locale.getDefault(), "%.1f кг × %d повт.",
                                entry.getWeight(), entry.getReps()));
                    }
                }
            } else {
                if (set.getTargetWeight() != null) {
                    sb.append(String.format(Locale.getDefault(), " — %.1f кг", set.getTargetWeight()));
                }
                if (set.getTargetReps() != null) {
                    sb.append(String.format(Locale.getDefault(), " × %d повт.", set.getTargetReps()));
                }
                if (set.getTargetTime() != null) {
                    int sec = set.getTargetTime();
                    sb.append(String.format(Locale.getDefault(), " × %02d:%02d", sec / 60, sec % 60));
                }
                if (set.getTargetDistance() != null) {
                    double km = set.getTargetDistance() / 1000.0;
                    sb.append(String.format(Locale.getDefault(), " / %.2f км", km));
                }
            }

            // Время отдыха
            if (set.getRestTime() != null) {
                int restSec = set.getRestTime();
                sb.append(String.format(Locale.getDefault(), " | Отдых: %02d:%02d", restSec / 60, restSec % 60));
            }

            return sb.toString();
        }
    }
}
