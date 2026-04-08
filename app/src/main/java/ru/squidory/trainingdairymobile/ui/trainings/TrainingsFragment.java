package ru.squidory.trainingdairymobile.ui.trainings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.squidory.trainingdairymobile.R;
import ru.squidory.trainingdairymobile.data.model.ProgramRequest;
import ru.squidory.trainingdairymobile.data.model.ProgramResponse;
import ru.squidory.trainingdairymobile.data.model.WorkoutResponse;
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
    private EditText searchEditText;
    private ImageButton clearSearchButton;

    private ProgramAdapter adapter;
    private ProgramRepository repository;

    private final List<ProgramResponse> allPrograms = new ArrayList<>();
    private String searchQuery = "";

    private ActivityResultLauncher<Intent> programDetailLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        programDetailLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (isAdded() && result.getResultCode() == Activity.RESULT_OK) {
                        loadPrograms();
                    }
                }
        );
    }

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
        searchEditText = view.findViewById(R.id.searchEditText);
        clearSearchButton = view.findViewById(R.id.clearSearchButton);
    }

    private void setupRecyclerView() {
        adapter = new ProgramAdapter();
        programsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        programsRecyclerView.setAdapter(adapter);

        adapter.setOnProgramClickListener(new ProgramAdapter.OnProgramClickListener() {
            @Override
            public void onProgramClick(ProgramResponse program) {
                // Открыть детали программы
                Intent intent = new Intent(getContext(), ProgramDetailActivity.class);
                intent.putExtra(ProgramDetailActivity.EXTRA_PROGRAM_ID, program.getId());
                intent.putExtra(ProgramDetailActivity.EXTRA_PROGRAM_NAME, program.getName());
                intent.putExtra(ProgramDetailActivity.EXTRA_PROGRAM_DESCRIPTION, program.getDescription());
                programDetailLauncher.launch(intent);
            }

            @Override
            public void onProgramLongClick(ProgramResponse program) {
                // Показать меню действий
                Toast.makeText(getContext(), "Долгий клик: " + program.getName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDeleteProgramClick(ProgramResponse program) {
                showDeleteProgramConfirmation(program);
            }
        });
    }

    private void setupListeners() {
        addProgramButton.setOnClickListener(v -> showCreateProgramDialog());

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().trim();
                clearSearchButton.setVisibility(searchQuery.isEmpty() ? View.GONE : View.VISIBLE);
                applyFilters();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        clearSearchButton.setOnClickListener(v -> {
            searchEditText.setText("");
            searchQuery = "";
            clearSearchButton.setVisibility(View.GONE);
            applyFilters();
        });
    }

    private void applyFilters() {
        List<ProgramResponse> filtered = new ArrayList<>();
        for (ProgramResponse p : allPrograms) {
            boolean matchesName = p.getName() != null && p.getName().toLowerCase().contains(searchQuery.toLowerCase());
            boolean matchesDesc = p.getDescription() != null && p.getDescription().toLowerCase().contains(searchQuery.toLowerCase());
            if (searchQuery.isEmpty() || matchesName || matchesDesc) {
                filtered.add(p);
            }
        }
        adapter.setPrograms(filtered);
        checkEmptyState();
    }

    private void showCreateProgramDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_create_program, null);
        builder.setView(dialogView);

        TextInputLayout nameLayout = dialogView.findViewById(R.id.programNameLayout);
        TextInputEditText nameInput = dialogView.findViewById(R.id.programNameInput);
        TextInputEditText descriptionInput = dialogView.findViewById(R.id.programDescriptionInput);
        MaterialButton cancelButton = dialogView.findViewById(R.id.dialogCancelButton);
        MaterialButton saveButton = dialogView.findViewById(R.id.dialogSaveButton);

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        saveButton.setOnClickListener(v -> {
            String name = nameInput.getText() != null ? nameInput.getText().toString().trim() : "";

            if (name.isEmpty()) {
                nameLayout.setError("Введите название программы");
                return;
            }

            nameLayout.setError(null);

            String description = descriptionInput.getText() != null ? descriptionInput.getText().toString().trim() : "";

            ProgramRequest request = new ProgramRequest();
            request.setName(name);
            request.setDescription(description);
            request.setIsPublic(false);

            repository.createProgram(request, new ProgramRepository.ProgramCallback() {
                @Override
                public void onSuccess(ProgramResponse program) {
                    if (!isAdded()) return;
                    dialog.dismiss();
                    Toast.makeText(getContext(), "Программа создана", Toast.LENGTH_SHORT).show();
                    loadPrograms();
                }

                @Override
                public void onError(String error) {
                    if (!isAdded()) return;
                    Toast.makeText(getContext(), "Ошибка: " + error, Toast.LENGTH_LONG).show();
                }
            });
        });

        dialog.show();
    }

    private void loadPrograms() {
        if (!isAdded()) return;
        showLoading(true);
        repository.getPrograms(new ProgramRepository.ProgramsCallback() {
            @Override
            public void onSuccess(List<ProgramResponse> programs) {
                if (!isAdded()) return;
                allPrograms.clear();
                allPrograms.addAll(programs);
                applyFilters();

                // Загружаем количество тренировок для каждой программы
                loadWorkoutCounts(programs);
            }

            @Override
            public void onError(String error) {
                if (!isAdded()) return;
                showLoading(false);
                String message = error != null && !error.isEmpty() ? error : getString(R.string.error_unknown);
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                emptyText.setText(message);
                emptyText.setVisibility(View.VISIBLE);
            }
        });
    }

    private void loadWorkoutCounts(List<ProgramResponse> programs) {
        if (!isAdded()) return;
        Map<Long, Integer> counts = new HashMap<>();
        final int[] pendingRequests = {programs.size()};

        if (programs.isEmpty()) {
            showLoading(false);
            checkEmptyState();
            return;
        }

        for (ProgramResponse program : programs) {
            repository.getWorkoutsByProgram(program.getId(), new ProgramRepository.WorkoutsCallback() {
                @Override
                public void onSuccess(List<WorkoutResponse> workouts) {
                    if (!isAdded()) return;
                    counts.put(program.getId(), workouts.size());
                    pendingRequests[0]--;

                    if (pendingRequests[0] == 0) {
                        // Все запросы завершены
                        showLoading(false);
                        adapter.updateWorkoutCounts(counts);
                        checkEmptyState();
                    }
                }

                @Override
                public void onError(String error) {
                    if (!isAdded()) return;
                    counts.put(program.getId(), 0);
                    pendingRequests[0]--;

                    if (pendingRequests[0] == 0) {
                        showLoading(false);
                        adapter.updateWorkoutCounts(counts);
                        checkEmptyState();
                    }
                }
            });
        }
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

    private void showDeleteProgramConfirmation(ProgramResponse program) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_program)
                .setMessage(R.string.delete_program_confirm)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    repository.deleteProgram(program.getId(), new ProgramRepository.SimpleCallback() {
                        @Override
                        public void onSuccess() {
                            if (isAdded()) {
                                Toast.makeText(getContext(), "Программа удалена", Toast.LENGTH_SHORT).show();
                                loadPrograms();
                            }
                        }

                        @Override
                        public void onError(String error) {
                            if (isAdded()) {
                                Toast.makeText(getContext(), "Ошибка: " + error, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
