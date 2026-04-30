package ru.squidory.trainingdairymobile.ui.statistics;

import ru.squidory.trainingdairymobile.R;
import android.content.Intent;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.util.Pair;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ru.squidory.trainingdairymobile.R;
import ru.squidory.trainingdairymobile.data.model.DurationDailyStats;
import ru.squidory.trainingdairymobile.data.model.DurationMonthlyStats;
import ru.squidory.trainingdairymobile.data.model.DurationWeeklyStats;
import ru.squidory.trainingdairymobile.data.model.MonthlyVolumeResponse;
import ru.squidory.trainingdairymobile.data.model.MuscleSetsStats;
import ru.squidory.trainingdairymobile.data.model.MuscleStatsResponse;
import ru.squidory.trainingdairymobile.data.model.StatsSummaryResponse;
import ru.squidory.trainingdairymobile.data.model.VolumeDailyResponse;
import ru.squidory.trainingdairymobile.data.model.WorkoutDailyStats;
import ru.squidory.trainingdairymobile.data.model.WorkoutMonthlyStats;
import ru.squidory.trainingdairymobile.data.model.WorkoutWeeklyStats;
import ru.squidory.trainingdairymobile.data.repository.StatsRepository;

/**
 * Фрагмент раздела "Статистика".
 */
public class StatisticsFragment extends Fragment {

    private TextView totalWorkoutsText;
    private TextView totalVolumeText;
    private TextView totalSessionsText;
    private TextView avgDurationText;
    private TextView favoriteExerciseText;
    private BarChart volumeChart;
    private PieChart musclesPieChart;
    private BarChart workoutCountChart;
    private BarChart durationChart;
    private BarChart setsChart;
    private Button bodyMeasurementsButton;
    private Button exerciseStatsButton;
    private ProgressBar progressBar;
    
    // ChipGroup references for period selection
    private com.google.android.material.chip.ChipGroup chipGroupPeriod;

    // Period type constants
    private static final int PERIOD_WEEKLY = 0;
    private static final int PERIOD_MONTHLY = 1;
    private static final int PERIOD_YEARLY = 2;

    private int totalLoads = 6;

    private StatsRepository statsRepository;

    private int loadingCounter = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);
        statsRepository = StatsRepository.getInstance();

        initViews(view);
        loadStatistics();

        return view;
    }

    private void setupCharts() {
        // Configure Volume chart
        volumeChart.setDrawGridBackground(false);
        volumeChart.setDrawBarShadow(false);
        volumeChart.setHighlightFullBarEnabled(false);
        volumeChart.getLegend().setEnabled(false);
        XAxis volumeXAxis = volumeChart.getXAxis();
        volumeXAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        volumeXAxis.setDrawGridLines(false);
        volumeXAxis.setGranularity(1f);
        volumeChart.getAxisLeft().setDrawGridLines(true);
        volumeChart.getAxisRight().setEnabled(false);
        // Настройка горизонтальной прокрутки и масштабирования
        volumeChart.setDragEnabled(true);
        volumeChart.setScaleEnabled(true);
        volumeChart.setVisibleXRangeMaximum(7);

        // Configure Muscles pie chart
        musclesPieChart.getDescription().setEnabled(false);
        musclesPieChart.setUsePercentValues(true);
        musclesPieChart.setDrawHoleEnabled(true);
        musclesPieChart.setHoleColor(Color.TRANSPARENT);
        musclesPieChart.setTransparentCircleRadius(0f);
        musclesPieChart.getLegend().setEnabled(true);
        musclesPieChart.setEntryLabelColor(Color.BLACK);
        musclesPieChart.setEntryLabelTextSize(12f);

        // Configure Duration chart
        durationChart.getDescription().setEnabled(false);
        durationChart.setDrawGridBackground(false);
        durationChart.setDrawBarShadow(false);
        durationChart.setHighlightFullBarEnabled(false);
        durationChart.getLegend().setEnabled(false);
        XAxis durationXAxis = durationChart.getXAxis();
        durationXAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        durationXAxis.setDrawGridLines(false);
        durationXAxis.setGranularity(1f);
        durationChart.getAxisLeft().setDrawGridLines(true);
        durationChart.getAxisRight().setEnabled(false);
        // Настройка горизонтальной прокрутки и масштабирования
        durationChart.setDragEnabled(true);
        durationChart.setScaleEnabled(true);
        durationChart.setVisibleXRangeMaximum(7);

        // Configure Sets chart
        setsChart.getDescription().setEnabled(false);
        setsChart.setDrawGridBackground(false);
        setsChart.setDrawBarShadow(false);
        setsChart.setHighlightFullBarEnabled(false);
        setsChart.getLegend().setEnabled(false);
        XAxis setsXAxis = setsChart.getXAxis();
        setsXAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        setsXAxis.setDrawGridLines(false);
        setsXAxis.setGranularity(1f);
        setsChart.getAxisLeft().setDrawGridLines(true);
        setsChart.getAxisRight().setEnabled(false);
        // Настройка горизонтальной прокрутки и масштабирования
        setsChart.setDragEnabled(true);
        setsChart.setScaleEnabled(true);
        setsChart.setVisibleXRangeMaximum(7);

        // Configure Workout Count chart
        workoutCountChart.getDescription().setEnabled(false);
        workoutCountChart.setDrawGridBackground(false);
        workoutCountChart.setDrawBarShadow(false);
        workoutCountChart.setHighlightFullBarEnabled(false);
        workoutCountChart.getLegend().setEnabled(false);
        XAxis wcXAxis = workoutCountChart.getXAxis();
        wcXAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        wcXAxis.setDrawGridLines(false);
        wcXAxis.setGranularity(1f);
        workoutCountChart.getAxisLeft().setDrawGridLines(true);
        workoutCountChart.getAxisRight().setEnabled(false);
        // Настройка горизонтальной прокрутки и масштабирования
        workoutCountChart.setDragEnabled(true);
        workoutCountChart.setScaleEnabled(true);
        workoutCountChart.setVisibleXRangeMaximum(7);
    }

    private void initViews(View view) {
        totalWorkoutsText = view.findViewById(R.id.totalWorkoutsText);
        totalVolumeText = view.findViewById(R.id.totalVolumeText);
        totalSessionsText = view.findViewById(R.id.totalSessionsText);
        avgDurationText = view.findViewById(R.id.avgDurationText);
        favoriteExerciseText = view.findViewById(R.id.favoriteExerciseText);
        volumeChart = view.findViewById(R.id.volumeChart);
        musclesPieChart = view.findViewById(R.id.musclesPieChart);
        workoutCountChart = view.findViewById(R.id.workoutCountChart);
        durationChart = view.findViewById(R.id.durationChart);
        setsChart = view.findViewById(R.id.setsChart);
        bodyMeasurementsButton = view.findViewById(R.id.bodyMeasurementsButton);
        exerciseStatsButton = view.findViewById(R.id.exerciseStatsButton);
        progressBar = view.findViewById(R.id.progressBar);

        chipGroupPeriod = view.findViewById(R.id.chip_group_period);

        // Setup chip click listeners
        Chip chipWeekly = view.findViewById(R.id.chip_weekly);
        Chip chipMonthly = view.findViewById(R.id.chip_monthly);
        Chip chipYearly = view.findViewById(R.id.chip_yearly);

        chipWeekly.setOnClickListener(v -> {
            chipGroupPeriod.check(R.id.chip_weekly);
            reloadAllData(PERIOD_WEEKLY);
        });
        chipMonthly.setOnClickListener(v -> {
            chipGroupPeriod.check(R.id.chip_monthly);
            reloadAllData(PERIOD_MONTHLY);
        });
        chipYearly.setOnClickListener(v -> {
            chipGroupPeriod.check(R.id.chip_yearly);
            reloadAllData(PERIOD_YEARLY);
        });

        bodyMeasurementsButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), BodyMeasurementsActivity.class);
            startActivity(intent);
        });

        exerciseStatsButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ExerciseStatsActivity.class);
            startActivity(intent);
        });

        setupCharts();
    }

    private void loadDurationData(int period, String startDate, String endDate) {
        // Используем daily эндпоинт для всех периодов
        statsRepository.getDailyDuration(startDate, endDate, new StatsRepository.DurationDailyCallback() {
            @Override
            public void onSuccess(List<DurationDailyStats> dailyDurations) {
                displayDurationChart(dailyDurations, period, startDate, endDate);
                checkLoadingComplete();
            }
            @Override
            public void onError(String error) {
                durationChart.setNoDataText("Нет данных");
                durationChart.invalidate();
                Toast.makeText(getContext(), "Ошибка длительности: " + error, Toast.LENGTH_SHORT).show();
                checkLoadingComplete();
            }
        });
    }

    private void loadWorkoutData(int period, String startDate, String endDate) {
        // Используем daily эндпоинт для всех периодов
        statsRepository.getDailyWorkouts(startDate, endDate, new StatsRepository.WorkoutDailyCallback() {
            @Override
            public void onSuccess(List<WorkoutDailyStats> dailyWorkouts) {
                displayWorkoutCountChart(dailyWorkouts, period, startDate, endDate);
                checkLoadingComplete();
            }
            @Override
            public void onError(String error) {
                workoutCountChart.setNoDataText("Нет данных");
                workoutCountChart.invalidate();
                Toast.makeText(getContext(), "Ошибка тренировок: " + error, Toast.LENGTH_SHORT).show();
                checkLoadingComplete();
            }
        });
    }

    private void loadVolumeData(int period, String startDate, String endDate) {
        android.util.Log.d("StatsFragment", "loadVolumeData: period=" + period + ", startDate=" + startDate + ", endDate=" + endDate);
        // Используем daily эндпоинт для всех периодов (включая год)
        statsRepository.getDailyVolume(startDate, endDate, new StatsRepository.VolumeDailyCallback() {
            @Override
            public void onSuccess(List<VolumeDailyResponse> dailyVolumes) {
                android.util.Log.d("StatsFragment", "loadVolumeData onSuccess: dailyVolumes size=" + (dailyVolumes != null ? dailyVolumes.size() : 0));
                if (dailyVolumes != null) {
                    for (VolumeDailyResponse vol : dailyVolumes) {
                        android.util.Log.d("StatsFragment", "  date=" + vol.getDate() + ", volume=" + vol.getTotalVolume());
                    }
                }
                displayVolumeChart(dailyVolumes, period, startDate, endDate);
                checkLoadingComplete();
            }
            @Override
            public void onError(String error) {
                volumeChart.setNoDataText("Нет данных");
                volumeChart.invalidate();
                Toast.makeText(getContext(), "Ошибка загрузки тоннажа: " + error, Toast.LENGTH_SHORT).show();
                checkLoadingComplete();
            }
        });
    }

    private void loadMuscleStats(String startDate, String endDate) {
        statsRepository.getMuscleStats(startDate, endDate, new StatsRepository.MuscleStatsCallback() {
            @Override
            public void onSuccess(List<MuscleStatsResponse> muscles) {
                displayMusclesPieChart(muscles);
                checkLoadingComplete();
            }
            @Override
            public void onError(String error) {
                musclesPieChart.setNoDataText("Нет данных");
                musclesPieChart.invalidate();
                Toast.makeText(getContext(), "Ошибка загрузки мышц: " + error, Toast.LENGTH_SHORT).show();
                checkLoadingComplete();
            }
        });
    }

    private void loadSetsData(String startDate, String endDate) {
        statsRepository.getMuscleSetsStats(startDate, endDate, new StatsRepository.MuscleSetsCallback() {
            @Override
            public void onSuccess(List<MuscleSetsStats> stats) {
                displaySetsChart(stats);
                checkLoadingComplete();
            }

            @Override
            public void onError(String error) {
                setsChart.setNoDataText("Нет данных");
                setsChart.invalidate();
                Toast.makeText(getContext(), "Ошибка загрузки подходов: " + error, Toast.LENGTH_SHORT).show();
                checkLoadingComplete();
            }
        });
    }

    private void reloadAllData(int period) {
        showLoading(true);
        loadingCounter = 0;
        totalLoads = 6; // Now includes summary
        Pair<String, String> dates = getPeriodDates(period);
        String startDate = dates.first;
        String endDate = dates.second;

        // Load summary with period dates
        statsRepository.getStatsSummary(startDate, endDate, new StatsRepository.StatsSummaryCallback() {
            @Override
            public void onSuccess(StatsSummaryResponse summary) {
                displaySummary(summary);
                checkLoadingComplete();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Ошибка загрузки сводки: " + error, Toast.LENGTH_SHORT).show();
                checkLoadingComplete();
            }
        });

        // Количество асинхронных вызовов: 5 (тоннаж, длительность, тренировки, мышцы, подходы)
        loadVolumeData(period, startDate, endDate);
        loadDurationData(period, startDate, endDate);
        loadWorkoutData(period, startDate, endDate);
        loadMuscleStats(startDate, endDate);
        loadSetsData(startDate, endDate);
    }

    private void loadStatistics() {
        showLoading(true);
        loadingCounter = 0;
        totalLoads = 6; // Includes summary now

        // Determine period once using chipGroupPeriod
        int period = PERIOD_WEEKLY; // default
        int checkedChipId = chipGroupPeriod.getCheckedChipId();
        if (checkedChipId == R.id.chip_monthly) {
            period = PERIOD_MONTHLY;
        } else if (checkedChipId == R.id.chip_yearly) {
            period = PERIOD_YEARLY;
        }

        // Get period dates for all data
        Pair<String, String> dates = getPeriodDates(period);
        String startDate = dates.first;
        String endDate = dates.second;

        android.util.Log.d("StatsFragment", "loadStatistics: period=" + period + ", startDate=" + startDate + ", endDate=" + endDate);

        // Load summary with period dates
        statsRepository.getStatsSummary(startDate, endDate, new StatsRepository.StatsSummaryCallback() {
            @Override
            public void onSuccess(StatsSummaryResponse summary) {
                displaySummary(summary);
                checkLoadingComplete();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Ошибка загрузки сводки: " + error, Toast.LENGTH_SHORT).show();
                checkLoadingComplete();
            }
        });

        // Volume data - use new approach with daily data
        loadVolumeData(period, startDate, endDate);

        // Muscle stats and sets data using period dates
        loadMuscleStats(startDate, endDate);

        // Load sets data with period date range
        loadSetsData(startDate, endDate);

        // Duration stats based on selected period
        loadDurationData(period, startDate, endDate);

        // Workout count stats based on selected period
        loadWorkoutData(period, startDate, endDate);
    }

    private List<WorkoutWeeklyStats> computeYearlyWorkoutStats(List<WorkoutMonthlyStats> monthlyStats) {
        List<WorkoutWeeklyStats> yearlyStats = new ArrayList<>();
        int totalWorkouts = 0;
        int totalCompleted = 0;
        double totalCompletionRate = 0;

        for (WorkoutMonthlyStats month : monthlyStats) {
            if (month.getWorkoutCount() != null) {
                totalWorkouts += month.getWorkoutCount();
            }
            if (month.getCompletedCount() != null) {
                totalCompleted += month.getCompletedCount();
            }
            if (month.getCompletionRate() != null) {
                totalCompletionRate += month.getCompletionRate();
            }
        }

        WorkoutWeeklyStats yearlyStat = new WorkoutWeeklyStats();
        yearlyStat.setWeek(53); // Special value for yearly
        yearlyStat.setYear(Calendar.getInstance().get(Calendar.YEAR));
        yearlyStat.setWorkoutCount(totalWorkouts);
        yearlyStat.setCompletedCount(totalCompleted);
        yearlyStat.setCompletionRate(monthCount(monthlyStats) > 0 ? totalCompletionRate / monthCount(monthlyStats) : 0);
        yearlyStats.add(yearlyStat);
        return yearlyStats;
    }

    private int monthCount(List<WorkoutMonthlyStats> monthlyStats) {
        int count = 0;
        for (WorkoutMonthlyStats month : monthlyStats) {
            if (month.getWorkoutCount() != null) count++;
        }
        return count;
    }

    private Pair<String, String> getPeriodDates(int period) {
        Calendar cal = Calendar.getInstance();
        String startDate, endDate;

        if (period == PERIOD_WEEKLY) {
            // Полная неделя: понедельник текущей недели до воскресенья
            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            startDate = String.format(Locale.US, "%d-%02d-%02d",
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
            // Воскресенье (через 6 дней после понедельника)
            Calendar endCal = (Calendar) cal.clone();
            endCal.add(Calendar.DAY_OF_YEAR, 6);
            endDate = String.format(Locale.US, "%d-%02d-%02d",
                    endCal.get(Calendar.YEAR), endCal.get(Calendar.MONTH) + 1, endCal.get(Calendar.DAY_OF_MONTH));
        } else if (period == PERIOD_MONTHLY) {
            // Полный месяц: с 1-го по последний день текущего месяца
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            startDate = String.format(Locale.US, "%d-%02d-%02d",
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
            // Последний день текущего месяца
            Calendar endCal = (Calendar) cal.clone();
            endCal.add(Calendar.MONTH, 1);
            endCal.add(Calendar.DAY_OF_MONTH, -1);
            endDate = String.format(Locale.US, "%d-%02d-%02d",
                    endCal.get(Calendar.YEAR), endCal.get(Calendar.MONTH) + 1, endCal.get(Calendar.DAY_OF_MONTH));
        } else { // YEARLY
            // Полный год: с 1 января по 31 декабря текущего года
            cal.set(Calendar.DAY_OF_YEAR, 1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            startDate = String.format(Locale.US, "%d-%02d-%02d",
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
            // 31 декабря текущего года
            Calendar endCal = (Calendar) cal.clone();
            endCal.add(Calendar.YEAR, 1);
            endCal.add(Calendar.DAY_OF_YEAR, -1);
            endDate = String.format(Locale.US, "%d-%02d-%02d",
                    endCal.get(Calendar.YEAR), endCal.get(Calendar.MONTH) + 1, endCal.get(Calendar.DAY_OF_MONTH));
        }
        return new Pair<>(startDate, endDate);
    }

    private void checkLoadingComplete() {
        loadingCounter++;
        if (loadingCounter >= totalLoads) {
            showLoading(false);
        }
    }

    private void displaySummary(StatsSummaryResponse summary) {
        if (summary == null) return;

        totalWorkoutsText.setText(String.valueOf(summary.getTotalWorkouts() != null ? summary.getTotalWorkouts() : 0));

        double totalVolume = summary.getTotalVolume() != null ? summary.getTotalVolume() : 0;
        if (totalVolume < 1000) {
            totalVolumeText.setText(String.format(Locale.getDefault(), "%.0f кг", totalVolume));
        } else {
            totalVolumeText.setText(String.format(Locale.getDefault(), "%.1f т", totalVolume / 1000.0));
        }

        totalSessionsText.setText(String.valueOf(summary.getTotalSets() != null ? summary.getTotalSets() : 0));

        int avgDuration = summary.getAverageDurationMinutes() != null ? summary.getAverageDurationMinutes() : 0;
        avgDurationText.setText(String.format(Locale.getDefault(), "%d мин", avgDuration));

        if (summary.getFavoriteExercise() != null && !summary.getFavoriteExercise().isEmpty()) {
            favoriteExerciseText.setText("Любимое упражнение: " + summary.getFavoriteExercise());
            favoriteExerciseText.setVisibility(View.VISIBLE);
        } else {
            favoriteExerciseText.setVisibility(View.GONE);
        }
    }

    /**
     * Возвращает количество дней в месяце для указанного года и месяца (1-12)
     */
    private int getDaysInMonth(int year, int month) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month - 1); // Calendar месяцы 0-11
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    /**
     * Возвращает количество дней в году (365 или 366)
     */
    private int getDaysInYear(int year) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        return cal.getActualMaximum(Calendar.DAY_OF_YEAR);
    }

    /**
     * Определяет, нужно ли использовать логарифмическую шкалу.
     * Используется если отношение максимального значения к минимальному (положительному) больше threshold.
     * @param values массив значений
     * @param threshold порог (по умолчанию 50)
     * @return true если нужна логарифмическая шкала
     */
    private boolean shouldUseLogScale(float[] values, float threshold) {
        float minPositive = Float.MAX_VALUE;
        float max = 0f;
        for (float val : values) {
            if (val > 0) {
                minPositive = Math.min(minPositive, val);
                max = Math.max(max, val);
            }
        }
        if (max == 0 || minPositive == Float.MAX_VALUE) return false;
        return (max / minPositive) > threshold;
    }

    private boolean shouldUseLogScale(float[] values) {
        return shouldUseLogScale(values, 50f);
    }

    /**
     * Преобразует значение в логарифмическую шкалу: log10(val + 1)
     */
    private float toLogScale(float val) {
        return val > 0 ? (float) Math.log10(val + 1) : 0f;
    }

    /**
     * Обратное преобразование из логарифмической шкалы в исходное значение
     */
    private double fromLogScale(float logVal) {
        return Math.pow(10, logVal) - 1;
    }

    /**
     * Форматирует значение для отображения (добавляет K/M для больших чисел)
     */
    private String formatValue(double value) {
        if (value < 1) return "0";
        if (value < 1000) return String.format(Locale.US, "%.0f", value);
        else if (value < 1000000) return String.format(Locale.US, "%.1fK", value / 1000);
        else return String.format(Locale.US, "%.1fM", value / 1000000);
    }

    /**
     * Создает ValueFormatter для оси Y в зависимости от того, используется ли логарифмическая шкала
     */
    private ValueFormatter createAxisFormatter(boolean useLog) {
        if (useLog) {
            return new ValueFormatter() {
                @Override
                public String getAxisLabel(float value, AxisBase axis) {
                    if (value <= 0) return "0";
                    double original = fromLogScale(value);
                    return formatValue(original);
                }
            };
        } else {
            return new ValueFormatter() {
                @Override
                public String getAxisLabel(float value, AxisBase axis) {
                    return formatValue(value);
                }
            };
        }
    }

    /**
     * Создает ValueFormatter для отображения значений над столбцами
     * При логарифмической шкале преобразует обратно в исходное значение
     */
    private ValueFormatter createBarValueFormatter(boolean useLog) {
        if (useLog) {
            return new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    // value — это логарифмическое значение из BarEntry
                    double original = fromLogScale(value);
                    return formatValue(original);
                }
            };
        } else {
            return new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return formatValue(value);
                }
            };
        }
    }

    private void displayVolumeChart(List<VolumeDailyResponse> dailyVolumes, int period, String startDate, String endDate) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        // Создаем мапу дата -> объем
        Map<String, Float> dateToVolume = new HashMap<>();
        if (dailyVolumes != null) {
            for (VolumeDailyResponse vol : dailyVolumes) {
                if (vol.getDate() != null && vol.getTotalVolume() != null) {
                    dateToVolume.put(vol.getDate(), vol.getTotalVolume().floatValue());
                }
            }
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Calendar cal = Calendar.getInstance();
        float[] originalValues;

        if (period == PERIOD_WEEKLY) {
            String[] days = {"Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"};
            originalValues = new float[7];
            try {
                cal.setTime(sdf.parse(startDate));
                for (int i = 0; i < 7; i++) {
                    String dateStr = sdf.format(cal.getTime());
                    float val = dateToVolume.getOrDefault(dateStr, 0f);
                    originalValues[i] = val;
                    labels.add(days[i]);
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                }
            } catch (Exception e) {
                e.printStackTrace();
                originalValues = new float[7];
            }
        } else if (period == PERIOD_MONTHLY) {
            try {
                cal.setTime(sdf.parse(startDate));
                int daysInMonth = getDaysInMonth(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1);
                originalValues = new float[daysInMonth];
                for (int i = 0; i < daysInMonth; i++) {
                    String dateStr = sdf.format(cal.getTime());
                    float val = dateToVolume.getOrDefault(dateStr, 0f);
                    originalValues[i] = val;
                    labels.add(String.valueOf(i + 1));
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                }
            } catch (Exception e) {
                e.printStackTrace();
                originalValues = new float[30];
            }
        } else { // YEARLY
            originalValues = new float[12];
            if (dailyVolumes != null) {
                for (VolumeDailyResponse vol : dailyVolumes) {
                    if (vol.getDate() != null && vol.getTotalVolume() != null) {
                        try {
                            cal.setTime(sdf.parse(vol.getDate()));
                            int month = cal.get(Calendar.MONTH);
                            originalValues[month] += vol.getTotalVolume().floatValue();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            for (int i = 0; i < 12; i++) {
                labels.add(String.valueOf(i + 1));
            }
        }

        // Определяем, нужна ли логарифмическая шкала
        boolean useLog = shouldUseLogScale(originalValues);

        // Создаем записи для графика
        for (int i = 0; i < originalValues.length; i++) {
            float val = originalValues[i];
            float entryVal = useLog ? toLogScale(val) : val;
            entries.add(new BarEntry(i, entryVal));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Т (т)");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(10f);
        dataSet.setValueFormatter(createBarValueFormatter(useLog));
        dataSet.setDrawValues(true);

        BarData data = new BarData(dataSet);
        volumeChart.setData(data);

        volumeChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        volumeChart.getAxisLeft().setAxisMinimum(0f);
        volumeChart.getAxisLeft().setValueFormatter(createAxisFormatter(useLog));

        volumeChart.setVisibleXRangeMaximum(period == PERIOD_WEEKLY ? 7 : (period == PERIOD_MONTHLY ? 10 : 12));
        volumeChart.invalidate();
    }

    private int getCurrentPeriod() {
        int checkedChipId = chipGroupPeriod.getCheckedChipId();
        if (checkedChipId == R.id.chip_monthly) {
            return PERIOD_MONTHLY;
        } else if (checkedChipId == R.id.chip_yearly) {
            return PERIOD_YEARLY;
        }
        return PERIOD_WEEKLY;
    }

    private void displayDurationChart(List<DurationDailyStats> dailyDurations, int period, String startDate, String endDate) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        Map<String, Float> dateToDuration = new HashMap<>();
        if (dailyDurations != null) {
            for (DurationDailyStats stat : dailyDurations) {
                if (stat.getDate() != null && stat.getTotalDurationMinutes() != null) {
                    dateToDuration.put(stat.getDate(), stat.getTotalDurationMinutes().floatValue());
                }
            }
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Calendar cal = Calendar.getInstance();
        float[] originalValues;

        if (period == PERIOD_WEEKLY) {
            String[] days = {"Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"};
            originalValues = new float[7];
            try {
                cal.setTime(sdf.parse(startDate));
                for (int i = 0; i < 7; i++) {
                    String dateStr = sdf.format(cal.getTime());
                    float val = dateToDuration.getOrDefault(dateStr, 0f);
                    originalValues[i] = val;
                    labels.add(days[i]);
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                }
            } catch (Exception e) { e.printStackTrace(); originalValues = new float[7]; }
        } else if (period == PERIOD_MONTHLY) {
            try {
                cal.setTime(sdf.parse(startDate));
                int daysInMonth = getDaysInMonth(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1);
                originalValues = new float[daysInMonth];
                for (int i = 0; i < daysInMonth; i++) {
                    String dateStr = sdf.format(cal.getTime());
                    float val = dateToDuration.getOrDefault(dateStr, 0f);
                    originalValues[i] = val;
                    labels.add(String.valueOf(i + 1));
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                }
            } catch (Exception e) { e.printStackTrace(); originalValues = new float[30]; }
        } else { // YEARLY
            originalValues = new float[12];
            if (dailyDurations != null) {
                for (DurationDailyStats stat : dailyDurations) {
                    if (stat.getDate() != null && stat.getTotalDurationMinutes() != null) {
                        try {
                            cal.setTime(sdf.parse(stat.getDate()));
                            int month = cal.get(Calendar.MONTH);
                            originalValues[month] += stat.getTotalDurationMinutes().floatValue();
                        } catch (Exception e) { e.printStackTrace(); }
                    }
                }
            }
            for (int i = 0; i < 12; i++) {
                labels.add(String.valueOf(i + 1));
            }
        }

        // Определяем, нужна ли логарифмическая шкала
        boolean useLog = shouldUseLogScale(originalValues);

        // Создаем записи для графика
        for (int i = 0; i < originalValues.length; i++) {
            float val = originalValues[i];
            float entryVal = useLog ? toLogScale(val) : val;
            entries.add(new BarEntry(i, entryVal));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Мин");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(10f);
        dataSet.setValueFormatter(createBarValueFormatter(useLog));
        dataSet.setDrawValues(true);

        BarData data = new BarData(dataSet);
        durationChart.setData(data);
        durationChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        durationChart.getAxisLeft().setAxisMinimum(0f);
        durationChart.getAxisLeft().setValueFormatter(createAxisFormatter(useLog));

        durationChart.setVisibleXRangeMaximum(period == PERIOD_WEEKLY ? 7 : (period == PERIOD_MONTHLY ? 10 : 12));
        durationChart.invalidate();
    }

    private void displaySetsChart(List<MuscleSetsStats> stats) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        android.util.Log.d("StatsFragment", "displaySetsChart: stats size=" + (stats != null ? stats.size() : 0));
        if (stats != null) {
            for (MuscleSetsStats stat : stats) {
                android.util.Log.d("StatsFragment", "  Muscle: " + stat.getMuscleGroupName() + ", Sets: " + stat.getTotalSets());
            }
        }

        // Полный список групп мышц (из справочника)
        String[] allMuscleGroups = {
            "Грудь", "Спина", "Плечи", "Бицепс", "Трицепс", "Квадрицепс", "Бицепс бедра", "Ягодицы",
            "Икры", "Пресс", "Предплечье", "Трапеция", "Шея", "Голень", "Стопы", "Дельтовидные"
        };
        float[] values = new float[allMuscleGroups.length];

        // Заполняем из API, если есть (суммируем значения для одинаковых групп мышц)
        if (stats != null && !stats.isEmpty()) {
            for (MuscleSetsStats stat : stats) {
                String muscleName = stat.getMuscleGroupName();
                if (muscleName != null) {
                    for (int i = 0; i < allMuscleGroups.length; i++) {
                        if (allMuscleGroups[i].equalsIgnoreCase(muscleName)) {
                            float sets = stat.getTotalSets() != null ? stat.getTotalSets().floatValue() : 0f;
                            values[i] += sets; // Суммируем, а не перезаписываем
                            break;
                        }
                    }
                }
            }
        }

        // Логируем итоговые значения для проверки
        for (int i = 0; i < allMuscleGroups.length; i++) {
            if (values[i] > 0) {
                android.util.Log.d("StatsFragment", "  Final " + allMuscleGroups[i] + ": " + values[i] + " sets");
            }
        }

        // Определяем, нужна ли логарифмическая шкала
        boolean useLog = shouldUseLogScale(values);

        // Создаем записи для графика
        for (int i = 0; i < allMuscleGroups.length; i++) {
            float val = values[i];
            float entryVal = useLog ? toLogScale(val) : val;
            entries.add(new BarEntry(i, entryVal));
            labels.add(allMuscleGroups[i]);
        }

        BarDataSet dataSet = new BarDataSet(entries, "Подходов");
        dataSet.setColors(ColorTemplate.VORDIPLOM_COLORS);
        dataSet.setValueTextSize(10f);
        dataSet.setValueFormatter(createBarValueFormatter(useLog));
        dataSet.setDrawValues(true);

        BarData data = new BarData(dataSet);
        setsChart.setData(data);

        setsChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        setsChart.getAxisLeft().setAxisMinimum(0f);
        setsChart.getAxisLeft().setValueFormatter(createAxisFormatter(useLog));

        setsChart.setVisibleXRangeMaximum(7); // Показываем 7 групп мышц за раз, прокрутка
        setsChart.invalidate();
    }

    private void displayMusclesPieChart(List<MuscleStatsResponse> muscles) {
        if (muscles == null || muscles.isEmpty()) {
            musclesPieChart.setNoDataText("Нет данных");
            musclesPieChart.invalidate();
            return;
        }

        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        int[] chartColors = {
            Color.rgb(98, 0, 238), Color.rgb(0, 184, 212), Color.rgb(3, 218, 198),
            Color.rgb(255, 171, 64), Color.rgb(255, 87, 34), Color.rgb(76, 175, 80),
            Color.rgb(156, 39, 176), Color.rgb(255, 193, 7), Color.rgb(96, 125, 139),
            Color.rgb(121, 134, 203), Color.rgb(0, 150, 136), Color.rgb(233, 30, 99),
        };

        for (int i = 0; i < muscles.size(); i++) {
            MuscleStatsResponse m = muscles.get(i);
            float vol = m.getTotalVolume() != null ? m.getTotalVolume().floatValue() : 0f;
            if (vol > 0) {
                entries.add(new PieEntry(vol, m.getMuscleGroupName()));
                colors.add(chartColors[i % chartColors.length]);
            }
        }

        if (entries.isEmpty()) {
            musclesPieChart.setNoDataText("Нет данных");
            musclesPieChart.invalidate();
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(11f);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setSliceSpace(2f);
        dataSet.setValueFormatter(new PercentFormatter(musclesPieChart));

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(musclesPieChart));

        musclesPieChart.setData(data);
        musclesPieChart.setCenterText("Распределение\nтоннажа");
        musclesPieChart.setCenterTextSize(14f);
        musclesPieChart.setCenterTextColor(Color.DKGRAY);

        Legend legend = musclesPieChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setTextSize(11f);

        musclesPieChart.invalidate();
    }


    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void displayWorkoutCountChart(List<WorkoutDailyStats> dailyWorkouts, int period, String startDate, String endDate) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        Map<String, Float> dateToCount = new HashMap<>();
        if (dailyWorkouts != null) {
            for (WorkoutDailyStats stat : dailyWorkouts) {
                if (stat.getDate() != null && stat.getWorkoutCount() != null) {
                    dateToCount.put(stat.getDate(), stat.getWorkoutCount().floatValue());
                }
            }
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Calendar cal = Calendar.getInstance();

        if (period == PERIOD_WEEKLY) {
            String[] days = {"Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"};
            try {
                cal.setTime(sdf.parse(startDate));
                for (int i = 0; i < 7; i++) {
                    String dateStr = sdf.format(cal.getTime());
                    float val = dateToCount.getOrDefault(dateStr, 0f);
                    entries.add(new BarEntry(i, val));
                    labels.add(days[i]);
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                }
            } catch (Exception e) { e.printStackTrace(); }
        } else if (period == PERIOD_MONTHLY) {
            try {
                cal.setTime(sdf.parse(startDate));
                int daysInMonth = getDaysInMonth(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1);
                for (int i = 0; i < daysInMonth; i++) {
                    String dateStr = sdf.format(cal.getTime());
                    float val = dateToCount.getOrDefault(dateStr, 0f);
                    entries.add(new BarEntry(i, val));
                    labels.add(String.valueOf(i + 1));
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                }
            } catch (Exception e) { e.printStackTrace(); }
        } else { // YEARLY
            float[] monthValues = new float[12];
            if (dailyWorkouts != null) {
                for (WorkoutDailyStats stat : dailyWorkouts) {
                    if (stat.getDate() != null && stat.getWorkoutCount() != null) {
                        try {
                            cal.setTime(sdf.parse(stat.getDate()));
                            int month = cal.get(Calendar.MONTH);
                            monthValues[month] += stat.getWorkoutCount().floatValue();
                        } catch (Exception e) { e.printStackTrace(); }
                    }
                }
            }
            for (int i = 0; i < 12; i++) {
                entries.add(new BarEntry(i, monthValues[i]));
                labels.add(String.valueOf(i + 1));
            }
        }

        BarDataSet dataSet = new BarDataSet(entries, "Кол-во");
        dataSet.setColor(Color.rgb(98, 0, 238));
        dataSet.setValueTextSize(10f);

        BarData data = new BarData(dataSet);
        workoutCountChart.setData(data);
        workoutCountChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        workoutCountChart.getAxisLeft().setAxisMinimum(0f);
        workoutCountChart.setVisibleXRangeMaximum(period == PERIOD_WEEKLY ? 7 : (period == PERIOD_MONTHLY ? 10 : 12));
        workoutCountChart.invalidate();
    }
}
