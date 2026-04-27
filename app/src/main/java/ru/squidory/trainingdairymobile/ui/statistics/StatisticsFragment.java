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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
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
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import ru.squidory.trainingdairymobile.R;
import ru.squidory.trainingdairymobile.data.model.DurationWeeklyStats;
import ru.squidory.trainingdairymobile.data.model.DurationMonthlyStats;
import ru.squidory.trainingdairymobile.data.model.MonthlyVolumeResponse;
import ru.squidory.trainingdairymobile.data.model.MuscleSetsStats;
import ru.squidory.trainingdairymobile.data.model.MuscleStatsResponse;
import ru.squidory.trainingdairymobile.data.model.StatsSummaryResponse;
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
            reloadChartsForPeriod(PERIOD_WEEKLY);
        });
        chipMonthly.setOnClickListener(v -> {
            chipGroupPeriod.check(R.id.chip_monthly);
            reloadChartsForPeriod(PERIOD_MONTHLY);
        });
        chipYearly.setOnClickListener(v -> {
            chipGroupPeriod.check(R.id.chip_yearly);
            reloadChartsForPeriod(PERIOD_YEARLY);
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

    private void reloadChartsForPeriod(int period) {
        loadDurationData(period);
        loadWorkoutData(period);
    }

    private void loadDurationData(int period) {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        if (period == PERIOD_WEEKLY) {
            statsRepository.getDurationWeeklyStats(currentYear, new StatsRepository.DurationWeeklyCallback() {
                @Override
                public void onSuccess(List<DurationWeeklyStats> stats) {
                    displayDurationChart(stats);
                }
                @Override
                public void onError(String error) {
                    durationChart.setNoDataText("Нет данных");
                    durationChart.invalidate();
                    Toast.makeText(getContext(), "Ошибка загрузки: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            statsRepository.getDurationMonthlyStats(currentYear, new StatsRepository.DurationMonthlyCallback() {
                @Override
                public void onSuccess(List<DurationMonthlyStats> stats) {
                    displayDurationChart(stats);
                }
                @Override
                public void onError(String error) {
                    durationChart.setNoDataText("Нет данных");
                    durationChart.invalidate();
                    Toast.makeText(getContext(), "Ошибка загрузки: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadWorkoutData(int period) {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        if (period == PERIOD_WEEKLY) {
            statsRepository.getWorkoutWeeklyStats(currentYear, new StatsRepository.WorkoutWeeklyCallback() {
                @Override
                public void onSuccess(List<WorkoutWeeklyStats> stats) {
                    displayWorkoutCountChart(stats);
                }
                @Override
                public void onError(String error) {
                    workoutCountChart.setNoDataText("Нет данных");
                    workoutCountChart.invalidate();
                    Toast.makeText(getContext(), "Ошибка загрузки: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        } else if (period == PERIOD_MONTHLY) {
            statsRepository.getWorkoutMonthlyStats(currentYear, new StatsRepository.WorkoutMonthlyCallback() {
                @Override
                public void onSuccess(List<WorkoutMonthlyStats> stats) {
                    List<WorkoutWeeklyStats> converted = new ArrayList<>();
                    for (WorkoutMonthlyStats m : stats) {
                        WorkoutWeeklyStats w = new WorkoutWeeklyStats();
                        w.setWeek(m.getMonth());
                        w.setWorkoutCount(m.getWorkoutCount());
                        converted.add(w);
                    }
                    displayWorkoutCountChart(converted);
                }
                @Override
                public void onError(String error) {
                    workoutCountChart.setNoDataText("Нет данных");
                    workoutCountChart.invalidate();
                    Toast.makeText(getContext(), "Ошибка загрузки: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Yearly - aggregate from monthly
            statsRepository.getWorkoutMonthlyStats(currentYear, new StatsRepository.WorkoutMonthlyCallback() {
                @Override
                public void onSuccess(List<WorkoutMonthlyStats> stats) {
                    List<WorkoutWeeklyStats> yearly = computeYearlyWorkoutStats(stats);
                    displayWorkoutCountChart(yearly);
                }
                @Override
                public void onError(String error) {
                    workoutCountChart.setNoDataText("Нет данных");
                    workoutCountChart.invalidate();
                    Toast.makeText(getContext(), "Ошибка загрузки: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadStatistics() {
        showLoading(true);
        loadingCounter = 6; // Number of async operations

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);

        // Summary
        statsRepository.getStatsSummary(new StatsRepository.StatsSummaryCallback() {
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

        // Monthly volume
        statsRepository.getMonthlyVolume(currentYear, new StatsRepository.MonthlyVolumeCallback() {
            @Override
            public void onSuccess(List<MonthlyVolumeResponse> volumes) {
                displayVolumeChart(volumes);
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

        // Muscle stats
        Calendar cal = Calendar.getInstance();
        String endDate = String.format(Locale.getDefault(), "%d-%02d-%02d",
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
        cal.add(Calendar.DAY_OF_YEAR, -90);
        String startDate = String.format(Locale.getDefault(), "%d-%02d-%02d",
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));

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

        // Determine period once using chipGroupPeriod
        int period = PERIOD_WEEKLY; // default
        int checkedChipId = chipGroupPeriod.getCheckedChipId();
        if (checkedChipId == R.id.chip_monthly) {
            period = PERIOD_MONTHLY;
        } else if (checkedChipId == R.id.chip_yearly) {
            period = PERIOD_YEARLY;
        }

        // Duration stats based on selected period
        // Yearly is not supported for duration chart, treat it as Monthly
        int durationPeriod = (period == PERIOD_YEARLY) ? PERIOD_MONTHLY : period;
        if (durationPeriod == PERIOD_WEEKLY) {
            statsRepository.getDurationWeeklyStats(currentYear, new StatsRepository.DurationWeeklyCallback() {
                @Override
                public void onSuccess(List<DurationWeeklyStats> stats) {
                    displayDurationChart(stats);
                    checkLoadingComplete();
                }

                @Override
                public void onError(String error) {
                    durationChart.setNoDataText("Нет данных");
                    durationChart.invalidate();
                    Toast.makeText(getContext(), "Ошибка загрузки: " + error, Toast.LENGTH_SHORT).show();
                    checkLoadingComplete();
                }
            });
        } else {
            statsRepository.getDurationMonthlyStats(currentYear, new StatsRepository.DurationMonthlyCallback() {
                @Override
                public void onSuccess(List<DurationMonthlyStats> stats) {
                    displayDurationChart(stats);
                    checkLoadingComplete();
                }

                @Override
                public void onError(String error) {
                    durationChart.setNoDataText("Нет данных");
                    durationChart.invalidate();
                    Toast.makeText(getContext(), "Ошибка загрузки: " + error, Toast.LENGTH_SHORT).show();
                    checkLoadingComplete();
                }
            });
        }

        // Workout count stats based on selected period
        if (period == PERIOD_WEEKLY) {
            statsRepository.getWorkoutWeeklyStats(currentYear, new StatsRepository.WorkoutWeeklyCallback() {
                @Override
                public void onSuccess(List<WorkoutWeeklyStats> stats) {
                    displayWorkoutCountChart(stats);
                    checkLoadingComplete();
                }

                @Override
                public void onError(String error) {
                    workoutCountChart.setNoDataText("Нет данных");
                    workoutCountChart.invalidate();
                    Toast.makeText(getContext(), "Ошибка загрузки: " + error, Toast.LENGTH_SHORT).show();
                    checkLoadingComplete();
                }
            });
        } else if (period == PERIOD_MONTHLY) {
            statsRepository.getWorkoutMonthlyStats(currentYear, new StatsRepository.WorkoutMonthlyCallback() {
                @Override
                public void onSuccess(List<WorkoutMonthlyStats> stats) {
                    displayWorkoutCountChart(stats);
                    checkLoadingComplete();
                }

                @Override
                public void onError(String error) {
                    workoutCountChart.setNoDataText("Нет данных");
                    workoutCountChart.invalidate();
                    Toast.makeText(getContext(), "Ошибка загрузки: " + error, Toast.LENGTH_SHORT).show();
                    checkLoadingComplete();
                }
            });
        } else {
            // Yearly - compute from monthly data
            statsRepository.getWorkoutMonthlyStats(currentYear, new StatsRepository.WorkoutMonthlyCallback() {
                @Override
                public void onSuccess(List<WorkoutMonthlyStats> monthlyStats) {
                    List<WorkoutWeeklyStats> yearlyStats = computeYearlyWorkoutStats(monthlyStats);
                    displayWorkoutCountChart(yearlyStats);
                    checkLoadingComplete();
                }

                @Override
                public void onError(String error) {
                    workoutCountChart.setNoDataText("Нет данных");
                    workoutCountChart.invalidate();
                    Toast.makeText(getContext(), "Ошибка загрузки: " + error, Toast.LENGTH_SHORT).show();
                    checkLoadingComplete();
                }
            });
        }
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

    private void checkLoadingComplete() {
        loadingCounter--;
        if (loadingCounter <= 0) {
            showLoading(false);
        }
    }

    private void displaySummary(StatsSummaryResponse summary) {
        if (summary == null) return;

        totalWorkoutsText.setText(String.valueOf(summary.getTotalWorkouts() != null ? summary.getTotalWorkouts() : 0));

        double totalVolume = summary.getTotalVolume() != null ? summary.getTotalVolume() : 0;
        totalVolumeText.setText(String.format(Locale.getDefault(), "%.1f т", totalVolume / 1000.0));

        totalSessionsText.setText(String.valueOf(summary.getTotalSessions() != null ? summary.getTotalSessions() : 0));

        int avgDuration = summary.getAverageSessionDuration() != null ? summary.getAverageSessionDuration() : 0;
        avgDurationText.setText(String.format(Locale.getDefault(), "%d мин", avgDuration / 60));

        if (summary.getFavoriteExercise() != null && !summary.getFavoriteExercise().isEmpty()) {
            favoriteExerciseText.setText("Любимое упражнение: " + summary.getFavoriteExercise());
            favoriteExerciseText.setVisibility(View.VISIBLE);
        } else {
            favoriteExerciseText.setVisibility(View.GONE);
        }
    }

    private void displayVolumeChart(List<MonthlyVolumeResponse> volumes) {
        if (volumes == null || volumes.isEmpty()) {
            volumeChart.setNoDataText("Нет данных");
            volumeChart.invalidate();
            return;
        }

        // Sort by month
        List<MonthlyVolumeResponse> sorted = new ArrayList<>(volumes);
        Collections.sort(sorted, (a, b) -> {
            int ma = a.getMonth() != null ? a.getMonth() : 0;
            int mb = b.getMonth() != null ? b.getMonth() : 0;
            return Integer.compare(ma, mb);
        });

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 0; i < sorted.size(); i++) {
            MonthlyVolumeResponse vol = sorted.get(i);
            float val = vol.getTotalVolume() != null ? (float)(vol.getTotalVolume() / 1000.0) : 0f;
            entries.add(new BarEntry(i, val));
            labels.add(vol.getMonth() != null ? String.valueOf(vol.getMonth()) : "M" + (i + 1));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Т (т)");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(10f);

        BarData data = new BarData(dataSet);
        volumeChart.setData(data);

        volumeChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        volumeChart.getAxisLeft().setAxisMinimum(0f);
        volumeChart.invalidate();
    }

    private void displayDurationChart(List<?> stats) {
        if (stats == null || stats.isEmpty()) {
            durationChart.setNoDataText("Нет данных");
            durationChart.invalidate();
            return;
        }

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        if (stats.get(0) instanceof DurationWeeklyStats) {
            // Weekly data
            List<DurationWeeklyStats> weeklyStats = (List<DurationWeeklyStats>) stats;
            Collections.sort(weeklyStats, (a, b) -> {
                int wa = a.getWeek() != null ? a.getWeek() : 0;
                int wb = b.getWeek() != null ? b.getWeek() : 0;
                return Integer.compare(wa, wb);
            });

            for (int i = 0; i < weeklyStats.size(); i++) {
                DurationWeeklyStats stat = weeklyStats.get(i);
                float val = stat.getTotalDurationMinutes() != null ? stat.getTotalDurationMinutes().floatValue() : 0f;
                entries.add(new BarEntry(i, val));
                labels.add("Нед. " + (stat.getWeek() != null ? stat.getWeek() : (i + 1)));
            }
        } else {
            // Monthly data - convert to weekly-like display
            List<DurationMonthlyStats> monthlyStats = (List<DurationMonthlyStats>) stats;
            Collections.sort(monthlyStats, (a, b) -> {
                int ma = a.getMonth() != null ? a.getMonth() : 0;
                int mb = b.getMonth() != null ? b.getMonth() : 0;
                return Integer.compare(ma, mb);
            });

            for (int i = 0; i < monthlyStats.size(); i++) {
                DurationMonthlyStats stat = monthlyStats.get(i);
                float val = stat.getTotalDurationMinutes() != null ? stat.getTotalDurationMinutes().floatValue() : 0f;
                entries.add(new BarEntry(i, val));
                labels.add("Месяц " + (stat.getMonth() != null ? stat.getMonth() : (i + 1)));
            }
        }

        // Set data
        BarDataSet dataSet = new BarDataSet(entries, stats.get(0) instanceof DurationWeeklyStats ? "Минут" : "Месяц");
        dataSet.setColors(stats.get(0) instanceof DurationWeeklyStats ? ColorTemplate.COLORFUL_COLORS : ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(10f);

        BarData data = new BarData(dataSet);
        durationChart.setData(data);

        durationChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        durationChart.getAxisLeft().setAxisMinimum(0f);
        durationChart.invalidate();
    }

    private void displaySetsChart(List<MuscleSetsStats> stats) {
        if (stats == null || stats.isEmpty()) {
            setsChart.setNoDataText("Нет данных");
            setsChart.invalidate();
            return;
        }

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 0; i < stats.size(); i++) {
            MuscleSetsStats stat = stats.get(i);
            float val = stat.getTotalSets() != null ? stat.getTotalSets().floatValue() : 0f;
            entries.add(new BarEntry(i, val));
            labels.add(stat.getMuscleGroupName() != null ? stat.getMuscleGroupName() : "Группа " + (i + 1));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Подходов");
        dataSet.setColors(ColorTemplate.VORDIPLOM_COLORS);
        dataSet.setValueTextSize(10f);

        BarData data = new BarData(dataSet);
        setsChart.setData(data);

        setsChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        setsChart.getAxisLeft().setAxisMinimum(0f);
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

    private void displayWorkoutCountChart(List<?> stats) {
        if (stats == null || stats.isEmpty()) {
            workoutCountChart.setNoDataText("Нет данных");
            workoutCountChart.invalidate();
            return;
        }

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        if (stats.get(0) instanceof WorkoutWeeklyStats) {
            // Weekly data
            List<WorkoutWeeklyStats> weeklyStats = (List<WorkoutWeeklyStats>) stats;
            Collections.sort(weeklyStats, (a, b) -> {
                int wa = a.getWeek() != null ? a.getWeek() : 0;
                int wb = b.getWeek() != null ? b.getWeek() : 0;
                return Integer.compare(wa, wb);
            });

            for (int i = 0; i < weeklyStats.size(); i++) {
                WorkoutWeeklyStats stat = weeklyStats.get(i);
                float val = stat.getWorkoutCount() != null ? stat.getWorkoutCount().floatValue() : 0f;
                entries.add(new BarEntry(i, val));
                labels.add("Нед. " + (stat.getWeek() != null ? stat.getWeek() : (i + 1)));
            }
        } else {
            // Yearly data - single bar
            WorkoutWeeklyStats yearlyStat = (WorkoutWeeklyStats) stats.get(0);
            float val = yearlyStat.getWorkoutCount() != null ? yearlyStat.getWorkoutCount().floatValue() : 0f;
            entries.add(new BarEntry(0, val));
            labels.add("Год");
        }

        BarDataSet dataSet = new BarDataSet(entries, stats.get(0) instanceof WorkoutWeeklyStats ? "Кол-во" : "Год");
        dataSet.setColor(stats.get(0) instanceof WorkoutWeeklyStats ? Color.rgb(98, 0, 238) : Color.rgb(98, 0, 238));
        dataSet.setValueTextSize(10f);

        BarData data = new BarData(dataSet);
        workoutCountChart.setData(data);

        workoutCountChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        workoutCountChart.getAxisLeft().setAxisMinimum(0f);
        workoutCountChart.invalidate();
    }
}
