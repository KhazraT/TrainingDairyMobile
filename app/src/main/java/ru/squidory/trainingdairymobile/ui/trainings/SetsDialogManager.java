package ru.squidory.trainingdairymobile.ui.trainings;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

import ru.squidory.trainingdairymobile.R;
import ru.squidory.trainingdairymobile.data.model.PlannedSetRequest;
import ru.squidory.trainingdairymobile.data.model.PlannedSetResponse;
import ru.squidory.trainingdairymobile.data.model.WorkoutExerciseResponse;

/**
 * Менеджер диалога добавления/редактирования подхода.
 * Работает с локальным буфером — изменения отправляются на сервер только при нажатии "Сохранить".
 */
class SetsDialogManager {

    static final String REPS_WEIGHT = "REPS_WEIGHT";
    static final String TIME_WEIGHT = "TIME_WEIGHT";
    static final String TIME_DISTANCE = "TIME_DISTANCE";
    static final String TIME_WEIGHT_DISTANCE = "TIME_WEIGHT_DISTANCE";

    static void showForAdd(ExerciseManagementActivity activity, WorkoutExerciseResponse exercise,
                           String exerciseType, List<PlannedSetResponse> localSets,
                           PlannedSetAdapter setAdapter, boolean isDropset) {
        new SetsDialogManager(activity, exercise, exerciseType, null, localSets, setAdapter, isDropset, false).show();
    }

    static void showForEdit(ExerciseManagementActivity activity, WorkoutExerciseResponse exercise,
                            String exerciseType, PlannedSetResponse existingSet,
                            List<PlannedSetResponse> localSets, PlannedSetAdapter setAdapter, boolean isDropset) {
        new SetsDialogManager(activity, exercise, exerciseType, existingSet, localSets, setAdapter, isDropset, true).show();
    }

    private final ExerciseManagementActivity activity;
    private final WorkoutExerciseResponse exercise;
    private final String exerciseType;
    private final PlannedSetResponse existingSet;
    private final List<PlannedSetResponse> localSets;
    private final PlannedSetAdapter setAdapter;
    private final boolean isEdit;
    private final boolean isDropsetMode;

    // UI
    private NumberPicker minutesPicker, secondsPicker;
    private LinearLayout timePickerLayout;
    private TextInputLayout weightLayout, repsLayout, distanceLayout;
    private TextInputEditText weightInput, repsInput, distanceInput;
    private NumberPicker restMinutesPicker, restSecondsPicker;
    private LinearLayout restPickerLayout;
    private LinearLayout dropsetEntriesContainer;
    private MaterialButton addDropsetEntryButton;

    private SetsDialogManager(ExerciseManagementActivity activity, WorkoutExerciseResponse exercise,
                              String exerciseType, PlannedSetResponse existingSet,
                              List<PlannedSetResponse> localSets, PlannedSetAdapter setAdapter,
                              boolean isDropset, boolean isEdit) {
        this.activity = activity;
        this.exercise = exercise;
        this.exerciseType = exerciseType;
        this.existingSet = existingSet;
        this.localSets = localSets;
        this.setAdapter = setAdapter;
        this.isEdit = isEdit;
        this.isDropsetMode = isDropset || (isEdit && "DROPSET".equalsIgnoreCase(existingSet.getSetType()));
    }

    void show() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_add_set, null);
        builder.setView(dialogView);

        TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        timePickerLayout = dialogView.findViewById(R.id.timePickerLayout);
        minutesPicker = dialogView.findViewById(R.id.minutesPicker);
        secondsPicker = dialogView.findViewById(R.id.secondsPicker);
        weightLayout = dialogView.findViewById(R.id.weightLayout);
        repsLayout = dialogView.findViewById(R.id.repsLayout);
        distanceLayout = dialogView.findViewById(R.id.distanceLayout);
        weightInput = dialogView.findViewById(R.id.weightInput);
        repsInput = dialogView.findViewById(R.id.repsInput);
        distanceInput = dialogView.findViewById(R.id.distanceInput);
        restPickerLayout = dialogView.findViewById(R.id.restPickerLayout);
        restMinutesPicker = dialogView.findViewById(R.id.restMinutesPicker);
        restSecondsPicker = dialogView.findViewById(R.id.restSecondsPicker);
        dropsetEntriesContainer = dialogView.findViewById(R.id.dropsetEntriesContainer);
        addDropsetEntryButton = dialogView.findViewById(R.id.addDropsetEntryButton);
        MaterialButton cancelButton = dialogView.findViewById(R.id.dialogCancelButton);
        MaterialButton saveButton = dialogView.findViewById(R.id.dialogSaveButton);

        if (isEdit) {
            dialogTitle.setText(isDropsetMode ? "Редактировать дропсет" : activity.getString(R.string.edit_set));
        } else {
            dialogTitle.setText(isDropsetMode ? "Добавить дропсет" : activity.getString(R.string.add_set));
        }

        applyExerciseTypeFields();

        // Заполняем при редактировании
        if (isEdit && existingSet != null) {
            if (existingSet.getTargetWeight() != null) weightInput.setText(String.valueOf(existingSet.getTargetWeight()));
            if (existingSet.getTargetReps() != null) repsInput.setText(String.valueOf(existingSet.getTargetReps()));
            if (existingSet.getTargetDistance() != null) {
                distanceInput.setText(String.valueOf(existingSet.getTargetDistance() / 1000.0));
            }
            if (existingSet.getTargetTime() != null) {
                int sec = existingSet.getTargetTime();
                minutesPicker.setValue(sec / 60);
                secondsPicker.setValue(sec % 60);
            }
            if (existingSet.getRestTime() != null) {
                int restSec = existingSet.getRestTime();
                restMinutesPicker.setValue(restSec / 60);
                restSecondsPicker.setValue(restSec % 60);
            }
        }

        // Дропсет
        List<DropsetRow> dropsetRows = new ArrayList<>();
        if (isEdit && isDropsetMode && existingSet != null && existingSet.getDropsetEntries() != null) {
            for (PlannedSetResponse.DropsetEntry entry : existingSet.getDropsetEntries()) {
                addDropsetRow(dropsetEntriesContainer, dropsetRows, entry.getWeight(), entry.getReps());
            }
        }
        addDropsetEntryButton.setOnClickListener(v -> addDropsetRow(dropsetEntriesContainer, dropsetRows, null, null));

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        cancelButton.setOnClickListener(v -> dialog.dismiss());

        saveButton.setOnClickListener(v -> {
            if (isDropsetMode) {
                handleDropsetSave(dialog);
            } else {
                handleRegularSave(dialog);
            }
        });

        dialog.show();
    }

    private void handleRegularSave(AlertDialog dialog) {
        String w = getText(weightInput);
        String r = getText(repsInput);
        String d = getText(distanceInput);
        int timeSec = minutesPicker.getValue() * 60 + secondsPicker.getValue();
        int restSec = restMinutesPicker.getValue() * 60 + restSecondsPicker.getValue();

        if (isEdit && existingSet != null) {
            if (!w.isEmpty()) existingSet.setTargetWeight(Double.parseDouble(w));
            else existingSet.setTargetWeight(null);
            if (!r.isEmpty()) existingSet.setTargetReps(Integer.parseInt(r));
            else existingSet.setTargetReps(null);
            existingSet.setTargetTime(timeSec > 0 ? timeSec : null);
            if (!d.isEmpty()) existingSet.setTargetDistance(Double.parseDouble(d) * 1000);
            else existingSet.setTargetDistance(null);
            existingSet.setRestTime(restSec > 0 ? restSec : null);
            existingSet.setSetType("REGULAR");
        } else {
            PlannedSetResponse newSet = new PlannedSetResponse();
            newSet.setId(0);
            newSet.setSetNumber(localSets.size() + 1);
            newSet.setSetType("REGULAR");
            if (!w.isEmpty()) newSet.setTargetWeight(Double.parseDouble(w));
            if (!r.isEmpty()) newSet.setTargetReps(Integer.parseInt(r));
            newSet.setTargetTime(timeSec > 0 ? timeSec : null);
            if (!d.isEmpty()) newSet.setTargetDistance(Double.parseDouble(d) * 1000);
            newSet.setRestTime(restSec > 0 ? restSec : null);
            localSets.add(newSet);
        }
        setAdapter.setSets(new ArrayList<>(localSets));
        dialog.dismiss();
    }

    private void handleDropsetSave(AlertDialog dialog) {
        List<DropsetRow> rows = collectDropsetRows();
        if (rows.isEmpty()) {
            Toast.makeText(activity, "Добавьте хотя бы одну запись дропсета", Toast.LENGTH_SHORT).show();
            return;
        }

        int restSec = restMinutesPicker.getValue() * 60 + restSecondsPicker.getValue();

        if (isEdit && existingSet != null) {
            List<PlannedSetRequest.DropsetEntry> entries = rowsToEntries(rows);
            existingSet.setDropsetEntries(convertToResponseEntries(entries));
            existingSet.setSetType("DROPSET");
            existingSet.setRestTime(restSec > 0 ? restSec : null);
        } else {
            PlannedSetResponse newSet = new PlannedSetResponse();
            newSet.setId(0);
            newSet.setSetNumber(localSets.size() + 1);
            newSet.setSetType("DROPSET");
            newSet.setRestTime(restSec > 0 ? restSec : null);
            newSet.setDropsetEntries(convertToResponseEntries(rowsToEntries(rows)));
            localSets.add(newSet);
        }
        setAdapter.setSets(new ArrayList<>(localSets));
        dialog.dismiss();
    }

    private List<DropsetRow> collectDropsetRows() {
        List<DropsetRow> rows = new ArrayList<>();
        int childCount = dropsetEntriesContainer.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = dropsetEntriesContainer.getChildAt(i);
            if (child.getTag() instanceof DropsetRow) {
                rows.add((DropsetRow) child.getTag());
            }
        }
        return rows;
    }

    private List<PlannedSetRequest.DropsetEntry> rowsToEntries(List<DropsetRow> rows) {
        List<PlannedSetRequest.DropsetEntry> entries = new ArrayList<>();
        for (DropsetRow row : rows) {
            if (row.getWeight() != null && row.getReps() != null) {
                PlannedSetRequest.DropsetEntry entry = new PlannedSetRequest.DropsetEntry();
                entry.setWeight(row.getWeight());
                entry.setReps(row.getReps());
                entries.add(entry);
            }
        }
        return entries;
    }

    private void applyExerciseTypeFields() {
        if (isDropsetMode) {
            timePickerLayout.setVisibility(View.GONE);
            weightLayout.setVisibility(View.GONE);
            repsLayout.setVisibility(View.GONE);
            distanceLayout.setVisibility(View.GONE);
            restPickerLayout.setVisibility(View.GONE);
            dropsetEntriesContainer.setVisibility(View.VISIBLE);
            addDropsetEntryButton.setVisibility(View.VISIBLE);
        } else {
            dropsetEntriesContainer.setVisibility(View.GONE);
            addDropsetEntryButton.setVisibility(View.GONE);
            restPickerLayout.setVisibility(View.VISIBLE);

            minutesPicker.setMinValue(0);
            minutesPicker.setMaxValue(59);
            minutesPicker.setValue(0);
            secondsPicker.setMinValue(0);
            secondsPicker.setMaxValue(59);
            secondsPicker.setValue(0);

            restMinutesPicker.setMinValue(0);
            restMinutesPicker.setMaxValue(59);
            restMinutesPicker.setValue(1);
            restSecondsPicker.setMinValue(0);
            restSecondsPicker.setMaxValue(59);
            restSecondsPicker.setValue(0);

            switch (exerciseType != null ? exerciseType : REPS_WEIGHT) {
                case REPS_WEIGHT:
                    timePickerLayout.setVisibility(View.GONE);
                    weightLayout.setVisibility(View.VISIBLE);
                    repsLayout.setVisibility(View.VISIBLE);
                    distanceLayout.setVisibility(View.GONE);
                    break;
                case TIME_WEIGHT:
                    timePickerLayout.setVisibility(View.VISIBLE);
                    weightLayout.setVisibility(View.VISIBLE);
                    repsLayout.setVisibility(View.GONE);
                    distanceLayout.setVisibility(View.GONE);
                    break;
                case TIME_DISTANCE:
                    timePickerLayout.setVisibility(View.VISIBLE);
                    weightLayout.setVisibility(View.GONE);
                    repsLayout.setVisibility(View.GONE);
                    distanceLayout.setVisibility(View.VISIBLE);
                    break;
                case TIME_WEIGHT_DISTANCE:
                    timePickerLayout.setVisibility(View.VISIBLE);
                    weightLayout.setVisibility(View.VISIBLE);
                    repsLayout.setVisibility(View.GONE);
                    distanceLayout.setVisibility(View.VISIBLE);
                    break;
                default:
                    timePickerLayout.setVisibility(View.VISIBLE);
                    weightLayout.setVisibility(View.VISIBLE);
                    repsLayout.setVisibility(View.VISIBLE);
                    distanceLayout.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }

    private void addDropsetRow(LinearLayout container, List<DropsetRow> rows, Double weight, Integer reps) {
        View row = LayoutInflater.from(activity).inflate(R.layout.item_dropset_entry, container, false);
        TextInputEditText wInput = row.findViewById(R.id.dropsetWeightInput);
        TextInputEditText rInput = row.findViewById(R.id.dropsetRepsInput);
        android.widget.ImageButton removeButton = row.findViewById(R.id.removeDropsetEntryButton);
        TextView numberView = row.findViewById(R.id.dropsetEntryNumber);

        numberView.setText(String.valueOf(rows.size() + 1));
        if (weight != null) wInput.setText(String.valueOf(weight));
        if (reps != null) rInput.setText(String.valueOf(reps));

        DropsetRow rowObj = new DropsetRow(wInput, rInput);
        rows.add(rowObj);
        row.setTag(rowObj);

        removeButton.setOnClickListener(v -> {
            container.removeView(row);
            rows.remove(rowObj);
        });

        container.addView(row);
    }

    private String getText(TextInputEditText input) {
        return input.getText() != null ? input.getText().toString().trim() : "";
    }

    private List<PlannedSetResponse.DropsetEntry> convertToResponseEntries(List<PlannedSetRequest.DropsetEntry> reqEntries) {
        List<PlannedSetResponse.DropsetEntry> result = new ArrayList<>();
        for (PlannedSetRequest.DropsetEntry req : reqEntries) {
            PlannedSetResponse.DropsetEntry resp = new PlannedSetResponse.DropsetEntry();
            resp.setWeight(req.getWeight());
            resp.setReps(req.getReps());
            result.add(resp);
        }
        return result;
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
