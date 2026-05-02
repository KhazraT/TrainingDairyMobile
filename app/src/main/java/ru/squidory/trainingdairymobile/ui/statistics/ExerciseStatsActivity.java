package ru.squidory.trainingdairymobile.ui.statistics;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.components.YAxis;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;

import ru.squidory.trainingdairymobile.R;
import ru.squidory.trainingdairymobile.data.model.ExerciseSessionsHistoryResponse;
import ru.squidory.trainingdairymobile.data.model.ExerciseStatsResponse;
import ru.squidory.trainingdairymobile.data.model.SessionSetHistoryResponse;
import ru.squidory.trainingdairymobile.data.repository.StatsRepository;
import ru.squidory.trainingdairymobile.util.Constants;
import timber.log.Timber;

/**
 * Экран статистики по упражнениям: список упражнений с возможностью просмотра прогресса.
 * Показывает комбинированный график (вес гистограммой, повторения/время/дистанция линией)
 * на основе истории всех сессий.
 */
public class ExerciseStatsActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private MaterialCardView progressCard;
    private TextView selectedExerciseTitle;
    private CombinedChart exerciseProgressChart;
    private RecyclerView exercisesRecyclerView;
    private TextView emptyText;
    private ProgressBar progressBar;

    private StatsRepository statsRepository;
    private ExerciseStatsAdapter adapter;

    private ExerciseStatsResponse selectedExercise;
    private String exerciseType; // Тип упражнения (REPS_WEIGHT, TIME_WEIGHT, и т.д.)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_stats);

        statsRepository = StatsRepository.getInstance();

        initViews();
        setupRecyclerView();
        setupChart();
        loadExerciseStats();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        progressCard = findViewById(R.id.progressCard);
        selectedExerciseTitle = findViewById(R.id.selectedExerciseTitle);
        exerciseProgressChart = findViewById(R.id.exerciseProgressChart);
        exercisesRecyclerView = findViewById(R.id.exercisesRecyclerView);
        emptyText = findViewById(R.id.emptyText);
        progressBar = findViewById(R.id.progressBar);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupRecyclerView() {
        adapter = new ExerciseStatsAdapter();
        adapter.setOnExerciseClickListener(exercise -> {
            selectedExercise = exercise;
            progressCard.setVisibility(View.VISIBLE);
            selectedExerciseTitle.setText(exercise.getExerciseName());
            // Получаем тип упражнения из списка (если есть)
            // В реальности тип нужно получать из детального запроса упражнения
            // Пока используем REPS_WEIGHT по умолчанию
            exerciseType = Constants.EXERCISE_TYPE_REPS_WEIGHT;
            loadExerciseSessionsHistory();
        });
        exercisesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        exercisesRecyclerView.setAdapter(adapter);
    }

    private void setupChart() {
        exerciseProgressChart.getDescription().setEnabled(false);
        exerciseProgressChart.setDrawGridBackground(false);

        // Настраиваем только горизонтальный скролл, отключаем масштабирование
        exerciseProgressChart.setTouchEnabled(true);
        exerciseProgressChart.setDragEnabled(true);
        exerciseProgressChart.setScaleEnabled(false);
        exerciseProgressChart.setPinchZoom(false);
        exerciseProgressChart.setDoubleTapToZoomEnabled(false);

        // Полностью отключаем подсветку (линии)
        exerciseProgressChart.setHighlightPerTapEnabled(false);
        exerciseProgressChart.setHighlightPerDragEnabled(false);
        exerciseProgressChart.setMaxHighlightDistance(Float.MAX_VALUE); // Невозможно подсветить
        exerciseProgressChart.setMarker(null);
        exerciseProgressChart.setHighlightFullBarEnabled(false);

        exerciseProgressChart.getLegend().setEnabled(true);

        XAxis xAxis = exerciseProgressChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);

        // Отступ сверху для свободного пространства
        exerciseProgressChart.setExtraTopOffset(40f);

        // Включаем правую ось Y для второго типа данных (дистанция/повторения)
        exerciseProgressChart.getAxisRight().setEnabled(true);
        exerciseProgressChart.getAxisRight().setDrawGridLines(false);
    }

    private void loadExerciseStats() {
        showLoading(true);

        // Получаем статистику за текущий год
        String startDate = "2020-01-01"; // За все время
        String endDate = "2030-12-31";

        statsRepository.getExerciseStats(startDate, endDate, new StatsRepository.ExerciseStatsCallback() {
            @Override
            public void onSuccess(List<ExerciseStatsResponse> exercises) {
                showLoading(false);
                adapter.setExercises(exercises);
                emptyText.setVisibility(
                        (exercises == null || exercises.isEmpty()) ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                emptyText.setVisibility(View.VISIBLE);
                Toast.makeText(ExerciseStatsActivity.this,
                        "Ошибка: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadExerciseSessionsHistory() {
        if (selectedExercise == null || selectedExercise.getExerciseId() == null) return;

        statsRepository.getExerciseSessionsHistory(
                selectedExercise.getExerciseId(),
                new StatsRepository.ExerciseSessionsHistoryCallback() {
                    @Override
                    public void onSuccess(List<ExerciseSessionsHistoryResponse> history) {
                        displayCombinedChart(history);
                    }

                    @Override
                    public void onError(String error) {
                        exerciseProgressChart.setNoDataText("Нет данных");
                        exerciseProgressChart.invalidate();
                        Timber.e("Error loading exercise sessions history: %s", error);
                    }
                });
    }

    private void displayCombinedChart(List<ExerciseSessionsHistoryResponse> history) {
        if (history == null || history.isEmpty()) {
            exerciseProgressChart.setNoDataText("Нет данных");
            exerciseProgressChart.invalidate();
            return;
        }

        // Собираем все подходы в один список в порядке выполнения
        List<SessionSetHistoryResponse> allSets = new ArrayList<>();
        List<String> xLabels = new ArrayList<>();

        for (ExerciseSessionsHistoryResponse session : history) {
            if (session.getSets() != null) {
                String sessionDateStr = session.getSessionDate();
                String dateLabel = "";
                if (sessionDateStr != null && !sessionDateStr.isEmpty()) {
                    try {
                        // Извлекаем дату (yyyy-MM-dd)
                        String datePart = sessionDateStr.length() >= 10 ? sessionDateStr.substring(0, 10) : sessionDateStr;
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                        Date date = sdf.parse(datePart);
                        SimpleDateFormat outFormat = new SimpleDateFormat("dd.MM", Locale.US);
                        dateLabel = outFormat.format(date);
                    } catch (Exception e) {
                        // fallback to first 10 chars
                        dateLabel = sessionDateStr.length() >= 10 ? sessionDateStr.substring(0, 10) : sessionDateStr;
                    }
                } else {
                    dateLabel = "???";
                }
                for (SessionSetHistoryResponse set : session.getSets()) {
                    allSets.add(set);
                    xLabels.add(dateLabel);
                }
            }
        }

        if (allSets.isEmpty()) {
            exerciseProgressChart.setNoDataText("Нет данных");
            exerciseProgressChart.invalidate();
            return;
        }

        // Определяем, нужна ли логарифмическая шкала (y-scale log)
        // Если разница между максимальным и минимальным значением слишком большая
        boolean useLogScale = shouldUseLogScale(allSets);

        // Настраиваем ось Y для логарифмического масштаба
        if (useLogScale) {
            exerciseProgressChart.getAxisLeft().setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    // value здесь — это log10(originalValue)
                    double original = Math.pow(10, value);
                    if (original >= 1000) {
                        return String.format(Locale.US, "%.1fK", original / 1000);
                    } else {
                        return String.format(Locale.US, "%.1f", original);
                    }
                }
            });
            // Правая ось Y тоже нуждается в форматтере, так как данные преобразованы в log10
            exerciseProgressChart.getAxisRight().setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    double original = Math.pow(10, value);
                    if (original >= 1000) {
                        return String.format(Locale.US, "%.1fK", original / 1000);
                    } else {
                        return String.format(Locale.US, "%.1f", original);
                    }
                }
            });
        } else {
            exerciseProgressChart.getAxisLeft().setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return String.format(Locale.US, "%.1f", value);
                }
            });
            exerciseProgressChart.getAxisRight().setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return String.format(Locale.US, "%.1f", value);
                }
            });
        }

        CombinedData combinedData = new CombinedData();

        // Определяем, какие графики строить в зависимости от типа упражнения
        if (Constants.EXERCISE_TYPE_REPS_WEIGHT.equalsIgnoreCase(exerciseType)) {
            addWeightBarData(combinedData, allSets, useLogScale);
            addRepsLineData(combinedData, allSets, useLogScale);
        } else if (Constants.EXERCISE_TYPE_TIME_WEIGHT.equalsIgnoreCase(exerciseType)) {
            addWeightBarData(combinedData, allSets, useLogScale);
            addDurationLineData(combinedData, allSets, useLogScale);
        } else if (Constants.EXERCISE_TYPE_TIME_DISTANCE.equalsIgnoreCase(exerciseType)) {
            addDurationBarData(combinedData, allSets, useLogScale);
            addDistanceLineData(combinedData, allSets, useLogScale);
        } else if (Constants.EXERCISE_TYPE_TIME_WEIGHT_DISTANCE.equalsIgnoreCase(exerciseType)) {
            addWeightBarData(combinedData, allSets, useLogScale);
            addDurationLineData(combinedData, allSets, useLogScale);
            addDistanceLineData(combinedData, allSets, useLogScale);
        } else {
            addWeightBarData(combinedData, allSets, useLogScale);
        }

        exerciseProgressChart.setData(combinedData);
        // Устанавливаем видимый диапазон ПОСЛЕ установки данных
        exerciseProgressChart.setVisibleXRangeMaximum(15f);
        exerciseProgressChart.setVisibleXRangeMinimum(5f);
        // Перемещаем вид к началу (последние данные)
        exerciseProgressChart.moveViewToX(0f);
        exerciseProgressChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xLabels));
        exerciseProgressChart.getXAxis().setLabelRotationAngle(-45f);
        exerciseProgressChart.invalidate();
    }

    /**
     * Проверяет, нужно ли использовать логарифмическую шкалу.
     * Если разница между yMax и yMin слишком большая (в 10 раз и более), возвращает true.
     */
    private boolean shouldUseLogScale(List<SessionSetHistoryResponse> allSets) {
        float minY = Float.MAX_VALUE;
        float maxY = Float.MIN_VALUE;

        for (SessionSetHistoryResponse set : allSets) {
            float val = 0f;
            // Выбираем значение, которое будет на главной оси Y (обычно вес или время)
            if (Constants.EXERCISE_TYPE_REPS_WEIGHT.equalsIgnoreCase(exerciseType) ||
                Constants.EXERCISE_TYPE_TIME_WEIGHT.equalsIgnoreCase(exerciseType) ||
                Constants.EXERCISE_TYPE_TIME_WEIGHT_DISTANCE.equalsIgnoreCase(exerciseType)) {
                val = set.getWeight() != null ? set.getWeight().floatValue() : 0f;
            } else if (Constants.EXERCISE_TYPE_TIME_DISTANCE.equalsIgnoreCase(exerciseType)) {
                val = set.getDurationSeconds() != null ? set.getDurationSeconds().floatValue() : 0f;
            }

            minY = Math.min(minY, val);
            maxY = Math.max(maxY, val);
        }

        // Если минимальное значение <= 0, не используем лог-скейл
        if (minY <= 0 || minY == Float.MAX_VALUE) {
            return false;
        }

        // Если разница более чем в 10 раз, включаем логарифмическую шкалу
        return (maxY / minY) > 10;
    }

    private void addWeightBarData(CombinedData combinedData, List<SessionSetHistoryResponse> allSets, boolean useLogScale) {
        List<BarEntry> barEntries = new ArrayList<>();

        for (int i = 0; i < allSets.size(); i++) {
            SessionSetHistoryResponse set = allSets.get(i);
            float weight = set.getWeight() != null ? set.getWeight().floatValue() : 0f;
            float yValue = useLogScale && weight > 0 ? (float) Math.log10(weight) : weight;
            BarEntry entry = new BarEntry(i, yValue);
            entry.setData(weight); // Сохраняем оригинальный вес
            barEntries.add(entry);
        }

        BarDataSet barDataSet = new BarDataSet(barEntries, "Вес (кг)");
        barDataSet.setColor(Color.parseColor("#6200EE")); // Фиолетовый
        barDataSet.setValueTextSize(9f);
        barDataSet.setDrawValues(true);
        barDataSet.setAxisDependency(YAxis.AxisDependency.LEFT); // Левая ось Y
        // Показываем оригинальное значение сверху (не логарифмическое)
        barDataSet.setValueFormatter(new ValueFormatter() {
            public String getFormattedValue(float value, com.github.mikephil.charting.data.Entry entry, int dataSetIndex, com.github.mikephil.charting.utils.ViewPortHandler viewPortHandler) {
                Object data = entry.getData();
                if (data instanceof Float) {
                    return String.format(Locale.US, "%.1f", (Float) data);
                }
                return String.format(Locale.US, "%.1f", value);
            }
        });

        BarData barData = new BarData(barDataSet);
        combinedData.setData(barData);
    }

    private void addRepsLineData(CombinedData combinedData, List<SessionSetHistoryResponse> allSets, boolean useLogScale) {
        List<Entry> lineEntries = new ArrayList<>();

        for (int i = 0; i < allSets.size(); i++) {
            SessionSetHistoryResponse set = allSets.get(i);
            if (set.getReps() != null) {
                float reps = set.getReps().floatValue();
                float yValue = useLogScale && reps > 0 ? (float) Math.log10(reps) : reps;
                Entry entry = new Entry(i, yValue);
                entry.setData(reps); // Сохраняем оригинальные повторения
                lineEntries.add(entry);
            }
        }

        LineDataSet lineDataSet = new LineDataSet(lineEntries, "Повторения");
        lineDataSet.setColor(Color.parseColor("#4CAF50")); // Зеленый
        lineDataSet.setCircleColor(Color.parseColor("#4CAF50"));
        lineDataSet.setLineWidth(2f);
        lineDataSet.setCircleRadius(3f);
        lineDataSet.setValueTextSize(10f);
        lineDataSet.setDrawValues(true);
        lineDataSet.setAxisDependency(YAxis.AxisDependency.RIGHT); // Правая ось Y для повторений
        lineDataSet.setValueFormatter(new ValueFormatter() {
            public String getFormattedValue(float value, com.github.mikephil.charting.data.Entry entry, int dataSetIndex, com.github.mikephil.charting.utils.ViewPortHandler viewPortHandler) {
                Object data = entry.getData();
                if (data instanceof Float) {
                    return String.format(Locale.US, "%.0f", (Float) data);
                }
                return String.format(Locale.US, "%.0f", value);
            }
        });

        LineData lineData = new LineData(lineDataSet);
        combinedData.setData(lineData);
    }

    private void addDurationLineData(CombinedData combinedData, List<SessionSetHistoryResponse> allSets, boolean useLogScale) {
        List<Entry> lineEntries = new ArrayList<>();

        for (int i = 0; i < allSets.size(); i++) {
            SessionSetHistoryResponse set = allSets.get(i);
            if (set.getDurationSeconds() != null) {
                float duration = set.getDurationSeconds().floatValue();
                float yValue = useLogScale && duration > 0 ? (float) Math.log10(duration) : duration;
                Entry entry = new Entry(i, yValue);
                entry.setData(duration); // Сохраняем оригинальное время
                lineEntries.add(entry);
            }
        }

        LineDataSet lineDataSet = new LineDataSet(lineEntries, "Время (сек)");
        lineDataSet.setColor(Color.parseColor("#FF9800")); // Оранжевый
        lineDataSet.setCircleColor(Color.parseColor("#FF9800"));
        lineDataSet.setLineWidth(2f);
        lineDataSet.setCircleRadius(3f);
        lineDataSet.setValueTextSize(10f);
        lineDataSet.setDrawValues(true);
        lineDataSet.setAxisDependency(YAxis.AxisDependency.RIGHT); // Правая ось Y для времени
        lineDataSet.setValueFormatter(new ValueFormatter() {
            public String getFormattedValue(float value, com.github.mikephil.charting.data.Entry entry, int dataSetIndex, com.github.mikephil.charting.utils.ViewPortHandler viewPortHandler) {
                Object data = entry.getData();
                if (data instanceof Float) {
                    return String.format(Locale.US, "%.0f", (Float) data);
                }
                return String.format(Locale.US, "%.0f", value);
            }
        });

        LineData lineData = new LineData(lineDataSet);
        combinedData.setData(lineData);
    }

    private void addDistanceLineData(CombinedData combinedData, List<SessionSetHistoryResponse> allSets, boolean useLogScale) {
        List<Entry> lineEntries = new ArrayList<>();

        for (int i = 0; i < allSets.size(); i++) {
            SessionSetHistoryResponse set = allSets.get(i);
            if (set.getDistanceMeters() != null) {
                float distance = set.getDistanceMeters().floatValue();
                float yValue = useLogScale && distance > 0 ? (float) Math.log10(distance) : distance;
                Entry entry = new Entry(i, yValue);
                entry.setData(distance); // Сохраняем оригинальную дистанцию
                lineEntries.add(entry);
            }
        }

        LineDataSet lineDataSet = new LineDataSet(lineEntries, "Дистанция (м)");
        lineDataSet.setColor(Color.parseColor("#9C27B0")); // Фиолетовый (другой оттенок)
        lineDataSet.setCircleColor(Color.parseColor("#9C27B0"));
        lineDataSet.setLineWidth(2f);
        lineDataSet.setCircleRadius(3f);
        lineDataSet.setValueTextSize(10f);
        lineDataSet.setDrawValues(true);
        lineDataSet.setAxisDependency(YAxis.AxisDependency.RIGHT); // Правая ось Y для дистанции
        lineDataSet.setValueFormatter(new ValueFormatter() {
            public String getFormattedValue(float value, com.github.mikephil.charting.data.Entry entry, int dataSetIndex, com.github.mikephil.charting.utils.ViewPortHandler viewPortHandler) {
                Object data = entry.getData();
                if (data instanceof Float) {
                    return String.format(Locale.US, "%.1f", (Float) data);
                }
                return String.format(Locale.US, "%.1f", value);
            }
        });

        LineData lineData = new LineData(lineDataSet);
        combinedData.setData(lineData);
    }

    private void addDurationBarData(CombinedData combinedData, List<SessionSetHistoryResponse> allSets, boolean useLogScale) {
        List<BarEntry> barEntries = new ArrayList<>();

        for (int i = 0; i < allSets.size(); i++) {
            SessionSetHistoryResponse set = allSets.get(i);
            float duration = set.getDurationSeconds() != null ? set.getDurationSeconds().floatValue() : 0f;
            float yValue = useLogScale && duration > 0 ? (float) Math.log10(duration) : duration;
            BarEntry entry = new BarEntry(i, yValue);
            entry.setData(duration); // Сохраняем оригинальное время
            barEntries.add(entry);
        }

        BarDataSet barDataSet = new BarDataSet(barEntries, "Время (сек)");
        barDataSet.setColor(Color.parseColor("#FF9800")); // Оранжевый
        barDataSet.setValueTextSize(9f);
        barDataSet.setDrawValues(true);
        barDataSet.setAxisDependency(YAxis.AxisDependency.LEFT); // Левая ось Y для времени
        barDataSet.setValueFormatter(new ValueFormatter() {
            public String getFormattedValue(float value, com.github.mikephil.charting.data.Entry entry, int dataSetIndex, com.github.mikephil.charting.utils.ViewPortHandler viewPortHandler) {
                Object data = entry.getData();
                if (data instanceof Float) {
                    return String.format(Locale.US, "%.0f", (Float) data);
                }
                return String.format(Locale.US, "%.0f", value);
            }
        });

        BarData barData = new BarData(barDataSet);
        combinedData.setData(barData);
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
