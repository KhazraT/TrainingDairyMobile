package ru.squidory.trainingdairymobile.ui.trainings;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.squidory.trainingdairymobile.R;
import ru.squidory.trainingdairymobile.data.model.PlannedSetRequest;
import ru.squidory.trainingdairymobile.data.model.PlannedSetResponse;
import ru.squidory.trainingdairymobile.data.model.WorkoutExerciseResponse;
import ru.squidory.trainingdairymobile.data.repository.ProgramRepository;

/**
 * Менеджер диалога добавления/редактирования подхода.
 * Показывает только поля, соответствующие типу упражнения.
 */
class SetsDialogManager {

    static final String REPS_WEIGHT = "REPS_WEIGHT";
    static final String TIME_WEIGHT = "TIME_WEIGHT";
    static final String TIME_DISTANCE = "TIME_DISTANCE";
    static final String TIME_WEIGHT_DISTANCE = "TIME_WEIGHT_DISTANCE";

    static void show(ExerciseManagementActivity activity, WorkoutExerciseResponse exercise,
                     String exerciseType, PlannedSetResponse existingSet, PlannedSetAdapter setAdapter, boolean forceDropset) {
        new SetsDialogManager(activity, exercise, exerciseType, existingSet, setAdapter, forceDropset).show();
    }

    private final ExerciseManagementActivity activity;
    private final WorkoutExerciseResponse exercise;
    private final PlannedSetResponse existingSet;
    private final PlannedSetAdapter setAdapter;
    private final boolean forceDropset;
    private final boolean isEdit;
    private final ProgramRepository programRepository;
    private final String exerciseType;

    // Все поля и их Layout-ы
    private TextInputLayout weightLayout;
    private TextInputLayout repsLayout;
    private TextInputLayout timeLayout;
    private TextInputLayout distanceLayout;
    private TextInputEditText weightInput;
    private TextInputEditText repsInput;
    private TextInputEditText timeInput;
    private TextInputEditText distanceInput;
    private LinearLayout dropsetEntriesContainer;
    private MaterialButton addDropsetEntryButton;

    private SetsDialogManager(ExerciseManagementActivity activity, WorkoutExerciseResponse exercise,
                              String exerciseType, PlannedSetResponse existingSet, PlannedSetAdapter setAdapter, boolean forceDropset) {
        this.activity = activity;
        this.exercise = exercise;
        this.existingSet = existingSet;
        this.setAdapter = setAdapter;
        this.forceDropset = forceDropset;
        this.isEdit = existingSet != null;
        this.programRepository = ProgramRepository.getInstance();
        this.exerciseType = exerciseType;
    }

    void show() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_add_set, null);
        builder.setView(dialogView);

        TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        android.widget.AutoCompleteTextView setTypeSpinner = dialogView.findViewById(R.id.setTypeSpinner);
        weightLayout = dialogView.findViewById(R.id.weightLayout);
        repsLayout = dialogView.findViewById(R.id.repsLayout);
        timeLayout = dialogView.findViewById(R.id.timeLayout);
        distanceLayout = dialogView.findViewById(R.id.distanceLayout);
        weightInput = dialogView.findViewById(R.id.weightInput);
        repsInput = dialogView.findViewById(R.id.repsInput);
        timeInput = dialogView.findViewById(R.id.timeInput);
        distanceInput = dialogView.findViewById(R.id.distanceInput);
        dropsetEntriesContainer = dialogView.findViewById(R.id.dropsetEntriesContainer);
        addDropsetEntryButton = dialogView.findViewById(R.id.addDropsetEntryButton);
        MaterialButton cancelButton = dialogView.findViewById(R.id.dialogCancelButton);
        MaterialButton saveButton = dialogView.findViewById(R.id.dialogSaveButton);

        dialogTitle.setText(isEdit ? R.string.edit_set : R.string.add_set);

        boolean isDropset = forceDropset || (isEdit && "DROPSET".equalsIgnoreCase(existingSet.getSetType()));

        // Дропсет доступен только для REPS_WEIGHT
        boolean dropsetEnabled = REPS_WEIGHT.equals(exerciseType);
        List<String> typeOptions = new ArrayList<>();
        typeOptions.add(activity.getString(R.string.set_regular));
        if (dropsetEnabled) {
            typeOptions.add(activity.getString(R.string.set_dropset));
        }

        android.widget.ArrayAdapter<String> typeAdapter = new android.widget.ArrayAdapter<>(activity,
                android.R.layout.simple_dropdown_item_1line, typeOptions.toArray(new String[0]));
        setTypeSpinner.setAdapter(typeAdapter);

        if (!dropsetEnabled) {
            setTypeSpinner.setText(activity.getString(R.string.set_regular), false);
            setTypeSpinner.setEnabled(false);
        } else {
            setTypeSpinner.setText(isDropset ? activity.getString(R.string.set_dropset) : activity.getString(R.string.set_regular), false);
        }

        // Заполняем существующие значения
        if (isEdit) {
            if (existingSet.getTargetWeight() != null) weightInput.setText(String.valueOf(existingSet.getTargetWeight()));
            if (existingSet.getTargetReps() != null) repsInput.setText(String.valueOf(existingSet.getTargetReps()));
            if (existingSet.getTargetTime() != null) timeInput.setText(String.valueOf(existingSet.getTargetTime()));
            if (existingSet.getTargetDistance() != null) distanceInput.setText(String.valueOf(existingSet.getTargetDistance()));
        }

        final boolean[] isDropsetState = new boolean[]{isDropset && dropsetEnabled};
        if (dropsetEnabled) {
            setTypeSpinner.setOnItemClickListener((parent, view, position, id) -> {
                isDropsetState[0] = (position == 1);
                updateDropsetVisibility(isDropsetState[0]);
            });
        }

        // Скрываем поля по типу упражнения
        applyExerciseTypeFields();
        updateDropsetVisibility(isDropsetState[0]);

        // Дропсет записи
        List<DropsetRow> dropsetRows = new ArrayList<>();
        if (isEdit && isDropset && existingSet.getDropsetEntries() != null) {
            for (PlannedSetResponse.DropsetEntry entry : existingSet.getDropsetEntries()) {
                addDropsetRow(dropsetEntriesContainer, dropsetRows, entry.getWeight(), entry.getReps());
            }
        }
        addDropsetEntryButton.setOnClickListener(v -> addDropsetRow(dropsetEntriesContainer, dropsetRows, null, null));

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        cancelButton.setOnClickListener(v -> dialog.dismiss());

        saveButton.setOnClickListener(v -> {
            PlannedSetRequest request = new PlannedSetRequest();
            request.setSetType(isDropsetState[0] ? "DROPSET" : "REGULAR");

            if (isDropsetState[0]) {
                List<PlannedSetRequest.DropsetEntry> entries = new ArrayList<>();
                for (DropsetRow row : dropsetRows) {
                    Double w = row.getWeight();
                    Integer r = row.getReps();
                    if (w != null && r != null) {
                        PlannedSetRequest.DropsetEntry entry = new PlannedSetRequest.DropsetEntry();
                        entry.setWeight(w);
                        entry.setReps(r);
                        entries.add(entry);
                    }
                }
                if (entries.isEmpty()) {
                    Toast.makeText(activity, "Добавьте хотя бы одну запись дропсета", Toast.LENGTH_SHORT).show();
                    return;
                }
                request.setDropsetEntries(entries);
            } else {
                String w = getText(weightInput);
                String r = getText(repsInput);
                String t = getText(timeInput);
                String d = getText(distanceInput);
                if (!w.isEmpty()) request.setTargetWeight(Double.parseDouble(w));
                if (!r.isEmpty()) request.setTargetReps(Integer.parseInt(r));
                if (!t.isEmpty()) request.setTargetTime(Integer.parseInt(t));
                if (!d.isEmpty()) request.setTargetDistance(Double.parseDouble(d));
            }

            List<PlannedSetResponse> currentSets = setAdapter.getSets();
            int nextNumber = currentSets.isEmpty() ? 1 : currentSets.size() + 1;
            request.setSetNumber(isEdit ? existingSet.getSetNumber() : nextNumber);

            Callback<PlannedSetResponse> callback = new Callback<PlannedSetResponse>() {
                @Override
                public void onResponse(Call<PlannedSetResponse> call, Response<PlannedSetResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        dialog.dismiss();
                        Toast.makeText(activity, R.string.set_saved, Toast.LENGTH_SHORT).show();
                        reloadSets();
                    } else {
                        Toast.makeText(activity, "Ошибка: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<PlannedSetResponse> call, Throwable t) {
                    Toast.makeText(activity, "Ошибка: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            };

            if (isEdit) {
                programRepository.updatePlannedSet(existingSet.getId(), request, callback);
            } else {
                programRepository.createPlannedSet(exercise.getId(), request, callback);
            }
        });

        dialog.show();
    }

    /**
     * Скрыть поля, не относящиеся к типу упражнения.
     */
    private void applyExerciseTypeFields() {
        // Сначала показываем все
        weightLayout.setVisibility(View.VISIBLE);
        repsLayout.setVisibility(View.VISIBLE);
        timeLayout.setVisibility(View.VISIBLE);
        distanceLayout.setVisibility(View.VISIBLE);

        if (exerciseType == null) return;

        switch (exerciseType) {
            case REPS_WEIGHT:
                // Вес, Повторения
                timeLayout.setVisibility(View.GONE);
                distanceLayout.setVisibility(View.GONE);
                break;
            case TIME_WEIGHT:
                // Вес, Время
                repsLayout.setVisibility(View.GONE);
                distanceLayout.setVisibility(View.GONE);
                break;
            case TIME_DISTANCE:
                // Время, Дистанция
                weightLayout.setVisibility(View.GONE);
                repsLayout.setVisibility(View.GONE);
                break;
            case TIME_WEIGHT_DISTANCE:
                // Вес, Время, Дистанция
                repsLayout.setVisibility(View.GONE);
                break;
        }
    }

    private void updateDropsetVisibility(boolean isDropset) {
        if (isDropset) {
            weightInput.setEnabled(false);
            repsInput.setEnabled(false);
            dropsetEntriesContainer.setVisibility(View.VISIBLE);
            addDropsetEntryButton.setVisibility(View.VISIBLE);
        } else {
            weightInput.setEnabled(true);
            repsInput.setEnabled(true);
            dropsetEntriesContainer.setVisibility(View.GONE);
            addDropsetEntryButton.setVisibility(View.GONE);
        }
    }

    private void reloadSets() {
        programRepository.getPlannedSets(exercise.getId(), new ProgramRepository.PlannedSetsCallback() {
            @Override
            public void onSuccess(List<PlannedSetResponse> sets) {
                sets.sort((a, b) -> {
                    Integer numA = a.getSetNumber();
                    Integer numB = b.getSetNumber();
                    if (numA == null) numA = 0;
                    if (numB == null) numB = 0;
                    return numA.compareTo(numB);
                });
                setAdapter.setSets(sets);
            }
            @Override public void onError(String error) { setAdapter.setSets(new ArrayList<>()); }
        });
    }

    private void addDropsetRow(LinearLayout container, List<DropsetRow> rows, Double weight, Integer reps) {
        View row = LayoutInflater.from(activity).inflate(R.layout.item_dropset_entry, container, false);
        TextInputEditText wInput = row.findViewById(R.id.dropsetWeightInput);
        TextInputEditText rInput = row.findViewById(R.id.dropsetRepsInput);
        android.widget.ImageButton removeButton = row.findViewById(R.id.removeDropsetEntryButton);
        TextView label = row.findViewById(R.id.dropsetEntryLabel);

        label.setText("Запись #" + (rows.size() + 1));
        if (weight != null) wInput.setText(String.valueOf(weight));
        if (reps != null) rInput.setText(String.valueOf(reps));

        DropsetRow rowObj = new DropsetRow(wInput, rInput);
        rows.add(rowObj);

        removeButton.setOnClickListener(v -> {
            container.removeView(row);
            rows.remove(rowObj);
        });

        container.addView(row);
    }

    private String getText(TextInputEditText input) {
        return input.getText() != null ? input.getText().toString().trim() : "";
    }

    private static class DropsetRow {
        final TextInputEditText weightInput;
        final TextInputEditText repsInput;

        DropsetRow(TextInputEditText weightInput, TextInputEditText repsInput) {
            this.weightInput = weightInput;
            this.repsInput = repsInput;
        }

        Double getWeight() {
            String s = weightInput.getText() != null ? weightInput.getText().toString().trim() : "";
            return s.isEmpty() ? null : Double.parseDouble(s);
        }

        Integer getReps() {
            String s = repsInput.getText() != null ? repsInput.getText().toString().trim() : "";
            return s.isEmpty() ? null : Integer.parseInt(s);
        }
    }
}
