package ru.squidory.trainingdairymobile.ui.trainings;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import ru.squidory.trainingdairymobile.R;
import ru.squidory.trainingdairymobile.data.model.SessionHistoryResponse;
import ru.squidory.trainingdairymobile.data.repository.SessionRepository;

/**
 * Экран истории тренировок.
 * Отображает завершенные сессии, сгруппированные по дням.
 */
public class SessionHistoryActivity extends AppCompatActivity implements SessionHistoryAdapter.OnSessionClickListener {

    private MaterialToolbar toolbar;
    private RecyclerView historyRecyclerView;
    private ProgressBar progressBar;
    private TextView emptyText;

    private SessionHistoryAdapter adapter;
    private SessionRepository repository;
    private List<SessionHistoryResponse.DaySessions> daySessionsList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_history);

        repository = SessionRepository.getInstance();

        initViews();
        setupToolbar();
        setupRecyclerView();
        loadHistory();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        historyRecyclerView = findViewById(R.id.historyRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyText = findViewById(R.id.emptyText);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("История тренировок");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new SessionHistoryAdapter();
        adapter.setOnSessionClickListener(this);
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        historyRecyclerView.setAdapter(adapter);
    }

    private void loadHistory() {
        showLoading(true);

        // Загружаем историю за текущий год БЕЗ указания месяца (бэкенд вернёт все сессии за год)
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        Integer month = null; // не передаём месяц, чтобы получить все сессии за год

        Log.d("SessionHistory", "Запрос истории: year=" + year + ", month=null, page=0, size=100");

        repository.getSessionHistory(year, month, 0, 100, new SessionRepository.SessionHistoryCallback() {
            @Override
            public void onSuccess(SessionHistoryResponse history) {
                showLoading(false);
                Log.d("SessionHistory", "Получено дней: " + (history.getSessionHistory() != null ? history.getSessionHistory().size() : 0));
                if (history.getSessionHistory() != null) {
                    for (SessionHistoryResponse.DaySessions day : history.getSessionHistory()) {
                        Log.d("SessionHistory", "Дата: " + day.getDate() + ", сессий: " + (day.getSessions() != null ? day.getSessions().size() : 0));
                    }
                }
                if (history.getSessionHistory() != null && !history.getSessionHistory().isEmpty()) {
                    daySessionsList.clear();
                    daySessionsList.addAll(history.getSessionHistory());
                    adapter.setDaySessionsList(daySessionsList);
                    checkEmptyState();
                } else {
                    emptyText.setText("В этом году нет тренировок");
                    emptyText.setVisibility(View.VISIBLE);
                    historyRecyclerView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                Log.e("SessionHistory", "Ошибка: " + error);
                emptyText.setText("Ошибка: " + error);
                emptyText.setVisibility(View.VISIBLE);
                historyRecyclerView.setVisibility(View.GONE);
                Toast.makeText(SessionHistoryActivity.this, "Ошибка загрузки истории", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            historyRecyclerView.setVisibility(View.GONE);
            emptyText.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            historyRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void checkEmptyState() {
        if (adapter.getItemCount() == 0) {
            historyRecyclerView.setVisibility(View.GONE);
            emptyText.setVisibility(View.VISIBLE);
        } else {
            historyRecyclerView.setVisibility(View.VISIBLE);
            emptyText.setVisibility(View.GONE);
        }
    }

    @Override
    public void onSessionClick(Long sessionId, Long workoutId) {
        // Находим сессию в загруженных данных
        SessionHistoryResponse.SessionInHistory session = findSessionById(sessionId);
        if (session != null) {
            SessionDetailActivity.start(
                    this,
                    sessionId,
                    session.getWorkoutName(),
                    session.getProgramName(),
                    session.getCompletedAt(),
                    session.getDurationMinutes() != null ? session.getDurationMinutes() : 0,
                    session.getTotalTonnage() != null ? session.getTotalTonnage() : 0.0,
                    session.getTotalSets() != null ? session.getTotalSets() : 0
            );
        } else {
            Toast.makeText(this, "Сессия не найдена", Toast.LENGTH_SHORT).show();
        }
    }

    private SessionHistoryResponse.SessionInHistory findSessionById(Long sessionId) {
        for (SessionHistoryResponse.DaySessions day : daySessionsList) {
            if (day.getSessions() != null) {
                for (SessionHistoryResponse.SessionInHistory session : day.getSessions()) {
                    if (sessionId.equals(session.getSessionId())) {
                        return session;
                    }
                }
            }
        }
        return null;
    }
}
