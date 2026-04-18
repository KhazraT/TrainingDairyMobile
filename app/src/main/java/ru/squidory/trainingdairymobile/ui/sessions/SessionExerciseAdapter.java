package ru.squidory.trainingdairymobile.ui.sessions;

import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

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
 * Поддерживает:
 * - Одиночные упражнения
 * - Суперсеты (группы упражнений с рамкой)
 * - Раскрытие/сворачивание
 * - Drag-and-drop на уровне групп
 */
public class SessionExerciseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPE_SINGLE = 0;
    public static final int TYPE_SUPERSET_GROUP = 1;

    private final List<DisplayItem> items = new ArrayList<>();
    private Map<Long, ExerciseResponse> exerciseMap;
    private OnExerciseExpandListener expandListener;
    private OnSetActionListener setActionListener;
    private OnExerciseDeleteListener deleteListener;
    private ItemTouchHelper itemTouchHelper;

    // Позиции раскрытых упражнений
    private final Set<Integer> expandedPositions = new HashSet<>();

    static class DisplayItem {
        final int type;
        final SessionExerciseResponse exercise;      // для SINGLE
        final Integer supersetGroup;                 // для SUPERSET_GROUP
        final List<SessionExerciseResponse> ssExercises;

        DisplayItem(int type, SessionExerciseResponse exercise, Integer supersetGroup,
                     List<SessionExerciseResponse> ssExercises) {
            this.type = type;
            this.exercise = exercise;
            this.supersetGroup = supersetGroup;
            this.ssExercises = ssExercises;
        }
    }

    public interface OnExerciseExpandListener {
        void onExpand(SessionExerciseResponse exercise, VideoView videoView);
        void onCollapse(VideoView videoView);
    }

    public interface OnSetActionListener {
        void onAddSet(SessionExerciseResponse exercise);
        void onDeleteSet(SessionExerciseResponse exercise, SessionSetResponse set);
        void onTimePickerClick(SessionExerciseResponse exercise, SessionSetResponse set, OnTimeSelectedCallback callback);
        void onDeleteSuperset(int supersetGroup);
    }

    public interface OnExerciseDeleteListener {
        void onDeleteExercise(SessionExerciseResponse exercise);
    }

    public interface OnTimeSelectedCallback {
        void onTimeSelected(int totalSeconds);
    }

    // Helper для работы с UI из ViewHolder
    private final android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());

    public void setOnExerciseExpandListener(OnExerciseExpandListener listener) {
        this.expandListener = listener;
    }

    public void setOnSetActionListener(OnSetActionListener listener) {
        this.setActionListener = listener;
    }

    public void setOnExerciseDeleteListener(OnExerciseDeleteListener listener) {
        this.deleteListener = listener;
    }

    public void setExerciseMap(Map<Long, ExerciseResponse> map) {
        this.exerciseMap = map;
    }

    public void setItemTouchHelper(ItemTouchHelper helper) {
        this.itemTouchHelper = helper;
    }

    /**
     * Переместить элемент на уровне групп.
     */
    public boolean moveDisplayItem(int fromPos, int toPos) {
        if (fromPos < 0 || fromPos >= items.size() || toPos < 0 || toPos >= items.size()) return false;
        if (fromPos == toPos) return false;

        DisplayItem moved = items.remove(fromPos);
        int insertPos = toPos;
        if (fromPos < toPos) insertPos--;
        insertPos = Math.max(0, Math.min(insertPos, items.size()));
        items.add(insertPos, moved);

        // Пересчитываем order
        int order = 1;
        for (DisplayItem item : items) {
            if (item.type == TYPE_SINGLE && item.exercise != null) {
                item.exercise.setExerciseOrder(order++);
            } else if (item.type == TYPE_SUPERSET_GROUP && item.ssExercises != null) {
                for (SessionExerciseResponse ex : item.ssExercises) {
                    ex.setExerciseOrder(order);
                    order++;
                }
            }
        }

        notifyItemMoved(fromPos, insertPos);
        return true;
    }

    /**
     * Получить текущий список упражнений.
     */
    public List<SessionExerciseResponse> getCurrentExercises() {
        List<SessionExerciseResponse> result = new ArrayList<>();
        for (DisplayItem item : items) {
            if (item.type == TYPE_SINGLE && item.exercise != null) {
                result.add(item.exercise);
            } else if (item.type == TYPE_SUPERSET_GROUP && item.ssExercises != null) {
                result.addAll(item.ssExercises);
            }
        }
        return result;
    }

    /**
     * Установить упражнения и сгруппировать по суперсетам.
     */
    public void setExercises(List<SessionExerciseResponse> exercises) {
        items.clear();
        if (exercises == null || exercises.isEmpty()) {
            notifyDataSetChanged();
            return;
        }

        List<SessionExerciseResponse> sorted = new ArrayList<>(exercises);
        sorted.sort((a, b) -> {
            int oa = a.getExerciseOrder() != null ? a.getExerciseOrder() : 0;
            int ob = b.getExerciseOrder() != null ? b.getExerciseOrder() : 0;
            return Integer.compare(oa, ob);
        });

        int i = 0;
        while (i < sorted.size()) {
            SessionExerciseResponse current = sorted.get(i);
            Integer group = current.getSupersetGroupNumber();

            if (group != null && group > 0) {
                List<SessionExerciseResponse> ssExercises = new ArrayList<>();
                while (i < sorted.size()) {
                    SessionExerciseResponse ex = sorted.get(i);
                    Integer exGroup = ex.getSupersetGroupNumber();
                    if (exGroup == null || !exGroup.equals(group)) break;
                    ssExercises.add(ex);
                    i++;
                }
                items.add(new DisplayItem(TYPE_SUPERSET_GROUP, null, group, ssExercises));
            } else {
                items.add(new DisplayItem(TYPE_SINGLE, current, null, null));
                i++;
            }
        }

        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_SUPERSET_GROUP) {
            return new SupersetGroupViewHolder(inflater.inflate(R.layout.item_superset_group, parent, false));
        } else {
            return new SingleViewHolder(inflater.inflate(R.layout.item_session_exercise, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DisplayItem item = items.get(position);
        if (holder instanceof SupersetGroupViewHolder) {
            ((SupersetGroupViewHolder) holder).bind(item.supersetGroup, item.ssExercises);
        } else if (holder instanceof SingleViewHolder) {
            ((SingleViewHolder) holder).bind(item.exercise, position);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class SingleViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView exerciseCard;
        private final LinearLayout exerciseHeader;
        private final ImageView dragHandle;
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

        SingleViewHolder(@NonNull View itemView) {
            super(itemView);
            exerciseCard = itemView.findViewById(R.id.exerciseCard);
            exerciseHeader = itemView.findViewById(R.id.exerciseHeader);
            dragHandle = itemView.findViewById(R.id.dragHandle);
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
                if (primaryMuscle == null) primaryMuscle = exercise.getTargetMuscles().get(0);
                targetMuscleChip.setText(primaryMuscle.getName());
                targetMusclesLayout.setVisibility(View.VISIBLE);
            }

            // Иконка раскрытия
            if (isExpanded) {
                expandIcon.setImageResource(R.drawable.ic_expand_less);
                expandIcon.setContentDescription("Свернуть");
                exerciseContentLayout.setVisibility(View.VISIBLE);
                if (exerciseVideoView != null && expandListener != null && exercise.getExerciseId() > 0) {
                    expandListener.onExpand(exercise, exerciseVideoView);
                }
            } else {
                expandIcon.setImageResource(R.drawable.ic_expand_more);
                expandIcon.setContentDescription("Раскрыть");
                exerciseContentLayout.setVisibility(View.GONE);
                if (exerciseVideoView != null && exerciseVideoView.isPlaying()) {
                    exerciseVideoView.stopPlayback();
                }
            }

            // RecyclerView подходов
            List<SessionSetResponse> completedSets = exercise.getCompletedSets();
            if (completedSets == null) completedSets = new ArrayList<>();

            setsAdapter = new SessionSetsAdapter(exercise);
            setsRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            setsRecyclerView.setAdapter(setsAdapter);
            setsRecyclerView.setNestedScrollingEnabled(false);

            addSetButton.setOnClickListener(v -> {
                if (setActionListener != null) setActionListener.onAddSet(exercise);
            });

            exerciseHeader.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;
                if (expandedPositions.contains(pos)) {
                    expandedPositions.remove(pos);
                    exerciseContentLayout.setVisibility(View.GONE);
                    expandIcon.setImageResource(R.drawable.ic_expand_more);
                    if (exerciseVideoView != null && exerciseVideoView.isPlaying()) {
                        exerciseVideoView.stopPlayback();
                    }
                    if (expandListener != null) expandListener.onCollapse(exerciseVideoView);
                } else {
                    expandedPositions.add(pos);
                    exerciseContentLayout.setVisibility(View.VISIBLE);
                    expandIcon.setImageResource(R.drawable.ic_expand_less);
                    if (expandListener != null) expandListener.onExpand(exercise, exerciseVideoView);
                }
            });

            exerciseInfoButton.setOnClickListener(v -> {
                long exerciseId = exercise.getExerciseId();
                if (exerciseId > 0) {
                    Intent intent = new Intent(itemView.getContext(), ExerciseDetailActivity.class);
                    intent.putExtra(ExerciseDetailActivity.EXTRA_EXERCISE_ID, exerciseId);
                    intent.putExtra(ExerciseDetailActivity.EXTRA_EXERCISE_NAME, name);
                    itemView.getContext().startActivity(intent);
                }
            });

            deleteExerciseButton.setOnClickListener(v -> {
                if (deleteListener != null) deleteListener.onDeleteExercise(exercise);
            });

            dragHandle.setOnTouchListener((v, event) -> {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN && itemTouchHelper != null) {
                    itemTouchHelper.startDrag(this);
                    return true;
                }
                return false;
            });
        }

        private String getExerciseName(SessionExerciseResponse ex) {
            if (ex.getExercise() != null && ex.getExercise().getName() != null) return ex.getExercise().getName();
            if (ex.getExerciseName() != null && !ex.getExerciseName().isEmpty()) return ex.getExerciseName();
            if (exerciseMap != null) {
                Long exId = ex.getExerciseId();
                if (exId != null && exId > 0) {
                    ExerciseResponse full = exerciseMap.get(exId);
                    if (full != null && full.getName() != null) return full.getName();
                }
            }
            return "Упражнение";
        }
    }

    class SupersetGroupViewHolder extends RecyclerView.ViewHolder {
        private final ImageView supersetDragHandle;
        private final TextView supersetTitle;
        private final ImageButton deleteSupersetButton;
        private final RecyclerView supersetExercisesRecyclerView;

        SupersetGroupViewHolder(@NonNull View itemView) {
            super(itemView);
            supersetDragHandle = itemView.findViewById(R.id.supersetDragHandle);
            supersetTitle = itemView.findViewById(R.id.supersetTitle);
            deleteSupersetButton = itemView.findViewById(R.id.deleteSupersetButton);
            supersetExercisesRecyclerView = itemView.findViewById(R.id.supersetExercisesRecyclerView);
        }

        void bind(Integer supersetGroup, List<SessionExerciseResponse> exercises) {
            supersetTitle.setText("Суперсет");
            deleteSupersetButton.setOnClickListener(v -> {
                if (supersetGroup != null && setActionListener != null) {
                    setActionListener.onDeleteSuperset(supersetGroup);
                }
            });

            // Drag handle для суперсета
            supersetDragHandle.setOnTouchListener((v, event) -> {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN && itemTouchHelper != null) {
                    itemTouchHelper.startDrag(this);
                    return true;
                }
                return false;
            });

            final SupersetSessionAdapter innerAdapter = new SupersetSessionAdapter(exercises);
            supersetExercisesRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            supersetExercisesRecyclerView.setAdapter(innerAdapter);
            supersetExercisesRecyclerView.setNestedScrollingEnabled(false);
            supersetExercisesRecyclerView.setHasFixedSize(true);

            // Drag-and-drop внутри суперсета
            ItemTouchHelper.Callback innerCallback = new ItemTouchHelper.SimpleCallback(
                    ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView,
                                      @NonNull RecyclerView.ViewHolder viewHolder,
                                      @NonNull RecyclerView.ViewHolder target) {
                    int from = viewHolder.getAdapterPosition();
                    int to = target.getAdapterPosition();
                    if (from >= 0 && from < exercises.size() && to >= 0 && to < exercises.size()) {
                        SessionExerciseResponse moved = exercises.remove(from);
                        exercises.add(to, moved);
                        innerAdapter.notifyItemMoved(from, to);
                        return true;
                    }
                    return false;
                }

                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {}

                @Override
                public boolean isLongPressDragEnabled() {
                    return true;
                }
            };
            new ItemTouchHelper(innerCallback).attachToRecyclerView(supersetExercisesRecyclerView);
        }
    }

    /**
     * Вложенный адаптер для упражнений внутри суперсета (сессия).
     */
    class SupersetSessionAdapter extends RecyclerView.Adapter<SupersetSessionAdapter.InnerViewHolder> {
        private final List<SessionExerciseResponse> exercises;

        SupersetSessionAdapter(List<SessionExerciseResponse> exercises) {
            this.exercises = exercises != null ? new ArrayList<>(exercises) : new ArrayList<>();
        }

        @NonNull
        @Override
        public InnerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_session_exercise, parent, false);
            // Убираем margin чтобы вложенные карточки выглядели紧凑
            return new InnerViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull InnerViewHolder holder, int position) {
            holder.bind(exercises.get(position));
        }

        @Override
        public int getItemCount() {
            return exercises.size();
        }

        class InnerViewHolder extends RecyclerView.ViewHolder {
            private final LinearLayout exerciseHeader;
            private final ImageView expandIcon;
            private final TextView exerciseNameTextView;
            private final LinearLayout exerciseContentLayout;
            private final VideoView exerciseVideoView;
            private final TextView setsLabel;
            private final RecyclerView setsRecyclerView;
            private final MaterialButton addSetButton;
            private SessionSetsAdapter setsAdapter;

            InnerViewHolder(@NonNull View itemView) {
                super(itemView);
                exerciseHeader = itemView.findViewById(R.id.exerciseHeader);
                expandIcon = itemView.findViewById(R.id.expandIcon);
                exerciseNameTextView = itemView.findViewById(R.id.exerciseNameTextView);
                exerciseContentLayout = itemView.findViewById(R.id.exerciseContentLayout);
                exerciseVideoView = itemView.findViewById(R.id.exerciseVideoView);
                setsLabel = itemView.findViewById(R.id.setsLabel);
                setsRecyclerView = itemView.findViewById(R.id.setsRecyclerView);
                addSetButton = itemView.findViewById(R.id.addSetButton);
                // Скрываем ненужные элементы внутри суперсета
                View dragHandle = itemView.findViewById(R.id.dragHandle);
                if (dragHandle != null) dragHandle.setVisibility(View.VISIBLE);
                View infoBtn = itemView.findViewById(R.id.exerciseInfoButton);
                if (infoBtn != null) infoBtn.setVisibility(View.VISIBLE); // Отображаем кнопку информации
                View delBtn = itemView.findViewById(R.id.deleteExerciseButton);
                if (delBtn != null) delBtn.setVisibility(View.GONE);
                View muscleChip = itemView.findViewById(R.id.targetMusclesLayout);
                if (muscleChip != null) muscleChip.setVisibility(View.GONE);
            }

            void bind(SessionExerciseResponse exercise) {
                exerciseNameTextView.setText(getExerciseName(exercise));

                // Кнопка "i" (информация об упражнении)
                View infoBtn = itemView.findViewById(R.id.exerciseInfoButton);
                if (infoBtn != null) {
                    infoBtn.setOnClickListener(v -> {
                        Intent intent = new Intent(itemView.getContext(), ExerciseDetailActivity.class);
                        intent.putExtra("exercise_id", exercise.getExerciseId());
                        itemView.getContext().startActivity(intent);
                    });
                }

                // Drag handle для перетаскивания внутри суперсета
                View dragHandle = itemView.findViewById(R.id.dragHandle);
                if (dragHandle != null) {
                    dragHandle.setOnTouchListener((v, event) -> {
                        if (event.getActionMasked() == MotionEvent.ACTION_DOWN && itemTouchHelper != null) {
                            itemTouchHelper.startDrag(this);
                            return true;
                        }
                        return false;
                    });
                }

                // ВОССТАНОВЛЕНИЕ СОСТОЯНИЯ:
                // Уникальный ID для этого упражнения в рамках сессии
                long exerciseId = exercise.getId();
                boolean isExpanded = expandedPositions.contains((int) exerciseId); // Используем ID, а не позицию

                if (isExpanded) {
                    exerciseContentLayout.setVisibility(View.VISIBLE);
                    expandIcon.setImageResource(R.drawable.ic_expand_less);
                    // ВОССТАНОВЛЕНИЕ ВИДЕО:
                    if (expandListener != null) {
                        expandListener.onExpand(exercise, exerciseVideoView);
                    }
                } else {
                    exerciseContentLayout.setVisibility(View.GONE);
                    expandIcon.setImageResource(R.drawable.ic_expand_more);
                }

                List<SessionSetResponse> completedSets = exercise.getCompletedSets();
                if (completedSets == null) completedSets = new ArrayList<>();

                setsAdapter = new SessionSetsAdapter(exercise);
                setsRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
                setsRecyclerView.setAdapter(setsAdapter);
                setsRecyclerView.setNestedScrollingEnabled(false);

                addSetButton.setOnClickListener(v -> {
                    if (setActionListener != null) setActionListener.onAddSet(exercise);
                });

                exerciseHeader.setOnClickListener(v -> {
                    if (exerciseContentLayout.getVisibility() == View.VISIBLE) {
                        exerciseContentLayout.setVisibility(View.GONE);
                        expandIcon.setImageResource(R.drawable.ic_expand_more);
                        expandedPositions.remove((int) exerciseId);
                        if (exerciseVideoView != null && exerciseVideoView.isPlaying()) {
                            exerciseVideoView.stopPlayback();
                        }
                    } else {
                        exerciseContentLayout.setVisibility(View.VISIBLE);
                        expandIcon.setImageResource(R.drawable.ic_expand_less);
                        expandedPositions.add((int) exerciseId);
                        if (expandListener != null) expandListener.onExpand(exercise, exerciseVideoView);
                    }
                });
            }

            private String getExerciseName(SessionExerciseResponse ex) {
                if (ex.getExercise() != null && ex.getExercise().getName() != null) return ex.getExercise().getName();
                if (ex.getExerciseName() != null && !ex.getExerciseName().isEmpty()) return ex.getExerciseName();
                return "Упражнение";
            }
        }
    }

    /**
     * Вложенный адаптер для подходов внутри упражнения.
     */
    class SessionSetsAdapter extends RecyclerView.Adapter<SessionSetsAdapter.SetViewHolder> {

        private SessionExerciseResponse exercise;
        private List<SessionSetResponse> sortedSets = new ArrayList<>();

        SessionSetsAdapter(SessionExerciseResponse exercise) {
            this.exercise = exercise;
            updateSortedSets();
        }

        void updateExercise(SessionExerciseResponse exercise) {
            this.exercise = exercise;
            updateSortedSets();
            // Используем notifyDataSetChanged только для вложенного адаптера подходов,
            // так как он не содержит раскрывающихся упражнений.
            notifyDataSetChanged();
        }

        private void updateSortedSets() {
            List<SessionSetResponse> sets = exercise != null ? exercise.getCompletedSets() : null;
            if (sets == null) {
                sortedSets = new ArrayList<>();
            } else {
                sortedSets = new ArrayList<>(sets);
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
            updateSortedSets();
            if (position >= sortedSets.size()) return;
            SessionSetResponse set = sortedSets.get(position);
            
            // Определяем, показывать ли отдых для этого подхода
            // Для дропсета: показываем отдых только после последнего подхода дропсета
            boolean showRestTime = shouldShowRestTime(set, position);
            
            holder.bind(set, exercise, position, showRestTime);
        }

        private boolean shouldShowRestTime(SessionSetResponse set, int position) {
            // Если это элемент отдыха - не показываем отдых
            if (set.isRest()) {
                return false;
            }
            
            // Для дропсета: только после последнего подхода дропсета
            if (set.isDropset() || set.isDropsetPart()) {
                // Ищем следующий подход в цепочке дропсета
                for (int i = position + 1; i < sortedSets.size(); i++) {
                    SessionSetResponse nextSet = sortedSets.get(i);
                    // Если следующий элемент - тоже часть дропсета, скрываем отдых
                    if (nextSet.isDropsetPart()) {
                        return false;
                    }
                    // Если следующий элемент - отдельный подход, скрываем отдых текущего
                    // (отдых будет показан после последнего подхода дропсета)
                    if (!nextSet.isDropset() && !nextSet.isDropsetPart() && !nextSet.isRest()) {
                        return false;
                    }
                    // Если следующий элемент - отдых после дропсета, скрываем отдых текущего
                    if (nextSet.isRest()) {
                        return false;
                    }
                }
                // Это последний подход в цепочке дропсета
                return true;
            }
            
            // Для обычного подхода - показываем отдых
            return true;
        }

        @Override
        public int getItemCount() {
            updateSortedSets();
            return sortedSets.size();
        }

        class SetViewHolder extends RecyclerView.ViewHolder {
            private final LinearLayout setItemRoot;
            private final TextView setNumberTextView;
            private final TextView setTypeLabel;
            private final LinearLayout weightColumn;
            private final TextInputLayout weightInputLayout;
            private final TextInputEditText weightInput;
            private final LinearLayout repsColumn;
            private final TextInputLayout repsInputLayout;
            private final TextInputEditText repsInput;
            private final LinearLayout timeColumn;
            private final TextInputLayout timeInputLayout;
            private final TextInputEditText timeInput;
            private final LinearLayout distanceColumn;
            private final TextInputLayout distanceInputLayout;
            private final TextInputEditText distanceInput;
            private final ImageButton deleteSetButton;

            // Отдых (на уровне подхода)
            private final LinearLayout restTimeRow;
            private final TextInputLayout restTimeInputLayout;
            private final TextInputEditText restTimeInput;
            private final MaterialButton startRestTimerButton;
            private boolean timerUsed = false;

            private SessionSetResponse currentSet;
            private TextWatcher weightWatcher;
            private TextWatcher repsWatcher;
            private TextWatcher distanceWatcher;

            SetViewHolder(@NonNull View itemView) {
                super(itemView);
                setItemRoot = itemView.findViewById(R.id.setItemRoot);
                setNumberTextView = itemView.findViewById(R.id.setNumberTextView);
                setTypeLabel = itemView.findViewById(R.id.setTypeLabel);
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

                restTimeRow = itemView.findViewById(R.id.restTimeRow);
                restTimeInputLayout = itemView.findViewById(R.id.restTimeInputLayout);
                restTimeInput = itemView.findViewById(R.id.restTimeInput);
                startRestTimerButton = itemView.findViewById(R.id.startRestTimerButton);

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

                deleteSetButton.setOnClickListener(v -> {
                    if (currentSet != null && setActionListener != null) {
                        setActionListener.onDeleteSet(exercise, currentSet);
                    }
                });
            }

            void bind(SessionSetResponse set, SessionExerciseResponse exercise, int sortedPosition, boolean showRestTime) {
                currentSet = set;
                setNumberTextView.setText(String.valueOf(sortedPosition + 1));

                // Определяем тип подхода для визуального отображения
                boolean isDropsetSet = set.isDropset();
                boolean isDropsetPart = set.isDropsetPart();
                boolean isRestItem = set.isRest();

                // Дропсет подходы и их части - оранжевый фон
                if (isDropsetSet || isDropsetPart) {
                    setItemRoot.setBackgroundColor(0x10FF9800);
                    setTypeLabel.setVisibility(View.VISIBLE);
                    setTypeLabel.setText("дропсет");
                    setNumberTextView.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFF9800));
                } else {
                    setItemRoot.setBackgroundColor(0x00000000);
                    setTypeLabel.setVisibility(View.GONE);
                    setNumberTextView.setBackgroundTintList(null);
                }

                // Для элемента отдыха - особое отображение
                if (isRestItem) {
                    setItemRoot.setBackgroundColor(0x2000FF00);
                    setTypeLabel.setVisibility(View.VISIBLE);
                    setTypeLabel.setText("отдых");
                    setTypeLabel.setTextColor(0xFF00C853);
                    setNumberTextView.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF00C853));
                    
                    // Скрываем поля ввода
                    weightColumn.setVisibility(View.GONE);
                    repsColumn.setVisibility(View.GONE);
                    timeColumn.setVisibility(View.GONE);
                    distanceColumn.setVisibility(View.GONE);
                    deleteSetButton.setVisibility(View.GONE);
                    
                    // Показываем отдых
                    restTimeRow.setVisibility(View.VISIBLE);
                    Integer restTime = set.getRestTime();
                    if (restTime != null && restTime > 0) {
                        restTimeInput.setText(String.format("%02d:%02d", restTime / 60, restTime % 60));
                    } else {
                        restTimeInput.setText("00:00");
                    }
                    restTimeInput.setEnabled(false);
                    startRestTimerButton.setVisibility(View.VISIBLE);
                    startRestTimerButton.setEnabled(true);
                    startRestTimerButton.setText("▶");
                    startRestTimerButton.setAlpha(1.0f);
                    
                    timerUsed = false;
                    startRestTimerButton.setOnClickListener(v -> {
                        if (timerUsed) return;
                        Integer rt = set.getRestTime();
                        if (rt == null || rt <= 0) {
                            Toast.makeText(itemView.getContext(), "Укажите время отдыха", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        timerUsed = true;
                        startRestTimerButton.setEnabled(false);
                        startRestTimerButton.setText("⏳");
                        startRestTimerButton.setAlpha(0.5f);

                        RestTimerDialog timerDialog = new RestTimerDialog(itemView.getContext(), rt);
                        timerDialog.setOnFinishListener(() -> mainHandler.post(() -> startRestTimerButton.setText("✓")));
                        timerDialog.show();
                    });
                    return;
                }

                // Сбрасываем цвета для обычного подхода
                setTypeLabel.setTextColor(0xFFFF9800);
                restTimeInput.setEnabled(true);

                String exerciseType = exercise.getExerciseType();
                if ((exerciseType == null || exerciseType.isEmpty()) && exerciseMap != null) {
                    Long exId = exercise.getExerciseId();
                    if (exId != null && exId > 0 && exerciseMap.containsKey(exId)) {
                        String mapType = exerciseMap.get(exId).getExerciseType();
                        if (mapType != null && !mapType.isEmpty()) exerciseType = mapType;
                    }
                }
                if (exerciseType == null || exerciseType.isEmpty()) exerciseType = "REPS_WEIGHT";

                boolean isRepsWeight = "REPS_WEIGHT".equalsIgnoreCase(exerciseType);
                boolean isTimeWeight = "TIME_WEIGHT".equalsIgnoreCase(exerciseType);
                boolean isTimeDistance = "TIME_DISTANCE".equalsIgnoreCase(exerciseType);
                boolean isTimeWeightDistance = "TIME_WEIGHT_DISTANCE".equalsIgnoreCase(exerciseType);

                weightColumn.setVisibility(isRepsWeight || isTimeWeight || isTimeWeightDistance ? View.VISIBLE : View.GONE);
                repsColumn.setVisibility(isRepsWeight ? View.VISIBLE : View.GONE);
                timeColumn.setVisibility(isTimeWeight || isTimeDistance || isTimeWeightDistance ? View.VISIBLE : View.GONE);
                distanceColumn.setVisibility(isTimeDistance || isTimeWeightDistance ? View.VISIBLE : View.GONE);
                deleteSetButton.setVisibility(View.VISIBLE);

                weightInput.setText(set.getWeight() != null ? String.valueOf(set.getWeight()) : "");
                repsInput.setText(set.getReps() != null ? String.valueOf(set.getReps()) : "");

                if (set.getDurationSeconds() != null) {
                    int sec = set.getDurationSeconds();
                    timeInput.setText(String.format("%02d:%02d", sec / 60, sec % 60));
                } else {
                    timeInput.setText("");
                }

                if (set.getDistanceMeters() != null) {
                    double km = set.getDistanceMeters() / 1000.0;
                    distanceInput.setText(String.valueOf(km));
                } else {
                    distanceInput.setText("");
                }

                timeInput.setFocusable(false);
                timeInput.setClickable(true);
                final SessionSetResponse[] setRef = new SessionSetResponse[]{set};
                timeInput.setOnClickListener(v -> {
                    if (setActionListener != null) {
                        int currentSec = set.getDurationSeconds() != null ? set.getDurationSeconds() : 0;
                        setActionListener.onTimePickerClick(exercise, setRef[0], seconds -> {
                            set.setDurationSeconds(seconds);
                            timeInput.setText(String.format("%02d:%02d", seconds / 60, seconds % 60));
                        });
                    }
                });

                distanceInput.setOnFocusChangeListener((v, hasFocus) -> {
                    if (!hasFocus) {
                        String val = distanceInput.getText() != null ? distanceInput.getText().toString().trim() : "";
                        if (!val.isEmpty()) {
                            try {
                                double km = Double.parseDouble(val);
                                set.setDistanceMeters(km * 1000);
                            } catch (NumberFormatException e) {}
                        }
                    }
                });

                // Отдых — показываем только для обычных подходов или последнего подхода дропсета
                if (showRestTime) {
                    restTimeRow.setVisibility(View.VISIBLE);
                    Integer restTime = set.getRestTime();
                    if (restTime != null && restTime > 0) {
                        restTimeInput.setText(String.format("%02d:%02d", restTime / 60, restTime % 60));
                    } else {
                        restTimeInput.setText("");
                    }

                    timerUsed = false;
                    startRestTimerButton.setEnabled(true);
                    startRestTimerButton.setText("▶");
                    startRestTimerButton.setAlpha(1.0f);
                    
                    // Если это дропсет - кнопка оранжевая
                    if (isDropsetSet || isDropsetPart) {
                        startRestTimerButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFF9800));
                    } else {
                        startRestTimerButton.setBackgroundTintList(null);
                    }

                    restTimeInput.setOnClickListener(v -> showRestTimePickerForSet(set));

                    startRestTimerButton.setOnClickListener(v -> {
                        if (timerUsed) return;
                        Integer rt = set.getRestTime();
                        if (rt == null || rt <= 0) {
                            Toast.makeText(itemView.getContext(), "Укажите время отдыха", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        timerUsed = true;
                        startRestTimerButton.setEnabled(false);
                        startRestTimerButton.setText("⏳");
                        startRestTimerButton.setAlpha(0.5f);

                        RestTimerDialog timerDialog = new RestTimerDialog(itemView.getContext(), rt);
                        timerDialog.setOnFinishListener(() -> mainHandler.post(() -> startRestTimerButton.setText("✓")));
                        timerDialog.show();
                    });
                } else {
                    restTimeRow.setVisibility(View.GONE);
                }
            }

            private void showRestTimePickerForSet(SessionSetResponse set) {
                View dialogView = LayoutInflater.from(itemView.getContext())
                        .inflate(R.layout.dialog_time_picker, null);
                android.widget.NumberPicker minutesPicker = dialogView.findViewById(R.id.minutesPicker);
                android.widget.NumberPicker secondsPicker = dialogView.findViewById(R.id.secondsPicker);
                minutesPicker.setMinValue(0); minutesPicker.setMaxValue(59);
                secondsPicker.setMinValue(0); secondsPicker.setMaxValue(59);
                int currentRest = set.getRestTime() != null ? set.getRestTime() : 0;
                minutesPicker.setValue(currentRest / 60);
                secondsPicker.setValue(currentRest % 60);

                new android.app.AlertDialog.Builder(itemView.getContext())
                        .setTitle("Время отдыха")
                        .setView(dialogView)
                        .setPositiveButton("OK", (d, w) -> {
                            int newRest = minutesPicker.getValue() * 60 + secondsPicker.getValue();
                            set.setRestTime(newRest);
                            restTimeInput.setText(String.format("%02d:%02d", newRest / 60, newRest % 60));
                        })
                        .setNegativeButton("Отмена", null)
                        .show();
            }
        }
    }
}
