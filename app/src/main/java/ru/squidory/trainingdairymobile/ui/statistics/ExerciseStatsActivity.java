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

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import ru.squidory.trainingdairymobile.R;
import ru.squidory.trainingdairymobile.data.model.ExerciseProgressResponse;
import ru.squidory.trainingdairymobile.data.model.ExerciseStatsResponse;
import ru.squidory.trainingdairymobile.data.repository.StatsRepository;

/**
 * Экран статистики по упражнениям: список упражнений с возможностью просмотра прогресса каждого.
 */
public class ExerciseStatsActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private MaterialCardView progressCard;
    private TextView selectedExerciseTitle;
    private ChipGroup periodChipGroup;
    private BarChart exerciseProgressChart;
    private RecyclerView exercisesRecyclerView;
    private TextView emptyText;
    private ProgressBar progressBar;

    private StatsRepository statsRepository;
    private ExerciseStatsAdapter adapter;

    private ExerciseStatsResponse selectedExercise;
    private String currentPeriodType = "MONTHLY";
    private int currentYear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_stats);

        statsRepository = StatsRepository.getInstance();
        currentYear = Calendar.getInstance().get(Calendar.YEAR);

        initViews();
        setupRecyclerView();
        setupChart();
        setupListeners();
        loadExerciseStats();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        progressCard = findViewById(R.id.progressCard);
        selectedExerciseTitle = findViewById(R.id.selectedExerciseTitle);
        periodChipGroup = findViewById(R.id.periodChipGroup);
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
            loadExerciseProgress();
        });
        exercisesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        exercisesRecyclerView.setAdapter(adapter);
    }

    private void setupChart() {
        exerciseProgressChart.getDescription().setEnabled(false);
        exerciseProgressChart.setDrawGridBackground(false);
        exerciseProgressChart.setDrawBarShadow(false);
        exerciseProgressChart.setHighlightFullBarEnabled(false);
        exerciseProgressChart.getLegend().setEnabled(false);

        XAxis xAxis = exerciseProgressChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);

        exerciseProgressChart.getAxisLeft().setDrawGridLines(true);
        exerciseProgressChart.getAxisRight().setEnabled(false);
    }

    private void setupListeners() {
        Chip chipWeekly = findViewById(R.id.chipWeekly);
        Chip chipMonthly = findViewById(R.id.chipMonthly);

        chipWeekly.setOnClickListener(v -> {
            periodChipGroup.check(R.id.chipWeekly);
            currentPeriodType = "WEEKLY";
            if (selectedExercise != null) {
                loadExerciseProgress();
            } else {
                Toast.makeText(this, "Сначала выберите упражнение", Toast.LENGTH_SHORT).show();
            }
        });

        chipMonthly.setOnClickListener(v -> {
            periodChipGroup.check(R.id.chipMonthly);
            currentPeriodType = "MONTHLY";
            if (selectedExercise != null) {
                loadExerciseProgress();
            } else {
                Toast.makeText(this, "Сначала выберите упражнение", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadExerciseStats() {
        showLoading(true);

        String startDate = currentYear + "-01-01";
        String endDate = currentYear + "-12-31";

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

    private void loadExerciseProgress() {
        if (selectedExercise == null || selectedExercise.getExerciseId() == null) return;

        statsRepository.getExerciseProgress(
                selectedExercise.getExerciseId(), currentPeriodType, currentYear,
                new StatsRepository.ExerciseProgressCallback() {
                    @Override
                    public void onSuccess(List<ExerciseProgressResponse> progress) {
                        displayProgressChart(progress);
                    }

                    @Override
                    public void onError(String error) {
                        exerciseProgressChart.setNoDataText("Нет данных");
                        exerciseProgressChart.invalidate();
                    }
                });
    }

    private void displayProgressChart(List<ExerciseProgressResponse> progress) {
        if (progress == null || progress.isEmpty()) {
            exerciseProgressChart.setNoDataText("Нет данных");
            exerciseProgressChart.invalidate();
            return;
        }

        // Sort by period
        List<ExerciseProgressResponse> sorted = new ArrayList<>(progress);
        Collections.sort(sorted, (a, b) -> {
            String pa = a.getPeriod() != null ? a.getPeriod() : "";
            String pb = b.getPeriod() != null ? b.getPeriod() : "";
            return pa.compareTo(pb);
        });

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 0; i < sorted.size(); i++) {
            ExerciseProgressResponse p = sorted.get(i);
            float val = p.getTotalVolume() != null ? (float)(p.getTotalVolume() / 1000.0) : 0f;
            entries.add(new BarEntry(i, val));
            labels.add(p.getPeriod() != null ? p.getPeriod() : "P" + (i + 1));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Т (т)");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(10f);

        BarData data = new BarData(dataSet);
        exerciseProgressChart.setData(data);
        exerciseProgressChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        exerciseProgressChart.getAxisLeft().setAxisMinimum(0f);
        exerciseProgressChart.invalidate();
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