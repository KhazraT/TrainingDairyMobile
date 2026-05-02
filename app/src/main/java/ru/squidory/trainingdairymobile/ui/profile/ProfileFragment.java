package ru.squidory.trainingdairymobile.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.content.Intent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ru.squidory.trainingdairymobile.R;
import ru.squidory.trainingdairymobile.data.model.UserResponse;
import ru.squidory.trainingdairymobile.data.repository.UserRepository;
import ru.squidory.trainingdairymobile.ui.main.BaseFragment;
import ru.squidory.trainingdairymobile.data.local.PreferencesManager;

/**
 * Фрагмент раздела "Профиль".
 * Отображает информацию о пользователе, настройки и действия с аккаунтом.
 */
public class ProfileFragment extends BaseFragment {

    private TextView tvName, tvEmail, tvBirthDate, tvGender;
    private Button btnEditProfile, btnSettings, btnExportData, btnDeleteAccount, btnLogout;
    private UserRepository userRepository;
    private PreferencesManager preferencesManager;
    private ActivityResultLauncher<Intent> editProfileLauncher;

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
        btnDeleteAccount = view.findViewById(R.id.btn_delete_account);
        btnLogout = view.findViewById(R.id.btn_logout);
    }

    private void setupClickListeners() {
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), EditProfileActivity.class);
            editProfileLauncher.launch(intent);
        });

        btnExportData.setOnClickListener(v -> {
            // TODO: реализовать экспорт данных
            Toast.makeText(getContext(), R.string.export_data, Toast.LENGTH_SHORT).show();
        });

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
}
