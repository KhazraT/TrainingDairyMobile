package ru.squidory.trainingdairymobile.ui.trainings;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ru.squidory.trainingdairymobile.R;
import ru.squidory.trainingdairymobile.data.model.ExerciseResponse;
import ru.squidory.trainingdairymobile.data.model.MuscleGroupResponse;
import ru.squidory.trainingdairymobile.data.model.WorkoutExerciseResponse;

/**
 * Адаптер упражнений в тренировке.
 *
 * Два типа элементов:
 * - TYPE_SINGLE: одно упражнение
 * - TYPE_SUPERSET_GROUP: суперсет с вложенным RecyclerView для упражнений
 *
 * Drag-and-drop:
 * - Внешний: перемещает одиночные упражнения и целые суперсеты
 * - Внутренний: перемещает упражнения внутри суперсета
 */
public class WorkoutExerciseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPE_SINGLE = 0;
    public static final int TYPE_SUPERSET_GROUP = 1;

    private final List<DisplayItem> items = new ArrayList<>();
    private OnExerciseActionListener listener;
    private Map<Long, ExerciseResponse> exerciseMap;

    /** Callback для DnD внутри суперсета. */
    public interface OnSupersetExerciseMoved {
        void onExerciseMovedWithinSuperset(int supersetGroup, int fromOrder, int toOrder);
    }
    private OnSupersetExerciseMoved supersetMoveListener;

    public void setOnSupersetExerciseMoved(OnSupersetExerciseMoved l) { this.supersetMoveListener = l; }

    static class DisplayItem {
        final int type;
        final WorkoutExerciseResponse exercise;      // для SINGLE
        final Integer supersetGroup;                 // для SUPERSET_GROUP
        final List<WorkoutExerciseResponse> ssExercises;

        DisplayItem(int type, WorkoutExerciseResponse exercise, Integer supersetGroup,
                     List<WorkoutExerciseResponse> ssExercises) {
            this.type = type;
            this.exercise = exercise;
            this.supersetGroup = supersetGroup;
            this.ssExercises = ssExercises;
        }
    }

    public interface OnExerciseActionListener {
        void onManageSets(WorkoutExerciseResponse exercise);
        void onDeleteExercise(WorkoutExerciseResponse exercise);
        void onEditExercise(WorkoutExerciseResponse exercise);
        void onDeleteSuperset(int supersetGroup);
    }

    public void setOnExerciseActionListener(OnExerciseActionListener listener) {
        this.listener = listener;
    }

    public void setExerciseMap(Map<Long, ExerciseResponse> map) {
        this.exerciseMap = map;
        notifyDataSetChanged();
    }

    public void setExercises(List<WorkoutExerciseResponse> exercises) {
        items.clear();
        if (exercises == null || exercises.isEmpty()) {
            notifyDataSetChanged();
            return;
        }

        // Сортируем ТОЛЬКО по exerciseOrder — supersetGroupNumber определяет
        // принадлежность к суперсету, но порядок задаётся exerciseOrder
        List<WorkoutExerciseResponse> sorted = new ArrayList<>(exercises);
        sorted.sort((a, b) -> {
            int oa = a.getExerciseOrder() != null ? a.getExerciseOrder() : 0;
            int ob = b.getExerciseOrder() != null ? b.getExerciseOrder() : 0;
            return Integer.compare(oa, ob);
        });

        int i = 0;
        while (i < sorted.size()) {
            WorkoutExerciseResponse current = sorted.get(i);
            Integer group = current.getSupersetGroupNumber();

            if (group != null && group > 0) {
                List<WorkoutExerciseResponse> ssExercises = new ArrayList<>();
                while (i < sorted.size()) {
                    WorkoutExerciseResponse ex = sorted.get(i);
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

    public List<WorkoutExerciseResponse> getAllExercises() {
        List<WorkoutExerciseResponse> result = new ArrayList<>();
        for (DisplayItem item : items) {
            if (item.type == TYPE_SINGLE && item.exercise != null) {
                result.add(item.exercise);
            } else if (item.type == TYPE_SUPERSET_GROUP && item.ssExercises != null) {
                result.addAll(item.ssExercises);
            }
        }
        return result;
    }

    public List<Long> getExistingExerciseIds() {
        List<Long> ids = new ArrayList<>();
        for (WorkoutExerciseResponse ex : getAllExercises()) {
            ids.add(ex.getExerciseId());
        }
        return ids;
    }

    public Integer getSupersetGroupAtPosition(int position) {
        if (position >= 0 && position < items.size()) {
            return items.get(position).supersetGroup;
        }
        return null;
    }

    public int getViewTypeAt(int position) {
        if (position >= 0 && position < items.size()) return items.get(position).type;
        return -1;
    }

    public List<WorkoutExerciseResponse> getSupersetExercises(int supersetGroup) {
        for (DisplayItem item : items) {
            if (item.type == TYPE_SUPERSET_GROUP && item.supersetGroup != null
                    && item.supersetGroup.equals(supersetGroup)) {
                return item.ssExercises;
            }
        }
        return new ArrayList<>();
    }

    /**
     * Переместить элемент на уровне групп (суперсет = один блок).
     * Пересчитывает order так чтобы упражнения одного суперсета всегда шли подряд.
     */
    public void moveDisplayItem(int fromPos, int toPos) {
        if (fromPos < 0 || fromPos >= items.size() || toPos < 0 || toPos >= items.size()) return;
        if (fromPos == toPos) return;

        DisplayItem moved = items.remove(fromPos);
        int insertPos = toPos;
        if (fromPos < toPos) insertPos--;
        insertPos = Math.max(0, Math.min(insertPos, items.size()));
        items.add(insertPos, moved);

        // Пересчитываем order — все упражнения подряд, суперсеты не разрываются
        int order = 1;
        for (DisplayItem item : items) {
            if (item.type == TYPE_SINGLE && item.exercise != null) {
                item.exercise.setExerciseOrder(order++);
                android.util.Log.d("WExerciseAdapter", "  SINGLE id=" + item.exercise.getId() + " → order=" + (order - 1));
            } else if (item.type == TYPE_SUPERSET_GROUP && item.ssExercises != null) {
                for (WorkoutExerciseResponse ex : item.ssExercises) {
                    ex.setExerciseOrder(order);
                    android.util.Log.d("WExerciseAdapter", "  SUPERSET id=" + ex.getId() + " (group=" + ex.getSupersetGroupNumber() + ") → order=" + order);
                    order++;
                }
            }
        }
        android.util.Log.d("WExerciseAdapter", "moveDisplayItem: from=" + fromPos + " to=" + toPos + ", total items=" + items.size() + ", maxOrder=" + (order-1));
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
            return new SingleViewHolder(inflater.inflate(R.layout.item_workout_exercise, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DisplayItem item = items.get(position);
        if (holder instanceof SupersetGroupViewHolder) {
            ((SupersetGroupViewHolder) holder).bind(item.supersetGroup, item.ssExercises, listener, exerciseMap, supersetMoveListener);
        } else if (holder instanceof SingleViewHolder) {
            ((SingleViewHolder) holder).bind(item.exercise, listener, exerciseMap);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ==================== ViewHolders ====================

    static class SingleViewHolder extends RecyclerView.ViewHolder {
        private final TextView exerciseNameText;
        private final TextView musclesText;
        private final TextView equipmentText;
        private final TextView exerciseSetsText;
        private final ImageButton manageSetsButton;
        private final ImageButton deleteExerciseButton;

        SingleViewHolder(@NonNull View itemView) {
            super(itemView);
            exerciseNameText = itemView.findViewById(R.id.exerciseNameText);
            musclesText = itemView.findViewById(R.id.musclesText);
            equipmentText = itemView.findViewById(R.id.equipmentText);
            exerciseSetsText = itemView.findViewById(R.id.exerciseSetsText);
            manageSetsButton = itemView.findViewById(R.id.manageSetsButton);
            deleteExerciseButton = itemView.findViewById(R.id.deleteExerciseButton);
        }

        void bind(WorkoutExerciseResponse exercise, OnExerciseActionListener listener, Map<Long, ExerciseResponse> map) {
            bindData(exercise, map);
            itemView.setOnClickListener(v -> { if (listener != null) listener.onEditExercise(exercise); });
            manageSetsButton.setOnClickListener(v -> { if (listener != null) listener.onManageSets(exercise); });
            deleteExerciseButton.setOnClickListener(v -> { if (listener != null) listener.onDeleteExercise(exercise); });
        }

        private void bindData(WorkoutExerciseResponse exercise, Map<Long, ExerciseResponse> map) {
            ExerciseResponse full = (exercise.getExercise() != null) ? exercise.getExercise() :
                    (map != null ? map.get(exercise.getExerciseId()) : null);
            exerciseNameText.setText(full != null ? full.getName() : "Упражнение");
            bindMuscles(full, musclesText);
            bindEquipment(full, equipmentText);
            exerciseSetsText.setText("Подходов: " + (exercise.getSetsCount() != null ? exercise.getSetsCount() : 0));
        }
    }

    static class SupersetGroupViewHolder extends RecyclerView.ViewHolder {
        private final TextView supersetTitle;
        private final ImageButton deleteSupersetButton;
        private final RecyclerView supersetExercisesRecyclerView;

        SupersetGroupViewHolder(@NonNull View itemView) {
            super(itemView);
            supersetTitle = itemView.findViewById(R.id.supersetTitle);
            deleteSupersetButton = itemView.findViewById(R.id.deleteSupersetButton);
            supersetExercisesRecyclerView = itemView.findViewById(R.id.supersetExercisesRecyclerView);
        }

        void bind(Integer supersetGroup, List<WorkoutExerciseResponse> exercises,
                  OnExerciseActionListener listener, Map<Long, ExerciseResponse> map,
                  OnSupersetExerciseMoved movedListener) {
            supersetTitle.setText("Суперсет");
            deleteSupersetButton.setOnClickListener(v -> {
                if (listener != null && supersetGroup != null) listener.onDeleteSuperset(supersetGroup);
            });

            // Вложенный адаптер
            final SupersetExercisesAdapter innerAdapter = new SupersetExercisesAdapter(exercises, listener, map, movedListener, supersetGroup != null ? supersetGroup : 0);
            supersetExercisesRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            supersetExercisesRecyclerView.setAdapter(innerAdapter);
            supersetExercisesRecyclerView.setNestedScrollingEnabled(false);
            supersetExercisesRecyclerView.setHasFixedSize(true);

            // Drag-and-drop внутри суперсета
            ItemTouchHelper.Callback innerDragCallback = new ItemTouchHelper.Callback() {
                @Override
                public boolean isLongPressDragEnabled() { return true; }

                @Override
                public boolean isItemViewSwipeEnabled() { return false; }

                @Override
                public int getMovementFlags(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh) {
                    return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0);
                }

                @Override
                public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh, @NonNull RecyclerView.ViewHolder target) {
                    int fromPos = vh.getAdapterPosition();
                    int toPos = target.getAdapterPosition();
                    innerAdapter.moveItem(fromPos, toPos);
                    return true;
                }

                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int direction) {}
            };

            ItemTouchHelper innerItemTouchHelper = new ItemTouchHelper(innerDragCallback);
            innerItemTouchHelper.attachToRecyclerView(supersetExercisesRecyclerView);
        }
    }

    private static void bindMuscles(ExerciseResponse full, TextView view) {
        if (full != null && full.getMuscleGroups() != null) {
            List<String> primary = new ArrayList<>();
            for (MuscleGroupResponse mg : full.getMuscleGroups()) {
                if (mg.getIsPrimary() != null && mg.getIsPrimary()) primary.add(mg.getName());
            }
            if (!primary.isEmpty()) {
                view.setText(String.join(", ", primary));
                view.setVisibility(View.VISIBLE);
            } else { view.setVisibility(View.GONE); }
        } else { view.setVisibility(View.GONE); }
    }

    private static void bindEquipment(ExerciseResponse full, TextView view) {
        if (full != null && full.getEquipment() != null) {
            List<String> equip = new ArrayList<>();
            for (ru.squidory.trainingdairymobile.data.model.EquipmentResponse eq : full.getEquipment()) {
                equip.add(eq.getName());
            }
            if (!equip.isEmpty()) {
                view.setText(String.join(", ", equip));
                view.setVisibility(View.VISIBLE);
            } else { view.setVisibility(View.GONE); }
        } else { view.setVisibility(View.GONE); }
    }
}
