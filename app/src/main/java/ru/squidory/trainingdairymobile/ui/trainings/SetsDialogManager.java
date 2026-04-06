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

    /**
     * Показать диалог добавления нового подхода (работает с локальным буфером).
     */
    static void showForAdd(ExerciseManagementActivity activity, WorkoutExerciseResponse exercise,
                           String exerciseType, List<PlannedSetResponse> localSets,
                           PlannedSetAdapter setAdapter, boolean isDropset) {
        new SetsDialogManager(activity, exercise, exerciseType, null, localSets, setAdapter, isDropset, false).show();
    }

    /**
     * Показать диалог редактирования подхода (работает с локальным буфером).
     */
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

    // UI элементы
    private TextInputLayout weightLayout, repsLayout, timeLayout, distanceLayout;
    private TextInputEditText weightInput, repsInput, timeInput, distanceInput;
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
        View setTypeLayout = dialogView.findViewById(R.id.setTypeLayout);
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

        // Скрываем выбор типа подхода — он определяется кнопкой
        setTypeLayout.setVisibility(View.GONE);

        // Заголовок
        if (isEdit) {
            dialogTitle.setText(isDropsetMode ? "Редактировать дропсет" : activity.getString(R.string.edit_set));
        } else {
            dialogTitle.setText(isDropsetMode ? "Добавить дропсет" : activity.getString(R.string.add_set));
        }

        // Заполняем существующие значения
        if (isEdit && existingSet != null) {
            if (existingSet.getTargetWeight() != null) weightInput.setText(String.valueOf(existingSet.getTargetWeight()));
            if (existingSet.getTargetReps() != null) repsInput.setText(String.valueOf(existingSet.getTargetReps()));
            if (existingSet.getTargetTime() != null) timeInput.setText(String.valueOf(existingSet.getTargetTime()));
            if (existingSet.getTargetDistance() != null) distanceInput.setText(String.valueOf(existingSet.getTargetDistance()));
        }

        // Настраиваем видимость полей
        applyExerciseTypeFields();

        // Дропсет записи
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

                if (isEdit && existingSet != null) {
                    existingSet.setDropsetEntries(convertToResponseEntries(entries));
                    existingSet.setSetType("DROPSET");
                } else {
                    PlannedSetResponse newSet = new PlannedSetResponse();
                    newSet.setId(0); // Новый подход
                    newSet.setSetNumber(localSets.size() + 1);
                    newSet.setSetType("DROPSET");
                    newSet.setDropsetEntries(convertToResponseEntries(entries));
                    localSets.add(newSet);
                }
            } else {
                String w = getText(weightInput);
                String r = getText(repsInput);
                String t = getText(timeInput);
                String d = getText(distanceInput);

                if (isEdit && existingSet != null) {
                    if (!w.isEmpty()) existingSet.setTargetWeight(Double.parseDouble(w));
                    else existingSet.setTargetWeight(null);
                    if (!r.isEmpty()) existingSet.setTargetReps(Integer.parseInt(r));
                    else existingSet.setTargetReps(null);
                    if (!t.isEmpty()) existingSet.setTargetTime(Integer.parseInt(t));
                    else existingSet.setTargetTime(null);
                    if (!d.isEmpty()) existingSet.setTargetDistance(Double.parseDouble(d));
                    else existingSet.setTargetDistance(null);
                    existingSet.setSetType("REGULAR");
                } else {
                    PlannedSetResponse newSet = new PlannedSetResponse();
                    newSet.setId(0);
                    newSet.setSetNumber(localSets.size() + 1);
                    newSet.setSetType("REGULAR");
                    if (!w.isEmpty()) newSet.setTargetWeight(Double.parseDouble(w));
                    if (!r.isEmpty()) newSet.setTargetReps(Integer.parseInt(r));
                    if (!t.isEmpty()) newSet.setTargetTime(Integer.parseInt(t));
                    if (!d.isEmpty()) newSet.setTargetDistance(Double.parseDouble(d));
                    localSets.add(newSet);
                }
            }

            // Обновляем локальный адаптер
            setAdapter.setSets(new ArrayList<>(localSets));
            dialog.dismiss();
        });

        dialog.show();
    }

    /**
     * Показать/скрыть поля в зависимости от типа упражнения.
     */
    private void applyExerciseTypeFields() {
        if (isDropsetMode) {
            weightLayout.setVisibility(View.GONE);
            repsLayout.setVisibility(View.GONE);
            timeLayout.setVisibility(View.GONE);
            distanceLayout.setVisibility(View.GONE);
            dropsetEntriesContainer.setVisibility(View.VISIBLE);
            addDropsetEntryButton.setVisibility(View.VISIBLE);
        } else {
            dropsetEntriesContainer.setVisibility(View.GONE);
            addDropsetEntryButton.setVisibility(View.GONE);

            switch (exerciseType != null ? exerciseType : REPS_WEIGHT) {
                case REPS_WEIGHT:
                    weightLayout.setVisibility(View.VISIBLE);
                    repsLayout.setVisibility(View.VISIBLE);
                    timeLayout.setVisibility(View.GONE);
                    distanceLayout.setVisibility(View.GONE);
                    break;
                case TIME_WEIGHT:
                    weightLayout.setVisibility(View.VISIBLE);
                    repsLayout.setVisibility(View.GONE);
                    timeLayout.setVisibility(View.VISIBLE);
                    distanceLayout.setVisibility(View.GONE);
                    break;
                case TIME_DISTANCE:
                    weightLayout.setVisibility(View.GONE);
                    repsLayout.setVisibility(View.GONE);
                    timeLayout.setVisibility(View.VISIBLE);
                    distanceLayout.setVisibility(View.VISIBLE);
                    break;
                case TIME_WEIGHT_DISTANCE:
                    weightLayout.setVisibility(View.VISIBLE);
                    repsLayout.setVisibility(View.GONE);
                    timeLayout.setVisibility(View.VISIBLE);
                    distanceLayout.setVisibility(View.VISIBLE);
                    break;
                default:
                    weightLayout.setVisibility(View.VISIBLE);
                    repsLayout.setVisibility(View.VISIBLE);
                    timeLayout.setVisibility(View.VISIBLE);
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
