package ru.squidory.trainingdairymobile.ui.trainings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ru.squidory.trainingdairymobile.R;
import ru.squidory.trainingdairymobile.data.model.ProgramResponse;
import ru.squidory.trainingdairymobile.data.repository.ProgramRepository;
import ru.squidory.trainingdairymobile.ui.main.BaseFragment;

/**
 * Фрагмент раздела "Тренировки".
 * Отображает список тренировочных программ.
 */
public class TrainingsFragment extends BaseFragment {

    private RecyclerView programsRecyclerView;
    private ProgressBar progressBar;
    private TextView emptyText;
    private ImageButton addProgramButton;

    private ProgramAdapter adapter;
    private ProgramRepository repository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trainings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repository = ProgramRepository.getInstance();

        initViews(view);
        setupRecyclerView();
        setupListeners();
        loadPrograms();
    }

    private void initViews(View view) {
        programsRecyclerView = view.findViewById(R.id.programsRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyText = view.findViewById(R.id.emptyText);
        addProgramButton = view.findViewById(R.id.addProgramButton);
    }

    private void setupRecyclerView() {
        adapter = new ProgramAdapter();
        programsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        programsRecyclerView.setAdapter(adapter);

        adapter.setOnProgramClickListener(new ProgramAdapter.OnProgramClickListener() {
            @Override
            public void onProgramClick(ProgramResponse program) {
                // Открыть детали программы
                Toast.makeText(getContext(), "Программа: " + program.getName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProgramLongClick(ProgramResponse program) {
                // Показать меню действий
                Toast.makeText(getContext(), "Долгий клик: " + program.getName(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        addProgramButton.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Создание программы (в разработке)", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadPrograms() {
        showLoading(true);
        repository.getPrograms(new ProgramRepository.ProgramsCallback() {
            @Override
            public void onSuccess(List<ProgramResponse> programs) {
                showLoading(false);
                adapter.setPrograms(programs);
                checkEmptyState();
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                String message = error != null && !error.isEmpty() ? error : getString(R.string.error_unknown);
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                emptyText.setText(message);
                emptyText.setVisibility(View.VISIBLE);
            }
        });
    }

    private void showLoading(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            programsRecyclerView.setVisibility(View.GONE);
            emptyText.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            programsRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void checkEmptyState() {
        if (adapter.getItemCount() == 0) {
            programsRecyclerView.setVisibility(View.GONE);
            emptyText.setVisibility(View.VISIBLE);
        } else {
            programsRecyclerView.setVisibility(View.VISIBLE);
            emptyText.setVisibility(View.GONE);
        }
    }

    @Override
    public int getMenuItemId() {
        return R.id.navigation_trainings;
    }

    @Override
    public String getTitle() {
        return getString(R.string.fragment_trainings);
    }
}
