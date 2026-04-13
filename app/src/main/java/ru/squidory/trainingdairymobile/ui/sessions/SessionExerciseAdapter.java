package ru.squidory.trainingdairymobile.ui.sessions;

import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.squidory.trainingdairymobile.R;
import ru.squidory.trainingdairymobile.data.model.ExerciseResponse;
import ru.squidory.trainingdairymobile.data.model.MuscleGroupResponse;
import ru.squidory.trainingdairymobile.data.model.SessionExerciseResponse;
import ru.squidory.trainingdairymobile.data.model.SessionSetResponse;
import ru.squidory.trainingdairymobile.ui.exercises.ExerciseDetailActivity;

/**
 * Адаптер для отображения упражнений в сессии.
 * Поддерживает раскрытие/сворачивание карточки упражнения.
 * При раскрытии: видео + RecyclerView подходов.
 */
public class SessionExerciseAdapter extends RecyclerView.Adapter<SessionExerciseAdapter.ViewHolder> {

    private final List<SessionExerciseResponse> exercises = new ArrayList<>();
    private Map<Long, ExerciseResponse> exerciseMap;
    private OnExerciseExpandListener expandListener;
    private OnSetActionListener setActionListener;

    // Позиции раскрытых упражнений
    private final Set<Integer> expandedPositions = new HashSet<>();

    public interface OnExerciseExpandListener {
        void onExpand(SessionExerciseResponse exercise, VideoView videoView);
        void onCollapse(VideoView videoView);
    }

    public interface OnSetActionListener {
        void onAddSet(SessionExerciseResponse exercise);
        void onDeleteSet(SessionExerciseResponse exercise, SessionSetResponse set);
        void onTimePickerClick(SessionExerciseResponse exercise, SessionSetResponse set, OnTimeSelectedCallback callback);
    }

    public interface OnExerciseDeleteListener {
        void onDeleteExercise(SessionExerciseResponse exercise);
    }

    private OnExerciseDeleteListener deleteListener;

    public void setOnExerciseDeleteListener(OnExerciseDeleteListener listener) {
        this.deleteListener = listener;
    }

    public interface OnTimeSelectedCallback {
        void onTimeSelected(int totalSeconds);
    }

    public void setOnExerciseExpandListener(OnExerciseExpandListener listener) {
        this.expandListener = listener;
    }

    public void setOnSetActionListener(OnSetActionListener listener) {
        this.setActionListener = listener;
    }

    public void setExerciseMap(Map<Long, ExerciseResponse> map) {
        this.exerciseMap = map;
    }

    /**
     * Принудительно прочитать все значения из видимых ViewHolder'ов
     * и записать в объекты. Вызывается перед валидацией/отправкой.
     */
    public void forceSyncVisibleSets() {
        RecyclerView rv = null; // нет доступа здесь, но TextWatchers уже синхронизировали
        // TextWatchers уже пишут в currentSet в реальном времени,
        // так что объекты всегда актуальны. Этот метод — заглушка на будущее.
    }

    public void setExercises(List<SessionExerciseResponse> exercises) {
        // НЕ очищаем expandedPositions — сохраняем состояние раскрытия
        this.exercises.clear();
        if (exercises != null) {
            List<SessionExerciseResponse> sorted = new ArrayList<>(exercises);
            sorted.sort((a, b) -> {
                int oa = a.getExerciseOrder() != null ? a.getExerciseOrder() : 1;
                int ob = b.getExerciseOrder() != null ? b.getExerciseOrder() : 1;
                return Integer.compare(oa, ob);
            });
            this.exercises.addAll(sorted);
        }
        // Сортируем подходы внутри каждого упражнения по setNumber
        for (SessionExerciseResponse ex : this.exercises) {
            List<SessionSetResponse> sets = ex.getCompletedSets();
            if (sets != null) {
                sets.sort((a, b) -> {
                    int oa = a.getSetNumber() != null ? a.getSetNumber() : 0;
                    int ob = b.getSetNumber() != null ? b.getSetNumber() : 0;
                    return Integer.compare(oa, ob);
                });
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_session_exercise, parent, false);
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

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        // Останавливаем видео при утилизации ViewHolder
        if (holder.exerciseVideoView != null && holder.exerciseVideoView.isPlaying()) {
            holder.exerciseVideoView.stopPlayback();
        }
        holder.exerciseVideoView.setVisibility(View.GONE);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView exerciseCard;
        private final LinearLayout exerciseHeader;
        private final ImageView expandIcon;
        private final TextView exerciseNameTextView;
        private final LinearLayout targetMusclesLayout;
        private final Chip targetMuscleChip;
        private final ImageButton exerciseInfoButton;
        private final ImageButton deleteExerciseButton;
        private final LinearLayout exerciseContentLayout;
        private final VideoView exerciseVideoView;
        private final TextView setsLabel;
        private final RecyclerView setsRecyclerView;
        private final MaterialButton addSetButton;

        private SessionSetsAdapter setsAdapter;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            exerciseCard = itemView.findViewById(R.id.exerciseCard);
            exerciseHeader = itemView.findViewById(R.id.exerciseHeader);
            expandIcon = itemView.findViewById(R.id.expandIcon);
            exerciseNameTextView = itemView.findViewById(R.id.exerciseNameTextView);
            targetMusclesLayout = itemView.findViewById(R.id.targetMusclesLayout);
            targetMuscleChip = itemView.findViewById(R.id.targetMuscleChip);
            exerciseInfoButton = itemView.findViewById(R.id.exerciseInfoButton);
            deleteExerciseButton = itemView.findViewById(R.id.deleteExerciseButton);
            exerciseContentLayout = itemView.findViewById(R.id.exerciseContentLayout);
            exerciseVideoView = itemView.findViewById(R.id.exerciseVideoView);
            setsLabel = itemView.findViewById(R.id.setsLabel);
            setsRecyclerView = itemView.findViewById(R.id.setsRecyclerView);
            addSetButton = itemView.findViewById(R.id.addSetButton);
        }

        void bind(SessionExerciseResponse exercise, int position) {
            boolean isExpanded = expandedPositions.contains(position);

            // Название упражнения
            String name = getExerciseName(exercise);
            exerciseNameTextView.setText(name);

            // Целевые мышцы
            targetMusclesLayout.setVisibility(View.GONE);
            if (exercise.getTargetMuscles() != null && !exercise.getTargetMuscles().isEmpty()) {
                MuscleGroupResponse primaryMuscle = null;
                for (MuscleGroupResponse muscle : exercise.getTargetMuscles()) {
                    if (muscle.getIsPrimary() != null && muscle.getIsPrimary()) {
                        primaryMuscle = muscle;
                        break;
                    }
                }
                if (primaryMuscle == null) {
                    primaryMuscle = exercise.getTargetMuscles().get(0);
                }
                targetMuscleChip.setText(primaryMuscle.getName());
                targetMusclesLayout.setVisibility(View.VISIBLE);
            }

            // Иконка раскрытия/сворачивания
            if (isExpanded) {
                expandIcon.setImageResource(R.drawable.ic_expand_less);
                expandIcon.setContentDescription("Свернуть");
                exerciseContentLayout.setVisibility(View.VISIBLE);
                // Восстанавливаем видео при перерисовке (после add/delete set)
                if (exerciseVideoView != null && expandListener != null && exercise.getExerciseId() > 0) {
                    expandListener.onExpand(exercise, exerciseVideoView);
                }
            } else {
                expandIcon.setImageResource(R.drawable.ic_expand_more);
                expandIcon.setContentDescription("Раскрыть");
                exerciseContentLayout.setVisibility(View.GONE);
                // Останавливаем видео при сворачивании
                if (exerciseVideoView != null && exerciseVideoView.isPlaying()) {
                    exerciseVideoView.stopPlayback();
                }
            }

            // Настройка RecyclerView подходов — ВСЕГДА создаём новый адаптер
            List<SessionSetResponse> completedSets = exercise.getCompletedSets();
            if (completedSets == null) {
                completedSets = new ArrayList<>();
            }

            setsAdapter = new SessionSetsAdapter(exercise);
            setsRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            setsRecyclerView.setAdapter(setsAdapter);
            setsRecyclerView.setNestedScrollingEnabled(false);

            // Кнопка добавления подхода
            addSetButton.setOnClickListener(v -> {
                if (setActionListener != null) {
                    setActionListener.onAddSet(exercise);
                }
            });

            // Клик по заголовку — раскрыть/свернуть
            exerciseHeader.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;

                if (expandedPositions.contains(pos)) {
                    // Свернуть
                    expandedPositions.remove(pos);
                    exerciseContentLayout.setVisibility(View.GONE);
                    expandIcon.setImageResource(R.drawable.ic_expand_more);
                    expandIcon.setContentDescription("Раскрыть");
                    if (exerciseVideoView != null && exerciseVideoView.isPlaying()) {
                        exerciseVideoView.stopPlayback();
                    }
                    if (expandListener != null) {
                        expandListener.onCollapse(exerciseVideoView);
                    }
                    // НЕ вызываем notifyItemChanged — views уже обновлены напрямую
                } else {
                    // Раскрыть
                    expandedPositions.add(pos);
                    exerciseContentLayout.setVisibility(View.VISIBLE);
                    expandIcon.setImageResource(R.drawable.ic_expand_less);
                    expandIcon.setContentDescription("Свернуть");
                    if (expandListener != null) {
                        expandListener.onExpand(exercise, exerciseVideoView);
                    }
                    // НЕ вызываем notifyItemChanged — views уже обновлены напрямую
                }
            });

            // Кнопка информации — открыть детали упражнения
            exerciseInfoButton.setOnClickListener(v -> {
                long exerciseId = exercise.getExerciseId();
                if (exerciseId > 0) {
                    Intent intent = new Intent(itemView.getContext(), ExerciseDetailActivity.class);
                    intent.putExtra(ExerciseDetailActivity.EXTRA_EXERCISE_ID, exerciseId);
                    intent.putExtra(ExerciseDetailActivity.EXTRA_EXERCISE_NAME, name);
                    itemView.getContext().startActivity(intent);
                }
            });

            // Кнопка удаления — удалить упражнение
            deleteExerciseButton.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDeleteExercise(exercise);
                }
            });
        }

        private String getExerciseName(SessionExerciseResponse ex) {
            if (ex.getExercise() != null && ex.getExercise().getName() != null) {
                return ex.getExercise().getName();
            }
            if (ex.getExerciseName() != null && !ex.getExerciseName().isEmpty()) {
                return ex.getExerciseName();
            }
            if (exerciseMap != null) {
                Long exId = ex.getExerciseId();
                if (exId != null && exId > 0) {
                    ExerciseResponse full = exerciseMap.get(exId);
                    if (full != null && full.getName() != null) {
                        return full.getName();
                    }
                }
            }
            return "Упражнение";
        }
    }

    /**
     * Вложенный адаптер для подходов внутри упражнения.
     */
    class SessionSetsAdapter extends RecyclerView.Adapter<SessionSetsAdapter.SetViewHolder> {

        private SessionExerciseResponse exercise;
        // Кэшированный отсортированный список
        private List<SessionSetResponse> sortedSets = new ArrayList<>();

        SessionSetsAdapter(SessionExerciseResponse exercise) {
            this.exercise = exercise;
            updateSortedSets();
        }

        void updateExercise(SessionExerciseResponse exercise) {
            this.exercise = exercise;
            updateSortedSets();
            notifyDataSetChanged();
        }

        private void updateSortedSets() {
            List<SessionSetResponse> sets = exercise != null ? exercise.getCompletedSets() : null;
            if (sets == null) {
                sortedSets = new ArrayList<>();
            } else {
                sortedSets = new ArrayList<>(sets);
                // Сортируем по setNumber (НЕ setOrder!) — setNumber это порядок от бэкенда
                sortedSets.sort((a, b) -> {
                    int oa = a.getSetNumber() != null ? a.getSetNumber() : 0;
                    int ob = b.getSetNumber() != null ? b.getSetNumber() : 0;
                    return Integer.compare(oa, ob);
                });
            }
        }

        @NonNull
        @Override
        public SetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_session_set, parent, false);
            return new SetViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SetViewHolder holder, int position) {
            // Обновляем отсортированный список каждый раз
            updateSortedSets();
            if (position >= sortedSets.size()) return;

            SessionSetResponse set = sortedSets.get(position);
            holder.bind(set, exercise, position);
        }

        @Override
        public int getItemCount() {
            updateSortedSets();
            return sortedSets.size();
        }

        class SetViewHolder extends RecyclerView.ViewHolder {
            private final TextView setNumberTextView;
            private final LinearLayout weightColumn;
            private final com.google.android.material.textfield.TextInputLayout weightInputLayout;
            private final com.google.android.material.textfield.TextInputEditText weightInput;
            private final LinearLayout repsColumn;
            private final com.google.android.material.textfield.TextInputLayout repsInputLayout;
            private final com.google.android.material.textfield.TextInputEditText repsInput;
            private final LinearLayout timeColumn;
            private final com.google.android.material.textfield.TextInputLayout timeInputLayout;
            private final com.google.android.material.textfield.TextInputEditText timeInput;
            private final LinearLayout distanceColumn;
            private final com.google.android.material.textfield.TextInputLayout distanceInputLayout;
            private final com.google.android.material.textfield.TextInputEditText distanceInput;
            private final ImageButton deleteSetButton;

            // Дропсет секция
            private final LinearLayout dropsetSection;
            private final com.google.android.material.textfield.TextInputLayout dropsetWeightInputLayout;
            private final com.google.android.material.textfield.TextInputEditText dropsetWeightInput;
            private final com.google.android.material.textfield.TextInputLayout dropsetRepsInputLayout;
            private final com.google.android.material.textfield.TextInputEditText dropsetRepsInput;

            // Текущий подход и TextWatchers (устанавливаются один раз)
            private SessionSetResponse currentSet;
            private TextWatcher weightWatcher;
            private TextWatcher repsWatcher;
            private TextWatcher distanceWatcher;

            SetViewHolder(@NonNull View itemView) {
                super(itemView);
                setNumberTextView = itemView.findViewById(R.id.setNumberTextView);
                weightColumn = itemView.findViewById(R.id.weightColumn);
                weightInputLayout = itemView.findViewById(R.id.weightInputLayout);
                weightInput = itemView.findViewById(R.id.weightInput);
                repsColumn = itemView.findViewById(R.id.repsColumn);
                repsInputLayout = itemView.findViewById(R.id.repsInputLayout);
                repsInput = itemView.findViewById(R.id.repsInput);
                timeColumn = itemView.findViewById(R.id.timeColumn);
                timeInputLayout = itemView.findViewById(R.id.timeInputLayout);
                timeInput = itemView.findViewById(R.id.timeInput);
                distanceColumn = itemView.findViewById(R.id.distanceColumn);
                distanceInputLayout = itemView.findViewById(R.id.distanceInputLayout);
                distanceInput = itemView.findViewById(R.id.distanceInput);
                deleteSetButton = itemView.findViewById(R.id.deleteSetButton);

                dropsetSection = itemView.findViewById(R.id.dropsetSection);
                dropsetWeightInputLayout = itemView.findViewById(R.id.dropsetWeightInputLayout);
                dropsetWeightInput = itemView.findViewById(R.id.dropsetWeightInput);
                dropsetRepsInputLayout = itemView.findViewById(R.id.dropsetRepsInputLayout);
                dropsetRepsInput = itemView.findViewById(R.id.dropsetRepsInput);

                // TextWatchers — устанавливаются ОДИН РАЗ
                weightWatcher = new TextWatcher() {
                    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                    @Override public void afterTextChanged(Editable s) {
                        if (currentSet == null) return;
                        String val = s.toString().trim();
                        if (!val.isEmpty()) {
                            try { currentSet.setWeight(Double.parseDouble(val)); } catch (NumberFormatException e) { currentSet.setWeight(null); }
                        } else { currentSet.setWeight(null); }
                    }
                };
                weightInput.addTextChangedListener(weightWatcher);

                repsWatcher = new TextWatcher() {
                    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                    @Override public void afterTextChanged(Editable s) {
                        if (currentSet == null) return;
                        String val = s.toString().trim();
                        if (!val.isEmpty()) {
                            try { currentSet.setReps(Integer.parseInt(val)); } catch (NumberFormatException e) { currentSet.setReps(null); }
                        } else { currentSet.setReps(null); }
                    }
                };
                repsInput.addTextChangedListener(repsWatcher);

                distanceWatcher = new TextWatcher() {
                    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                    @Override public void afterTextChanged(Editable s) {
                        if (currentSet == null) return;
                        String val = s.toString().trim();
                        if (!val.isEmpty()) {
                            try { currentSet.setDistanceMeters(Double.parseDouble(val) * 1000); } catch (NumberFormatException e) { currentSet.setDistanceMeters(null); }
                        } else { currentSet.setDistanceMeters(null); }
                    }
                };
                distanceInput.addTextChangedListener(distanceWatcher);

                // Кнопка удаления
                deleteSetButton.setOnClickListener(v -> {
                    if (currentSet != null && setActionListener != null) {
                        setActionListener.onDeleteSet(exercise, currentSet);
                    }
                });
            }

            void bind(SessionSetResponse set, SessionExerciseResponse exercise, int sortedPosition) {
                currentSet = set;
                setNumberTextView.setText(String.valueOf(sortedPosition + 1));

                // Берём тип упражнения из exerciseMap если есть, иначе из самого exercise
                String exerciseType = exercise.getExerciseType();
                if ((exerciseType == null || exerciseType.isEmpty()) && exerciseMap != null) {
                    Long exId = exercise.getExerciseId();
                    if (exId != null && exId > 0 && exerciseMap.containsKey(exId)) {
                        String mapType = exerciseMap.get(exId).getExerciseType();
                        if (mapType != null && !mapType.isEmpty()) {
                            exerciseType = mapType;
                        }
                    }
                }
                // Fallback только если всё ещё null
                if (exerciseType == null || exerciseType.isEmpty()) {
                    exerciseType = "REPS_WEIGHT";
                }

                boolean isRepsWeight = "REPS_WEIGHT".equalsIgnoreCase(exerciseType);
                boolean isTimeWeight = "TIME_WEIGHT".equalsIgnoreCase(exerciseType);
                boolean isTimeDistance = "TIME_DISTANCE".equalsIgnoreCase(exerciseType);
                boolean isTimeWeightDistance = "TIME_WEIGHT_DISTANCE".equalsIgnoreCase(exerciseType);

                weightColumn.setVisibility(isRepsWeight || isTimeWeight || isTimeWeightDistance ? View.VISIBLE : View.GONE);
                repsColumn.setVisibility(isRepsWeight ? View.VISIBLE : View.GONE);
                timeColumn.setVisibility(isTimeWeight || isTimeDistance || isTimeWeightDistance ? View.VISIBLE : View.GONE);
                distanceColumn.setVisibility(isTimeDistance || isTimeWeightDistance ? View.VISIBLE : View.GONE);

                weightInput.setText(set.getWeight() != null ? String.valueOf(set.getWeight()) : "");
                repsInput.setText(set.getReps() != null ? String.valueOf(set.getReps()) : "");

                // Время: секунды → мм:сс
                if (set.getDurationSeconds() != null) {
                    int sec = set.getDurationSeconds();
                    int min = sec / 60;
                    int s = sec % 60;
                    timeInput.setText(String.format("%02d:%02d", min, s));
                } else {
                    timeInput.setText("");
                }

                // Дистанция: метры → км
                if (set.getDistanceMeters() != null) {
                    double km = set.getDistanceMeters() / 1000.0;
                    distanceInput.setText(String.valueOf(km));
                } else {
                    distanceInput.setText("");
                }

                // Поле времени — NumberPicker по клику
                timeInput.setFocusable(false);
                timeInput.setClickable(true);
                final SessionSetResponse[] setRef = new SessionSetResponse[]{set};
                timeInput.setOnClickListener(v -> {
                    if (setActionListener != null) {
                        int currentSec = set.getDurationSeconds() != null ? set.getDurationSeconds() : 0;
                        final int[] resultSec = {currentSec};
                        setActionListener.onTimePickerClick(exercise, setRef[0], seconds -> {
                            resultSec[0] = seconds;
                            set.setDurationSeconds(seconds);
                            int min = seconds / 60;
                            int sec = seconds % 60;
                            timeInput.setText(String.format("%02d:%02d", min, sec));
                        });
                    }
                });

                // Конвертация дистанции при потере фокуса (км → метры)
                distanceInput.setOnFocusChangeListener((v, hasFocus) -> {
                    if (!hasFocus) {
                        String val = distanceInput.getText() != null ? distanceInput.getText().toString().trim() : "";
                        if (!val.isEmpty()) {
                            try {
                                double km = Double.parseDouble(val);
                                set.setDistanceMeters(km * 1000);
                            } catch (NumberFormatException e) {
                                // Оставляем как есть
                            }
                        }
                    }
                });

                boolean hasDropset = set.isDropset() || set.getDropsetWeight() != null || set.getDropsetReps() != null;
                dropsetSection.setVisibility(hasDropset ? View.VISIBLE : View.GONE);
                if (hasDropset) {
                    dropsetSection.setBackgroundColor(0x1AFF9800);
                    dropsetWeightInput.setText(set.getDropsetWeight() != null ? String.valueOf(set.getDropsetWeight()) : "");
                    dropsetRepsInput.setText(set.getDropsetReps() != null ? String.valueOf(set.getDropsetReps()) : "");
                } else {
                    dropsetSection.setBackgroundColor(0x00000000);
                }
            }
        }
    }
}
