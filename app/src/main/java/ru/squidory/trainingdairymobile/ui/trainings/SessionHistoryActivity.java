package ru.squidory.trainingdairymobile.ui.trainings;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.Calendar;

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

        // Получаем текущий год и месяц
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; // Calendar.MONTH is 0-based

        repository.getSessionHistory(year, month, 0, 20, new SessionRepository.SessionHistoryCallback() {
            @Override
            public void onSuccess(SessionHistoryResponse history) {
                showLoading(false);
                if (history.getSessionHistory() != null && !history.getSessionHistory().isEmpty()) {
                    adapter.setDaySessionsList(history.getSessionHistory());
                    checkEmptyState();
                } else {
                    emptyText.setText("В этом месяце нет тренировок");
                    emptyText.setVisibility(View.VISIBLE);
                    historyRecyclerView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String error) {
                showLoading(false);
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
        // TODO: Открыть детали сессии
        Toast.makeText(this, "Session " + sessionId + ", Workout " + workoutId, Toast.LENGTH_SHORT).show();
    }
}
