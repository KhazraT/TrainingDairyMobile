package ru.squidory.trainingdairymobile.ui.trainings;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Gravity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.appbar.MaterialToolbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import ru.squidory.trainingdairymobile.R;
import ru.squidory.trainingdairymobile.data.local.PreferencesManager;
import ru.squidory.trainingdairymobile.data.model.AntiGSessionResponse;
import ru.squidory.trainingdairymobile.data.repository.AntiGSessionRepository;
import ru.squidory.trainingdairymobile.util.ThemeUtils;

/**
 * Activity for displaying history and statistics of anti-G breathing training.
 */
public class AntiGHistoryActivity extends AppCompatActivity {

    // UI elements
    private MaterialToolbar toolbar;
    private TextView statsSummaryTextView;
    private RecyclerView sessionsRecyclerView;
    private BarChart barChart;

    // Dependencies
    private AntiGSessionRepository antiGSessionRepository;
    private long userId;

    // Data
    private List<AntiGSessionResponse> sessions;
    private AntiGSessionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_anti_g_history);

        // Initialize repository and user ID
        antiGSessionRepository = AntiGSessionRepository.getInstance();
        userId = PreferencesManager.getInstance().getUserId();

        initViews();
        setupToolbar();
        setupRecyclerView();
        loadData();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        statsSummaryTextView = findViewById(R.id.statsSummaryTextView);
        sessionsRecyclerView = findViewById(R.id.sessionsRecyclerView);
        barChart = findViewById(R.id.barChart);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        sessions = new ArrayList<>();
        adapter = new AntiGSessionAdapter(sessions,
                session -> {
                    // Handle session click if needed
                },
                session -> {
                    // Handle delete click
                    deleteSession(session);
                });
        sessionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        sessionsRecyclerView.setAdapter(adapter);
    }

    private void loadData() {
        // Load sessions from backend via repository
        antiGSessionRepository.getAllSessions(new AntiGSessionRepository.AntiGSessionListCallback() {
            @Override
            public void onSuccess(List<AntiGSessionResponse> sessionsFromServer) {
                runOnUiThread(() -> {
                    sessions.clear();
                    sessions.addAll(sessionsFromServer);
                    adapter.setSessions(sessions);
                    updateStatistics();
                    drawBarChart();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(AntiGHistoryActivity.this, "Ошибка загрузки истории: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void updateStatistics() {
        if (sessions == null || sessions.isEmpty()) {
            statsSummaryTextView.setText("Нет данных");
            return;
        }

        int totalSessions = sessions.size();
        int totalCycles = 0;
        for (AntiGSessionResponse session : sessions) {
            totalCycles += session.getAmount();
        }
        double avgCycles = totalSessions > 0 ? (double) totalCycles / totalSessions : 0;

        String text = String.format(Locale.getDefault(),
                "Всего тренировок: %d\nВсего циклов: %d\nСреднее циклов за тренировку: %.1f",
                totalSessions, totalCycles, avgCycles);
        statsSummaryTextView.setText(text);
    }

    private void drawBarChart() {
        if (sessions == null || sessions.isEmpty()) {
            barChart.clear();
            barChart.setNoDataText("Нет данных");
            return;
        }

        int textColor = ThemeUtils.getChartTextColor(this);
        int axisTextColor = ThemeUtils.getChartAxisTextColor(this);

        Map<Long, Integer> dateToCycles = new HashMap<>();
        for (AntiGSessionResponse session : sessions) {
            OffsetDateTime offsetDateTime = session.getDate();
            long dayStart = offsetDateTime
                    .atZoneSameInstant(ZoneId.systemDefault())
                    .toLocalDate()
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli();
            dateToCycles.merge(dayStart, session.getAmount(), Integer::sum);
        }

        List<Long> last10Days = new ArrayList<>();
        for (int i = 9; i >= 0; i--) {
            Calendar day = Calendar.getInstance();
            day.add(Calendar.DAY_OF_MONTH, -i);
            day.set(Calendar.HOUR_OF_DAY, 0);
            day.set(Calendar.MINUTE, 0);
            day.set(Calendar.SECOND, 0);
            day.set(Calendar.MILLISECOND, 0);
            last10Days.add(day.getTimeInMillis());
        }

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM", Locale.getDefault());

        for (int i = 0; i < last10Days.size(); i++) {
            Long day = last10Days.get(i);
            int cycles = dateToCycles.getOrDefault(day, 0);
            entries.add(new BarEntry(i, cycles));
            labels.add(sdf.format(new Date(day)));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Циклы");
        dataSet.setColor(0xFF4CAF50);
        dataSet.setValueTextSize(9f);
        dataSet.setValueTextColor(textColor);
        dataSet.setDrawValues(true);
        dataSet.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.8f);

        barChart.setData(data);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(axisTextColor);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(7, true);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setDrawGridLines(false);

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setTextColor(axisTextColor);
        leftAxis.setGranularity(1f);
        leftAxis.setAxisMinimum(0f);
        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setEnabled(false);

        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.invalidate();
    }

    private void deleteSession(AntiGSessionResponse session) {
        antiGSessionRepository.deleteSession(session.getId(), new AntiGSessionRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    loadData(); // reload to reflect the deletion
                    Toast.makeText(AntiGHistoryActivity.this, "Запись удалена", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(AntiGHistoryActivity.this, "Ошибка удаления: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}