package ru.squidory.trainingdairymobile.ui.trainings;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ru.squidory.trainingdairymobile.R;
import ru.squidory.trainingdairymobile.data.model.SessionExerciseResponse;
import ru.squidory.trainingdairymobile.data.model.SessionSetResponse;
import ru.squidory.trainingdairymobile.ui.exercises.ExerciseDetailActivity;

/**
 * Readonly адаптер для отображения упражнений в деталях сессии.
 * Поддерживает группировку суперсетов.
 */
public class SessionDetailExerciseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPE_SINGLE = 0;
    public static final int TYPE_SUPERSET_GROUP = 1;

    private final List<DisplayItem> items = new ArrayList<>();

    static class DisplayItem {
        final int type;
        final SessionExerciseResponse exercise;
        final Integer supersetGroup;
        final List<SessionExerciseResponse> ssExercises;

        DisplayItem(int type, SessionExerciseResponse exercise, Integer supersetGroup,
                     List<SessionExerciseResponse> ssExercises) {
            this.type = type;
            this.exercise = exercise;
            this.supersetGroup = supersetGroup;
            this.ssExercises = ssExercises;
        }
    }

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
        return new ExerciseViewHolder(inflater.inflate(R.layout.item_session_detail_exercise, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DisplayItem item = items.get(position);
        if (holder instanceof ExerciseViewHolder) {
            if (item.type == TYPE_SINGLE) {
                ((ExerciseViewHolder) holder).bindSingle(item.exercise);
            } else {
                ((ExerciseViewHolder) holder).bindSuperset(item.supersetGroup, item.ssExercises);
            }
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ExerciseViewHolder extends RecyclerView.ViewHolder {
        protected final TextView exerciseNameText;
        protected final TextView supersetLabel;
        protected final RecyclerView setsRecyclerView;
        protected final TextView noSetsText;
        protected final ImageButton exerciseInfoButton;

        ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            exerciseNameText = itemView.findViewById(R.id.exerciseNameText);
            supersetLabel = itemView.findViewById(R.id.supersetLabel);
            setsRecyclerView = itemView.findViewById(R.id.setsRecyclerView);
            noSetsText = itemView.findViewById(R.id.noSetsText);
            exerciseInfoButton = itemView.findViewById(R.id.exerciseInfoButton);
        }

        void bindSingle(SessionExerciseResponse exercise) {
            exerciseNameText.setText(getExerciseName(exercise));
            supersetLabel.setVisibility(View.GONE);
            setupInfoButton(exercise);

            List<SessionExerciseResponse> singleList = new ArrayList<>();
            singleList.add(exercise);
            bindSets(singleList);
        }

        void bindSuperset(Integer supersetGroup, List<SessionExerciseResponse> exercises) {
            StringBuilder nameBuilder = new StringBuilder();
            for (int i = 0; i < exercises.size(); i++) {
                if (i > 0) nameBuilder.append(" / ");
                nameBuilder.append(getExerciseName(exercises.get(i)));
            }
            exerciseNameText.setText(nameBuilder.toString());
            supersetLabel.setVisibility(View.VISIBLE);
            supersetLabel.setText("Суперсет (" + exercises.size() + ")");
            exerciseInfoButton.setVisibility(View.GONE);

            bindSets(exercises);
        }

        private void setupInfoButton(SessionExerciseResponse exercise) {
            long exerciseId = exercise.getExerciseId() != null ? exercise.getExerciseId() : 0;
            if (exerciseId > 0) {
                exerciseInfoButton.setVisibility(View.VISIBLE);
                exerciseInfoButton.setOnClickListener(v -> {
                    Intent intent = new Intent(itemView.getContext(), ExerciseDetailActivity.class);
                    intent.putExtra(ExerciseDetailActivity.EXTRA_EXERCISE_ID, exerciseId);
                    intent.putExtra(ExerciseDetailActivity.EXTRA_EXERCISE_NAME, getExerciseName(exercise));
                    itemView.getContext().startActivity(intent);
                });
            } else {
                exerciseInfoButton.setVisibility(View.GONE);
            }
        }

        protected void bindSets(List<SessionExerciseResponse> exercises) {
            List<SessionSetResponse> allSets = new ArrayList<>();
            for (SessionExerciseResponse ex : exercises) {
                List<SessionSetResponse> completed = ex.getCompletedSets();
                if (completed != null && !completed.isEmpty()) {
                    allSets.addAll(completed);
                }
            }

            if (allSets.isEmpty()) {
                setsRecyclerView.setVisibility(View.GONE);
                noSetsText.setVisibility(View.VISIBLE);
            } else {
                setsRecyclerView.setVisibility(View.VISIBLE);
                noSetsText.setVisibility(View.GONE);

                SessionExerciseResponse wrapper = new SessionExerciseResponse();
                wrapper.setExerciseType(exercises.get(0).getExerciseType());
                wrapper.setCompletedSets(allSets);

                SessionDetailSetsAdapter adapter = new SessionDetailSetsAdapter(wrapper);
                setsRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
                setsRecyclerView.setAdapter(adapter);
                setsRecyclerView.setNestedScrollingEnabled(false);
            }
        }

        protected String getExerciseName(SessionExerciseResponse ex) {
            if (ex.getExercise() != null && ex.getExercise().getName() != null) {
                return ex.getExercise().getName();
            }
            if (ex.getExerciseName() != null && !ex.getExerciseName().isEmpty()) {
                return ex.getExerciseName();
            }
            return "Упражнение";
        }
    }
}