package ru.squidory.trainingdairymobile.ui.trainings;

import android.content.Context;
import android.content.Intent;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ru.squidory.trainingdairymobile.R;
import ru.squidory.trainingdairymobile.data.model.SessionResponse;
import ru.squidory.trainingdairymobile.data.repository.SessionRepository;

/**
 * Экран детальной информации о выполненной сессии (readonly).
 */
public class SessionDetailActivity extends AppCompatActivity {

    public static final String EXTRA_SESSION_ID = "session_id";
    public static final String EXTRA_WORKOUT_NAME = "workout_name";
    public static final String EXTRA_PROGRAM_NAME = "program_name";
    public static final String EXTRA_COMPLETED_AT = "completed_at";
    public static final String EXTRA_DURATION_MINUTES = "duration_minutes";
    public static final String EXTRA_TOTAL_TONNAGE = "total_tonnage";
    public static final String EXTRA_TOTAL_SETS = "total_sets";

    public static void start(Context context, long sessionId, String workoutName, String programName,
                             String completedAt, int durationMinutes, double totalTonnage, int totalSets) {
        Intent intent = new Intent(context, SessionDetailActivity.class);
        intent.putExtra(EXTRA_SESSION_ID, sessionId);
        intent.putExtra(EXTRA_WORKOUT_NAME, workoutName);
        intent.putExtra(EXTRA_PROGRAM_NAME, programName);
        intent.putExtra(EXTRA_COMPLETED_AT, completedAt);
        intent.putExtra(EXTRA_DURATION_MINUTES, durationMinutes);
        intent.putExtra(EXTRA_TOTAL_TONNAGE, totalTonnage);
        intent.putExtra(EXTRA_TOTAL_SETS, totalSets);
        context.startActivity(intent);
    }

    private MaterialToolbar toolbar;
    private TextView workoutNameText;
    private TextView programNameText;
    private TextView dateText;
    private TextView durationText;
    private TextView tonnageText;
    private TextView setsCountText;
    private RecyclerView exercisesRecyclerView;
    private ProgressBar progressBar;
    private TextView emptyText;

    private SessionDetailExerciseAdapter adapter;
    private SessionRepository repository;

    private long sessionId;
    private String workoutName;
    private String programName;
    private String completedAt;
    private int durationMinutes;
    private double totalTonnage;
    private int totalSets;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_detail);

        sessionId = getIntent().getLongExtra(EXTRA_SESSION_ID, -1);
        workoutName = getIntent().getStringExtra(EXTRA_WORKOUT_NAME);
        programName = getIntent().getStringExtra(EXTRA_PROGRAM_NAME);
        completedAt = getIntent().getStringExtra(EXTRA_COMPLETED_AT);
        durationMinutes = getIntent().getIntExtra(EXTRA_DURATION_MINUTES, 0);
        totalTonnage = getIntent().getDoubleExtra(EXTRA_TOTAL_TONNAGE, 0.0);
        totalSets = getIntent().getIntExtra(EXTRA_TOTAL_SETS, 0);

        if (sessionId == -1) {
            Toast.makeText(this, "Ошибка: сессия не найдена", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        repository = SessionRepository.getInstance();

        initViews();
        setupToolbar();
        setupRecyclerView();
        displaySummary();
        loadSessionDetails();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        workoutNameText = findViewById(R.id.workoutNameText);
        programNameText = findViewById(R.id.programNameText);
        dateText = findViewById(R.id.dateText);
        durationText = findViewById(R.id.durationText);
        tonnageText = findViewById(R.id.tonnageText);
        setsCountText = findViewById(R.id.setsCountText);
        exercisesRecyclerView = findViewById(R.id.exercisesRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyText = findViewById(R.id.emptyText);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new SessionDetailExerciseAdapter();
        exercisesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        exercisesRecyclerView.setAdapter(adapter);
        exercisesRecyclerView.setNestedScrollingEnabled(false);
    }

    private void displaySummary() {
        workoutNameText.setText(workoutName != null ? workoutName : "Тренировка");
        programNameText.setText(programName != null ? programName : "");
        dateText.setText(formatDate(completedAt));
        durationText.setText(durationMinutes + " мин");
        tonnageText.setText(String.format(Locale.getDefault(), "%.1f т", totalTonnage / 1000.0));
        setsCountText.setText(String.valueOf(totalSets));
    }

    private void loadSessionDetails() {
        showLoading(true);

        repository.getSessionById(sessionId, new SessionRepository.SessionCallback() {
            @Override
            public void onSuccess(SessionResponse session) {
                showLoading(false);
                if (session.getExercises() != null && !session.getExercises().isEmpty()) {
                    adapter.setExercises(session.getExercises());
                    emptyText.setVisibility(View.GONE);
                    exercisesRecyclerView.setVisibility(View.VISIBLE);
                } else {
                    emptyText.setVisibility(View.VISIBLE);
                    exercisesRecyclerView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                Toast.makeText(SessionDetailActivity.this, "Ошибка загрузки: " + error, Toast.LENGTH_SHORT).show();
                emptyText.setVisibility(View.VISIBLE);
                exercisesRecyclerView.setVisibility(View.GONE);
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private String formatDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return "";
        }
        try {
            // ISO 8601
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(dateStr);
            if (date == null) {
                // Пробуем только дату
                inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                date = inputFormat.parse(dateStr);
            }
            if (date != null) {
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMMM yyyy, HH:mm", new Locale("ru"));
                return outputFormat.format(date);
            }
        } catch (Exception e) {
            // ignore
        }
        return dateStr;
    }
}