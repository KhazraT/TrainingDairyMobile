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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

/**
 * Fragment for displaying history and statistics of anti-G breathing training.
 */
public class AntiGHistoryFragment extends Fragment {

    // UI elements
    private TextView statsSummaryTextView;
    private RecyclerView sessionsRecyclerView;
    private LinearLayout barChartContainer;

    // Dependencies
    private AntiGSessionRepository antiGSessionRepository;
    private long userId;

    // Data
    private List<AntiGSessionResponse> sessions;
    private AntiGSessionAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize repository and user ID
        antiGSessionRepository = AntiGSessionRepository.getInstance();
        userId = PreferencesManager.getInstance().getUserId();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_anti_g_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupRecyclerView();
        loadData();
    }

    private void initViews(View view) {
        statsSummaryTextView = view.findViewById(R.id.statsSummaryTextView);
        sessionsRecyclerView = view.findViewById(R.id.sessionsRecyclerView);
        barChartContainer = view.findViewById(R.id.barChartContainer);
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
        sessionsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        sessionsRecyclerView.setAdapter(adapter);
    }

    private void loadData() {
        // Load sessions from backend via repository
        antiGSessionRepository.getAllSessions(new AntiGSessionRepository.AntiGSessionListCallback() {
            @Override
            public void onSuccess(List<AntiGSessionResponse> sessionsFromServer) {
                requireActivity().runOnUiThread(() -> {
                    sessions.clear();
                    sessions.addAll(sessionsFromServer);
                    adapter.setSessions(sessions);
                    updateStatistics();
                    drawBarChart();
                });
            }

            @Override
            public void onError(String error) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Ошибка загрузки истории: " + error, Toast.LENGTH_LONG).show();
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
        barChartContainer.removeAllViews();
        if (sessions == null || sessions.isEmpty()) {
            return;
        }

        // We'll show the last 7 days
        Calendar calendar = Calendar.getInstance();
        Map<Long, Integer> dateToCycles = new HashMap<>();
        for (AntiGSessionResponse session : sessions) {
            OffsetDateTime offsetDateTime = session.getDate();
            // Normalize to start of day
            long dayStart = offsetDateTime
                    .atZoneSameInstant(ZoneId.systemDefault())
                    .toLocalDate()
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli();
            dateToCycles.merge(dayStart, session.getAmount(), Integer::sum);
        }

        // Get the last 7 days including today
        List<Long> last7Days = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            Calendar day = Calendar.getInstance();
            day.add(Calendar.DAY_OF_MONTH, -i);
            day.set(Calendar.HOUR_OF_DAY, 0);
            day.set(Calendar.MINUTE, 0);
            day.set(Calendar.SECOND, 0);
            day.set(Calendar.MILLISECOND, 0);
            last7Days.add(day.getTimeInMillis());
        }

        // Find max cycles for scaling
        int maxCycles = 0;
        for (Long day : last7Days) {
            int cycles = dateToCycles.getOrDefault(day, 0);
            if (cycles > maxCycles) {
                maxCycles = cycles;
            }
        }
        if (maxCycles == 0) {
            maxCycles = 1; // avoid division by zero
        }

        // Create a bar for each day
        int barWidth = 30; // dp
        int spacing = 8; // dp
        int maxBarHeight = 150; // max height of the bar in dp

        float scale = requireContext().getResources().getDisplayMetrics().density;
        int barWidthPx = (int) (barWidth * scale + 0.5f);
        int barHeightPx = (int) (maxBarHeight * scale + 0.5f);
        int spacingPx = (int) (spacing * scale + 0.5f);

        for (int i = 0; i < last7Days.size(); i++) {
            Long day = last7Days.get(i);
            int cycles = dateToCycles.getOrDefault(day, 0);

            // Calculate bar height
            int barHeight = (int) (((double) cycles / maxCycles) * maxBarHeight);
            if (barHeight == 0 && cycles > 0) {
                barHeight = 1; // at least 1px if there is any value
            }

            // Create a vertical LinearLayout for the bar and date
            LinearLayout barContainer = new LinearLayout(requireContext());
            barContainer.setOrientation(LinearLayout.VERTICAL);
            barContainer.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);

            // Bar view
            View barView = new View(requireContext());
            int barWidthPxFinal = (int) (barWidth * scale + 0.5f);
            int barHeightPxFinal = (int) (barHeight * scale + 0.5f);
            barView.setLayoutParams(new LinearLayout.LayoutParams(barWidthPxFinal, barHeightPxFinal));
            barView.setBackgroundColor(Color.parseColor("#4CAF50")); // green color

            // Date label
            TextView dateLabel = new TextView(requireContext());
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM", Locale.getDefault());
            String dateString = sdf.format(new Date(day * 1000L)); // Convert milliseconds to Date
            dateLabel.setText(dateString);
            dateLabel.setTextSize(10);
            dateLabel.setTextColor(Color.GRAY);

            barContainer.addView(barView);
            barContainer.addView(dateLabel);

            // Add to container with spacing
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            if (i < last7Days.size() - 1) {
                params.rightMargin = spacingPx;
            }
            barChartContainer.addView(barContainer, params);
        }
    }

    private void deleteSession(AntiGSessionResponse session) {
        antiGSessionRepository.deleteSession(session.getId(), new AntiGSessionRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                requireActivity().runOnUiThread(() -> {
                    loadData(); // reload to reflect the deletion
                    Toast.makeText(requireContext(), "Запись удалена", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String error) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Ошибка удаления: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}