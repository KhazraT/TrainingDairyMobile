package ru.squidory.trainingdairymobile.ui.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ru.squidory.trainingdairymobile.R;
import ru.squidory.trainingdairymobile.data.local.PreferencesManager;
import ru.squidory.trainingdairymobile.data.model.UserResponse;
import ru.squidory.trainingdairymobile.data.repository.UserRepository;
import ru.squidory.trainingdairymobile.ui.main.BaseFragment;
import ru.squidory.trainingdairymobile.util.DataExportManager;
import ru.squidory.trainingdairymobile.util.DataImportManager;

/**
 * Фрагмент раздела "Профиль".
 * Отображает информацию о пользователе, настройки и действия с аккаунтом.
 */
public class ProfileFragment extends BaseFragment {

    private TextView tvName, tvEmail, tvBirthDate, tvGender;
    private Button btnEditProfile, btnExportData, btnImportData, btnDeleteAccount, btnLogout;
    private UserRepository userRepository;
    private PreferencesManager preferencesManager;
    private ActivityResultLauncher<Intent> editProfileLauncher;
    private ActivityResultLauncher<Intent> exportLauncher;
    private ActivityResultLauncher<Intent> importLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        userRepository = UserRepository.getInstance();
        preferencesManager = PreferencesManager.getInstance();

        // Инициализируем launcher для обновления данных после редактирования
        editProfileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> loadUserProfile()
        );

        // Launcher для экспорта: SAF picker → запись в выбранный URI
        exportLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            performExport(uri);
                        }
                    }
                }
        );

        // Launcher для импорта: SAF picker → чтение из выбранного URI
        importLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            performImport(uri);
                        }
                    }
                }
        );

        initViews(view);
        setupClickListeners();
        loadUserProfile();

        return view;
    }

    private void initViews(View view) {
        tvName = view.findViewById(R.id.tv_name);
        tvEmail = view.findViewById(R.id.tv_email);
        tvBirthDate = view.findViewById(R.id.tv_birth_date);
        tvGender = view.findViewById(R.id.tv_gender);

        btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        btnExportData = view.findViewById(R.id.btn_export_data);
        btnImportData = view.findViewById(R.id.btn_import_data);
        btnDeleteAccount = view.findViewById(R.id.btn_delete_account);
        btnLogout = view.findViewById(R.id.btn_logout);
    }

    private void setupClickListeners() {
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), EditProfileActivity.class);
            editProfileLauncher.launch(intent);
        });

        btnExportData.setOnClickListener(v -> startExportWithSAF());

        btnImportData.setOnClickListener(v -> startImportWithSAF());

        btnDeleteAccount.setOnClickListener(v -> {
            // Показываем диалог подтверждения
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle(R.string.delete_account)
                    .setMessage(R.string.confirm_delete_account)
                    .setPositiveButton(R.string.delete, (dialog, which) -> deleteAccount())
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        });

        btnLogout.setOnClickListener(v -> {
            preferencesManager.clearAll();
            // Возвращаемся к экрану входа
            if (getActivity() != null) {
                getActivity().finish();
            }
        });
    }

    private void loadUserProfile() {
        userRepository.getCurrentUser(new UserRepository.GetUserCallback() {
            @Override
            public void onSuccess(UserResponse user) {
                if (getActivity() == null || getActivity().isFinishing()) return;

                tvName.setText(user.getName() != null ? user.getName() : "Имя не указано");
                tvEmail.setText(user.getEmail() != null ? user.getEmail() : "");

                // Форматируем дату рождения
                Date birthDate = user.getBirthDate();
                if (birthDate != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                    tvBirthDate.setText(sdf.format(birthDate));
                } else {
                    tvBirthDate.setText("не указана");
                }

                // Отображаем пол
                String gender = user.getGender();
                if (gender != null) {
                    switch (gender.toLowerCase()) {
                        case "male":
                            tvGender.setText("Мужской");
                            break;
                        case "female":
                            tvGender.setText("Женский");
                            break;
                        default:
                            tvGender.setText(gender);
                    }
                } else {
                    tvGender.setText("не указан");
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() == null || getActivity().isFinishing()) return;
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void deleteAccount() {
        btnDeleteAccount.setEnabled(false);
        userRepository.deleteCurrentUser(new UserRepository.DeleteUserCallback() {
            @Override
            public void onSuccess() {
                preferencesManager.clearAll();
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() == null || getActivity().isFinishing()) return;
                btnDeleteAccount.setEnabled(true);
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public int getMenuItemId() {
        return R.id.navigation_profile;
    }

    @Override
    public String getTitle() {
        return getString(R.string.fragment_profile);
    }

    // ==================== ЭКСПОРТ ====================

    private void startExportWithSAF() {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String fileName = "training_dairy_full_export_" + timestamp + ".json";

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, fileName);
        exportLauncher.launch(intent);
    }

    private void performExport(Uri uri) {
        new Thread(() -> {
            android.os.StrictMode.setThreadPolicy(
                    new android.os.StrictMode.ThreadPolicy.Builder()
                            .permitAll()
                            .build()
            );
            try {
                android.util.Log.d("ProfileFragment", "Начало полного экспорта...");
                java.io.OutputStream os = requireContext().getContentResolver().openOutputStream(uri);
                if (os == null) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Ошибка: не удалось открыть файл", Toast.LENGTH_LONG).show()
                    );
                    return;
                }

                boolean success = DataExportManager.exportFullData(os);
                os.close();

                requireActivity().runOnUiThread(() -> {
                    if (success) {
                        Toast.makeText(requireContext(), "Полный экспорт завершён успешно", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(requireContext(), "Ошибка при экспорте данных", Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                android.util.Log.e("ProfileFragment", "Export error", e);
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }

    // ==================== ИМПОРТ ====================

    private void startImportWithSAF() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        importLauncher.launch(intent);
    }

    private void performImport(Uri uri) {
        new Thread(() -> {
            android.os.StrictMode.setThreadPolicy(
                    new android.os.StrictMode.ThreadPolicy.Builder()
                            .permitAll()
                            .build()
            );
            try {
                android.util.Log.d("ProfileFragment", "Начало импорта из файла...");
                java.io.InputStream is = requireContext().getContentResolver().openInputStream(uri);
                if (is == null) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Ошибка: не удалось открыть файл", Toast.LENGTH_LONG).show()
                    );
                    return;
                }

                boolean success = DataImportManager.importFullData(is);
                is.close();

                requireActivity().runOnUiThread(() -> {
                    if (success) {
                        Toast.makeText(requireContext(), "Импорт завершён успешно. Данные восстановлены.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(requireContext(), "Ошибка при импорте данных. Проверьте формат файла.", Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                android.util.Log.e("ProfileFragment", "Import error", e);
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }
}
