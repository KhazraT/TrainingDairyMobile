package ru.squidory.trainingdairymobile.ui.profile;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import ru.squidory.trainingdairymobile.R;
import ru.squidory.trainingdairymobile.data.model.UserResponse;
import ru.squidory.trainingdairymobile.data.repository.UserRepository;

/**
 * Активность для редактирования профиля пользователя.
 * Позволяет изменить имя, дату рождения и пол.
 */
public class EditProfileActivity extends androidx.appcompat.app.AppCompatActivity {

    private EditText etName, etBirthDate;
    private Spinner spinnerGender;
    private Button btnSave, btnCancel;
    private UserRepository userRepository;

    private Calendar birthCalendar = Calendar.getInstance();
    private boolean birthDateSet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        userRepository = UserRepository.getInstance();

        initViews();
        setupGenderSpinner();
        loadCurrentProfile();
        setupListeners();
    }

    private void initViews() {
        etName = findViewById(R.id.et_name);
        etBirthDate = findViewById(R.id.et_birth_date);
        spinnerGender = findViewById(R.id.spinner_gender);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);
    }

    private void setupGenderSpinner() {
        String[] genders = {getString(R.string.gender_male), getString(R.string.gender_female)};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, genders);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);
    }

    private void loadCurrentProfile() {
        userRepository.getCurrentUser(new UserRepository.GetUserCallback() {
            @Override
            public void onSuccess(UserResponse user) {
                runOnUiThread(() -> {
                    etName.setText(user.getName());

                    // Устанавливаем дату рождения
                    Date birthDate = user.getBirthDate();
                    if (birthDate != null) {
                        birthCalendar.setTime(birthDate);
                        birthDateSet = true;
                        updateBirthDateDisplay();
                    }

                    // Устанавливаем пол
                    String gender = user.getGender();
                    if ("male".equalsIgnoreCase(gender)) {
                        spinnerGender.setSelection(0);
                    } else if ("female".equalsIgnoreCase(gender)) {
                        spinnerGender.setSelection(1);
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(EditProfileActivity.this, error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void setupListeners() {
        etBirthDate.setOnClickListener(v -> showDatePicker());

        btnCancel.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void showDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    birthCalendar.set(year, month, dayOfMonth);
                    birthDateSet = true;
                    updateBirthDateDisplay();
                },
                birthCalendar.get(Calendar.YEAR),
                birthCalendar.get(Calendar.MONTH),
                birthCalendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis()); // Нелзя выбрать будущее
        dialog.show();
    }

    private void updateBirthDateDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        etBirthDate.setText(sdf.format(birthCalendar.getTime()));
    }

    private void saveProfile() {
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            etName.setError("Введите имя");
            return;
        }

        UserResponse request = new UserResponse();
        request.setName(name);

        if (birthDateSet) {
            request.setBirthDate(birthCalendar.getTime());
        }

        String selectedGender = spinnerGender.getSelectedItem().toString();
        if (getString(R.string.gender_male).equals(selectedGender)) {
            request.setGender("male");
        } else if (getString(R.string.gender_female).equals(selectedGender)) {
            request.setGender("female");
        }

        btnSave.setEnabled(false);
        userRepository.updateCurrentUser(request, new UserRepository.UpdateUserCallback() {
            @Override
            public void onSuccess(UserResponse user) {
                runOnUiThread(() -> {
                    Toast.makeText(EditProfileActivity.this, R.string.success, Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    btnSave.setEnabled(true);
                    Toast.makeText(EditProfileActivity.this, error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}
