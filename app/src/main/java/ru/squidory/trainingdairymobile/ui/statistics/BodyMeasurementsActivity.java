package ru.squidory.trainingdairymobile.ui.statistics;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ru.squidory.trainingdairymobile.R;
import ru.squidory.trainingdairymobile.data.model.BodyMeasurementProgress;
import ru.squidory.trainingdairymobile.data.model.BodyMeasurementResponse;
import ru.squidory.trainingdairymobile.data.repository.StatsRepository;

/**
 * Activity для просмотра и управления измерениями тела.
 */
public class BodyMeasurementsActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TabLayout measurementTabs;
    private TextView currentValueText;
    private TextView changeText;
    private LineChart progressChart;
    private RecyclerView measurementsRecyclerView;
    private TextView emptyText;
    private ProgressBar progressBar;
    private FloatingActionButton addFab;

    private StatsRepository statsRepository;
    private BodyMeasurementAdapter adapter;
    private String currentMeasurementType = BodyMeasurementResponse.BODY_WEIGHT;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat displayFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

    // Список типов измерений
    private final String[] measurementTypes = {
        BodyMeasurementResponse.BODY_WEIGHT,
        BodyMeasurementResponse.BODY_FAT_PERCENTAGE,
        BodyMeasurementResponse.CHEST_CIRCUMFERENCE,
        BodyMeasurementResponse.WAIST_CIRCUMFERENCE,
        BodyMeasurementResponse.HIP_CIRCUMFERENCE,
        BodyMeasurementResponse.ARM_CIRCUMFERENCE,
        BodyMeasurementResponse.THIGH_CIRCUMFERENCE,
        BodyMeasurementResponse.CALF_CIRCUMFERENCE,
        BodyMeasurementResponse.NECK_CIRCUMFERENCE,
        BodyMeasurementResponse.SHOULDER_CIRCUMFERENCE
    };

    private final String[] measurementTypeNames = {
        "Вес", "% жира", "Грудь", "Талия", "Бёдра", 
        "Рука", "Бедро", "Голень", "Шея", "Плечи"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_body_measurements);

        statsRepository = StatsRepository.getInstance();
        
        initViews();
        setupTabs();
        setupRecyclerView();
        setupChart();
        
        loadData();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        measurementTabs = findViewById(R.id.measurementTabs);
        currentValueText = findViewById(R.id.currentValueText);
        changeText = findViewById(R.id.changeText);
        progressChart = findViewById(R.id.progressChart);
        measurementsRecyclerView = findViewById(R.id.measurementsRecyclerView);
        emptyText = findViewById(R.id.emptyText);
        progressBar = findViewById(R.id.progressBar);
        addFab = findViewById(R.id.addFab);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        addFab.setOnClickListener(v -> showAddMeasurementDialog());
    }

    private void setupTabs() {
        for (String name : measurementTypeNames) {
            measurementTabs.addTab(measurementTabs.newTab().setText(name));
        }

        measurementTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentMeasurementType = measurementTypes[tab.getPosition()];
                // Очищаем график при переключении вкладки
                progressChart.clear();
                loadData();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupRecyclerView() {
        adapter = new BodyMeasurementAdapter();
        adapter.setOnMeasurementClickListener(new BodyMeasurementAdapter.OnMeasurementClickListener() {
            @Override
            public void onMeasurementClick(BodyMeasurementResponse measurement) {
                showEditMeasurementDialog(measurement);
            }

            @Override
            public void onMeasurementLongClick(BodyMeasurementResponse measurement) {
                showDeleteConfirmation(measurement);
            }
        });
        measurementsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        measurementsRecyclerView.setAdapter(adapter);
    }

    private void setupChart() {
        progressChart.getDescription().setEnabled(false);
        progressChart.setDrawGridBackground(false);
        progressChart.getLegend().setEnabled(true);
        
        XAxis xAxis = progressChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        
        progressChart.getAxisLeft().setDrawGridLines(true);
        progressChart.getAxisRight().setEnabled(false);
    }

    private void loadData() {
        showLoading(true);

        // Загружаем последнее значение
        statsRepository.getLatestBodyMeasurement(currentMeasurementType, new StatsRepository.BodyMeasurementCallback() {
            @Override
            public void onSuccess(BodyMeasurementResponse measurement) {
                displayCurrentValue(measurement);
            }

            @Override
            public void onError(String error) {
                currentValueText.setText("-");
                changeText.setText("Нет данных");
            }
        });

        // Загружаем историю
        statsRepository.getBodyMeasurementsByType(currentMeasurementType, new StatsRepository.BodyMeasurementsCallback() {
            @Override
            public void onSuccess(List<BodyMeasurementResponse> measurements) {
                // Сортируем по дате (старые первые) для графика
                List<BodyMeasurementResponse> sortedForChart = new ArrayList<>(measurements);
                sortedForChart.sort((a, b) -> {
                    if (a.getMeasuredAt() == null) return 1;
                    if (b.getMeasuredAt() == null) return -1;
                    return a.getMeasuredAt().compareTo(b.getMeasuredAt());
                });

                // Сортируем в обратном порядке (новые первые) для списка
                List<BodyMeasurementResponse> sortedForList = new ArrayList<>(measurements);
                sortedForList.sort((a, b) -> {
                    if (a.getMeasuredAt() == null) return 1;
                    if (b.getMeasuredAt() == null) return -1;
                    return b.getMeasuredAt().compareTo(a.getMeasuredAt()); // Обратный порядок
                });

                adapter.setMeasurements(sortedForList);
                emptyText.setVisibility(measurements.isEmpty() ? View.VISIBLE : View.GONE);
                displayProgressChart(sortedForChart);
                showLoading(false);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(BodyMeasurementsActivity.this, "Ошибка: " + error, Toast.LENGTH_SHORT).show();
                progressChart.clear(); // Очищаем график при ошибке
                showLoading(false);
            }
        });
    }

    private void displayCurrentValue(BodyMeasurementResponse measurement) {
        if (measurement == null || measurement.getValue() == null) {
            currentValueText.setText("-");
            changeText.setVisibility(View.GONE);
            return;
        }

        String unit = getUnitDisplay(measurement.getMeasurementType());
        currentValueText.setText(String.format(Locale.getDefault(), "%.1f %s",
            measurement.getValue(), unit));

        // Скрываем поле изменения (пока не реализовано)
        changeText.setVisibility(View.GONE);
    }

    private void displayProgressChart(List<BodyMeasurementResponse> measurements) {
        if (measurements == null || measurements.isEmpty()) {
            progressChart.clear(); // Очищаем старые данные
            progressChart.setNoDataText("Нет данных");
            progressChart.invalidate();
            return;
        }

        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        // Сортируем по дате
        List<BodyMeasurementResponse> sorted = new ArrayList<>(measurements);
        sorted.sort((a, b) -> {
            if (a.getMeasuredAt() == null) return 1;
            if (b.getMeasuredAt() == null) return -1;
            return a.getMeasuredAt().compareTo(b.getMeasuredAt());
        });

        for (int i = 0; i < sorted.size(); i++) {
            BodyMeasurementResponse m = sorted.get(i);
            if (m.getValue() != null) {
                entries.add(new Entry(i, m.getValue().floatValue()));
                if (m.getMeasuredAt() != null) {
                    try {
                        Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                            .parse(m.getMeasuredAt());
                        labels.add(displayFormat.format(date));
                    } catch (Exception e) {
                        labels.add("");
                    }
                } else {
                    labels.add("");
                }
            }
        }

        LineDataSet dataSet = new LineDataSet(entries, getMeasurementTypeName(currentMeasurementType));
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS[0]);
        dataSet.setCircleColors(ColorTemplate.MATERIAL_COLORS[0]);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(true);
        dataSet.setValueTextSize(10f);

        LineData data = new LineData(dataSet);
        progressChart.setData(data);
        progressChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        progressChart.invalidate();
    }

    private void showAddMeasurementDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_measurement, null);
        EditText valueInput = dialogView.findViewById(R.id.valueInput);
        EditText notesInput = dialogView.findViewById(R.id.notesInput);

        new AlertDialog.Builder(this)
            .setTitle("Добавить измерение")
            .setView(dialogView)
            .setPositiveButton("Сохранить", (dialog, which) -> {
                String valueStr = valueInput.getText().toString().trim();
                if (valueStr.isEmpty()) {
                    Toast.makeText(this, "Введите значение", Toast.LENGTH_SHORT).show();
                    return;
                }

                double value = Double.parseDouble(valueStr);
                String notes = notesInput.getText().toString().trim();
                String unit = getUnitForType(currentMeasurementType);
                
                String now = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    .format(new Date());

                BodyMeasurementResponse request = new BodyMeasurementResponse();
                request.setMeasurementType(currentMeasurementType);
                request.setValue(value);
                request.setValueUnit(unit);
                request.setMeasuredAt(now);
                request.setNotes(notes.isEmpty() ? null : notes);

                statsRepository.createBodyMeasurement(request, new StatsRepository.BodyMeasurementCallback() {
                    @Override
                    public void onSuccess(BodyMeasurementResponse measurement) {
                        Toast.makeText(BodyMeasurementsActivity.this, 
                            "Измерение добавлено", Toast.LENGTH_SHORT).show();
                        loadData();
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(BodyMeasurementsActivity.this, 
                            "Ошибка: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .setNegativeButton("Отмена", null)
            .show();
    }

    private void showEditMeasurementDialog(BodyMeasurementResponse measurement) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_measurement, null);
        EditText valueInput = dialogView.findViewById(R.id.valueInput);
        EditText notesInput = dialogView.findViewById(R.id.notesInput);

        valueInput.setText(String.valueOf(measurement.getValue()));
        if (measurement.getNotes() != null) {
            notesInput.setText(measurement.getNotes());
        }

        new AlertDialog.Builder(this)
            .setTitle("Редактировать измерение")
            .setView(dialogView)
            .setPositiveButton("Сохранить", (dialog, which) -> {
                String valueStr = valueInput.getText().toString().trim();
                if (valueStr.isEmpty()) {
                    Toast.makeText(this, "Введите значение", Toast.LENGTH_SHORT).show();
                    return;
                }

                double value = Double.parseDouble(valueStr);
                String notes = notesInput.getText().toString().trim();
                
                measurement.setValue(value);
                measurement.setNotes(notes.isEmpty() ? null : notes);

                statsRepository.updateBodyMeasurement(measurement.getId(), measurement, 
                    new StatsRepository.BodyMeasurementCallback() {
                        @Override
                        public void onSuccess(BodyMeasurementResponse m) {
                            Toast.makeText(BodyMeasurementsActivity.this, 
                                "Измерение обновлено", Toast.LENGTH_SHORT).show();
                            loadData();
                        }

                        @Override
                        public void onError(String error) {
                            Toast.makeText(BodyMeasurementsActivity.this, 
                                "Ошибка: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
            })
            .setNegativeButton("Отмена", null)
            .show();
    }

    private void showDeleteConfirmation(BodyMeasurementResponse measurement) {
        new AlertDialog.Builder(this)
            .setTitle("Удалить измерение?")
            .setMessage("Это действие нельзя отменить")
            .setPositiveButton("Удалить", (dialog, which) -> {
                statsRepository.deleteBodyMeasurement(measurement.getId(), new StatsRepository.SimpleCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(BodyMeasurementsActivity.this, 
                            "Измерение удалено", Toast.LENGTH_SHORT).show();
                        loadData();
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(BodyMeasurementsActivity.this, 
                            "Ошибка: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .setNegativeButton("Отмена", null)
            .show();
    }

    private String getMeasurementTypeName(String type) {
        for (int i = 0; i < measurementTypes.length; i++) {
            if (measurementTypes[i].equals(type)) {
                return measurementTypeNames[i];
            }
        }
        return type;
    }

    private String getUnitForType(String type) {
        if (BodyMeasurementResponse.BODY_WEIGHT.equals(type)) {
            return "KG";
        } else if (BodyMeasurementResponse.BODY_FAT_PERCENTAGE.equals(type)) {
            return "PERCENT";
        }
        return "CM";
    }

    private String getUnitDisplay(String type) {
        if (BodyMeasurementResponse.BODY_WEIGHT.equals(type)) {
            return "кг";
        } else if (BodyMeasurementResponse.BODY_FAT_PERCENTAGE.equals(type)) {
            return "%";
        }
        return "см";
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
