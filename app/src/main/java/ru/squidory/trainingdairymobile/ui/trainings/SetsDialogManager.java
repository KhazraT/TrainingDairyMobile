package ru.squidory.trainingdairymobile.ui.trainings;

import android.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
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
 * Синхронизирован с SessionActivity для единообразия UI.
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
    private boolean isDropsetMode;

    // UI Elements
    private TextView dialogTitle;
    private MaterialButton typeRegularButton, typeDropsetButton;
    private TextInputLayout restTimeInputLayout;
    private TextInputEditText restTimeInput;
    private View dropsetDivider;
    private TextView dropsetEntriesTitle;
    private LinearLayout dropsetEntriesContainer;
    private MaterialButton addDropsetEntryButton;

    private LinearLayout timePickerLayout;
    private NumberPicker minutesPicker, secondsPicker;
    private TextInputLayout weightLayout, repsLayout, distanceLayout;
    private TextInputEditText weightInput, repsInput, distanceInput;
    private LinearLayout restPickerLayout;
    private NumberPicker restMinutesPicker, restSecondsPicker;

    private List<DropsetEntry> dropsetEntries = new ArrayList<>();

    private SetsDialogManager(ExerciseManagementActivity activity, WorkoutExerciseResponse exercise,
                              String exerciseType, PlannedSetResponse existingSet,
                              List<PlannedSetResponse> localSets, PlannedSetAdapter setAdapter,
                              boolean isDropset, boolean isEdit) {
        this.activity = activity;
        this.exercise = exercise;
        this.exerciseType = exerciseType != null ? exerciseType : REPS_WEIGHT;
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

        initViews(dialogView);
        setupModes();
        fillData();

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);

        dialogView.findViewById(R.id.dialogCancelButton).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.dialogSaveButton).setOnClickListener(v -> handleSave(dialog));

        dialog.show();
    }

    private void initViews(View v) {
        dialogTitle = v.findViewById(R.id.dialogTitle);
        typeRegularButton = v.findViewById(R.id.typeRegularButton);
        typeDropsetButton = v.findViewById(R.id.typeDropsetButton);
        restTimeInputLayout = v.findViewById(R.id.restTimeInputLayout);
        restTimeInput = v.findViewById(R.id.restTimeInput);
        dropsetDivider = v.findViewById(R.id.dropsetDivider);
        dropsetEntriesTitle = v.findViewById(R.id.dropsetEntriesTitle);
        dropsetEntriesContainer = v.findViewById(R.id.dropsetEntriesContainer);
        addDropsetEntryButton = v.findViewById(R.id.addDropsetEntryButton);

        timePickerLayout = v.findViewById(R.id.timePickerLayout);
        minutesPicker = v.findViewById(R.id.minutesPicker);
        secondsPicker = v.findViewById(R.id.secondsPicker);
        weightLayout = v.findViewById(R.id.weightLayout);
        repsLayout = v.findViewById(R.id.repsLayout);
        distanceLayout = v.findViewById(R.id.distanceLayout);
        weightInput = v.findViewById(R.id.weightInput);
        repsInput = v.findViewById(R.id.repsInput);
        distanceInput = v.findViewById(R.id.distanceInput);
        restPickerLayout = v.findViewById(R.id.restPickerLayout);
        restMinutesPicker = v.findViewById(R.id.restMinutesPicker);
        restSecondsPicker = v.findViewById(R.id.restSecondsPicker);

        // Pickers setup
        setupPicker(minutesPicker, 0, 59);
        setupPicker(secondsPicker, 0, 59);
        setupPicker(restMinutesPicker, 0, 59);
        setupPicker(restSecondsPicker, 0, 59);
        restMinutesPicker.setValue(1);

        restTimeInput.setOnClickListener(view -> showTimePickerDialog(restTimeInput));

        boolean isRepsWeight = REPS_WEIGHT.equals(exerciseType);
        v.findViewById(R.id.setTypeLabel).setVisibility(isRepsWeight ? View.VISIBLE : View.GONE);
        v.findViewById(R.id.setTypeButtonsLayout).setVisibility(isRepsWeight ? View.VISIBLE : View.GONE);

        typeRegularButton.setOnClickListener(view -> switchMode(false));
        typeDropsetButton.setOnClickListener(view -> switchMode(true));
        addDropsetEntryButton.setOnClickListener(view -> addDropsetRow(dropsetEntries.size() + 1, null, null));
    }

    private void setupPicker(NumberPicker picker, int min, int max) {
        picker.setMinValue(min);
        picker.setMaxValue(max);
    }

    private void setupModes() {
        if (isEdit) {
            dialogTitle.setText(isDropsetMode ? "Редактировать дропсет" : "Редактировать подход");
            // В режиме редактирования нельзя менять тип
            activity.findViewById(R.id.setTypeButtonsLayout).setVisibility(View.GONE);
            activity.findViewById(R.id.setTypeLabel).setVisibility(View.GONE);
        } else {
            dialogTitle.setText("Добавить подход");
        }
        updateVisibility();
    }

    private void switchMode(boolean dropset) {
        if (isEdit) return; // Защита
        isDropsetMode = dropset;
        updateVisibility();

        if (isDropsetMode && dropsetEntries.isEmpty()) {
            // Инициализация 2 записями по умолчанию
            addDropsetRow(1, null, null);
            addDropsetRow(2, null, null);
        }
    }

    private void updateVisibility() {
        // Buttons
        if (isDropsetMode) {
            typeDropsetButton.setBackgroundColor(0xFFFF9800); // Orange
            typeDropsetButton.setTextColor(0xFFFFFFFF);
            typeRegularButton.setBackgroundColor(0x00000000);
            typeRegularButton.setTextColor(0xFF6200EE);
        } else {
            typeRegularButton.setBackgroundColor(0xFF6200EE); // Purple
            typeRegularButton.setTextColor(0xFFFFFFFF);
            typeDropsetButton.setBackgroundColor(0x00000000);
            typeDropsetButton.setTextColor(0xFF6200EE);
        }

        // Dropset fields
        int dsVis = isDropsetMode ? View.VISIBLE : View.GONE;
        restTimeInputLayout.setVisibility(dsVis);
        dropsetDivider.setVisibility(dsVis);
        dropsetEntriesTitle.setVisibility(dsVis);
        dropsetEntriesContainer.setVisibility(dsVis);
        addDropsetEntryButton.setVisibility(dsVis);

        // Regular fields
        int regVis = isDropsetMode ? View.GONE : View.VISIBLE;
        restPickerLayout.setVisibility(regVis);

        if (!isDropsetMode) {
            switch (exerciseType) {
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
            }
        } else {
            timePickerLayout.setVisibility(View.GONE);
            weightLayout.setVisibility(View.GONE);
            repsLayout.setVisibility(View.GONE);
            distanceLayout.setVisibility(View.GONE);
        }
    }

    private void fillData() {
        if (!isEdit || existingSet == null) return;

        if (isDropsetMode) {
            if (existingSet.getRestTime() != null) {
                int rt = existingSet.getRestTime();
                restTimeInput.setText(String.format("%02d:%02d", rt / 60, rt % 60));
            }
            if (existingSet.getDropsetEntries() != null) {
                dropsetEntriesContainer.removeAllViews();
                dropsetEntries.clear();
                int num = 1;
                for (PlannedSetResponse.DropsetEntry e : existingSet.getDropsetEntries()) {
                    addDropsetRow(num++, e.getWeight(), e.getReps());
                }
            }
        } else {
            if (existingSet.getTargetWeight() != null) weightInput.setText(String.valueOf(existingSet.getTargetWeight()));
            if (existingSet.getTargetReps() != null) repsInput.setText(String.valueOf(existingSet.getTargetReps()));
            if (existingSet.getTargetDistance() != null) distanceInput.setText(String.valueOf(existingSet.getTargetDistance() / 1000.0));
            if (existingSet.getTargetTime() != null) {
                minutesPicker.setValue(existingSet.getTargetTime() / 60);
                secondsPicker.setValue(existingSet.getTargetTime() % 60);
            }
            if (existingSet.getRestTime() != null) {
                restMinutesPicker.setValue(existingSet.getRestTime() / 60);
                restSecondsPicker.setValue(existingSet.getRestTime() % 60);
            }
        }
    }

    private void addDropsetRow(int number, Double weight, Integer reps) {
        View row = LayoutInflater.from(activity).inflate(R.layout.item_dropset_entry, dropsetEntriesContainer, false);
        TextView numberView = row.findViewById(R.id.dropsetEntryNumber);
        TextView typeView = row.findViewById(R.id.dropsetEntryType);
        TextInputEditText wInput = row.findViewById(R.id.dropsetWeightInput);
        TextInputEditText rInput = row.findViewById(R.id.dropsetRepsInput);
        ImageButton removeButton = row.findViewById(R.id.removeDropsetEntryButton);

        numberView.setText(String.valueOf(number));
        boolean isFirst = (number == 1);
        if (isFirst) {
            typeView.setText("основной");
            typeView.setTextColor(0xFF6200EE);
        } else {
            typeView.setText("дропсет");
            typeView.setTextColor(0xFFFF9800);
        }

        if (weight != null) wInput.setText(String.valueOf(weight));
        if (reps != null) rInput.setText(String.valueOf(reps));

        DropsetEntry entry = new DropsetEntry(wInput, rInput);
        dropsetEntries.add(entry);
        row.setTag(entry);

        removeButton.setVisibility(number > 2 ? View.VISIBLE : View.GONE);
        removeButton.setOnClickListener(v -> {
            if (dropsetEntries.size() <= 2) {
                Toast.makeText(activity, "Минимум 2 записи", Toast.LENGTH_SHORT).show();
                return;
            }
            dropsetEntriesContainer.removeView(row);
            dropsetEntries.remove(entry);
            renumberDropsetEntries();
        });

        dropsetEntriesContainer.addView(row);
    }

    private void renumberDropsetEntries() {
        for (int i = 0; i < dropsetEntriesContainer.getChildCount(); i++) {
            View child = dropsetEntriesContainer.getChildAt(i);
            TextView numberView = child.findViewById(R.id.dropsetEntryNumber);
            TextView typeView = child.findViewById(R.id.dropsetEntryType);
            ImageButton removeButton = child.findViewById(R.id.removeDropsetEntryButton);

            numberView.setText(String.valueOf(i + 1));
            boolean isFirst = (i == 0);
            if (isFirst) {
                typeView.setText("основной");
                typeView.setTextColor(0xFF6200EE);
            } else {
                typeView.setText("дропсет");
                typeView.setTextColor(0xFFFF9800);
            }
            removeButton.setVisibility(i >= 2 ? View.VISIBLE : View.GONE);
        }
    }

    private void handleSave(AlertDialog dialog) {
        if (isDropsetMode) {
            handleDropsetSave(dialog);
        } else {
            handleRegularSave(dialog);
        }
    }

    private void handleRegularSave(AlertDialog dialog) {
        String w = getText(weightInput);
        String r = getText(repsInput);
        String d = getText(distanceInput);
        int timeSec = minutesPicker.getValue() * 60 + secondsPicker.getValue();
        int restSec = restMinutesPicker.getValue() * 60 + restSecondsPicker.getValue();

        PlannedSetResponse target = isEdit ? existingSet : new PlannedSetResponse();
        if (!isEdit) {
            target.setId(0);
            target.setSetNumber(localSets.size() + 1);
            localSets.add(target);
        }

        target.setSetType("REGULAR");
        target.setTargetWeight(!w.isEmpty() ? Double.parseDouble(w) : null);
        target.setTargetReps(!r.isEmpty() ? Integer.parseInt(r) : null);
        target.setTargetDistance(!d.isEmpty() ? Double.parseDouble(d) * 1000 : null);
        target.setTargetTime(timeSec > 0 ? timeSec : null);
        target.setRestTime(restSec > 0 ? restSec : null);
        target.setDropsetEntries(null);

        setAdapter.setSets(new ArrayList<>(localSets));
        dialog.dismiss();
    }

    private void handleDropsetSave(AlertDialog dialog) {
        List<PlannedSetResponse.DropsetEntry> entries = new ArrayList<>();
        for (DropsetEntry e : dropsetEntries) {
            Double w = e.getWeightValue();
            Integer r = e.getRepsValue();
            if (w != null && r != null) {
                PlannedSetResponse.DropsetEntry entry = new PlannedSetResponse.DropsetEntry();
                entry.setWeight(w);
                entry.setReps(r);
                entries.add(entry);
            }
        }

        if (entries.size() < 2) {
            Toast.makeText(activity, "Заполните минимум 2 записи", Toast.LENGTH_SHORT).show();
            return;
        }

        int restSec = parseTime(restTimeInput.getText().toString());

        PlannedSetResponse target = isEdit ? existingSet : new PlannedSetResponse();
        if (!isEdit) {
            target.setId(0);
            target.setSetNumber(localSets.size() + 1);
            localSets.add(target);
        }

        target.setSetType("DROPSET");
        target.setDropsetEntries(entries);
        target.setRestTime(restSec > 0 ? restSec : null);
        // Clear regular fields
        target.setTargetWeight(null);
        target.setTargetReps(null);
        target.setTargetTime(null);
        target.setTargetDistance(null);

        setAdapter.setSets(new ArrayList<>(localSets));
        dialog.dismiss();
    }

    private String getText(TextInputEditText in) {
        return in.getText() != null ? in.getText().toString().trim() : "";
    }

    private int parseTime(String s) {
        if (s == null || !s.contains(":")) return 0;
        try {
            String[] p = s.split(":");
            return Integer.parseInt(p[0]) * 60 + Integer.parseInt(p[1]);
        } catch (Exception e) { return 0; }
    }

    private void showTimePickerDialog(TextInputEditText input) {
        View v = LayoutInflater.from(activity).inflate(R.layout.dialog_time_picker, null);
        NumberPicker m = v.findViewById(R.id.minutesPicker);
        NumberPicker s = v.findViewById(R.id.secondsPicker);
        m.setMinValue(0); m.setMaxValue(59);
        s.setMinValue(0); s.setMaxValue(59);

        String current = input.getText().toString();
        if (!current.isEmpty() && current.contains(":")) {
            String[] p = current.split(":");
            m.setValue(Integer.parseInt(p[0]));
            s.setValue(Integer.parseInt(p[1]));
        }

        new AlertDialog.Builder(activity)
                .setTitle("Время отдыха")
                .setView(v)
                .setPositiveButton("OK", (d, w) -> {
                    input.setText(String.format("%02d:%02d", m.getValue(), s.getValue()));
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private static class DropsetEntry {
        final TextInputEditText weightInput, repsInput;
        DropsetEntry(TextInputEditText w, TextInputEditText r) {
            this.weightInput = w; this.repsInput = r;
        }
        Double getWeightValue() {
            String s = weightInput.getText().toString().trim();
            return s.isEmpty() ? null : Double.parseDouble(s);
        }
        Integer getRepsValue() {
            String s = repsInput.getText().toString().trim();
            return s.isEmpty() ? null : Integer.parseInt(s);
        }
    }
}
