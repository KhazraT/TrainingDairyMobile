package ru.squidory.trainingdairymobile.ui.profile;

import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Locale;

import ru.squidory.trainingdairymobile.R;
import ru.squidory.trainingdairymobile.data.local.PreferencesManager;
import ru.squidory.trainingdairymobile.data.model.UserSettingsRequest;
import ru.squidory.trainingdairymobile.data.repository.UserRepository;

/**
 * Активность настроек приложения.
 * Позволяет изменить единицы измерения, тему и язык.
 */
public class SettingsActivity extends androidx.appcompat.app.AppCompatActivity {

    private Spinner spinnerWeightUnit, spinnerLengthUnit, spinnerDistanceUnit, spinnerTheme, spinnerLanguage;
    private PreferencesManager preferencesManager;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        preferencesManager = PreferencesManager.getInstance();
        userRepository = UserRepository.getInstance();

        initViews();
        setupSpinners();
        loadCurrentSettings();
        setupListeners();
    }

    private void initViews() {
        spinnerWeightUnit = findViewById(R.id.spinner_weight_unit);
        spinnerLengthUnit = findViewById(R.id.spinner_length_unit);
        spinnerDistanceUnit = findViewById(R.id.spinner_distance_unit);
        spinnerTheme = findViewById(R.id.spinner_theme);
        spinnerLanguage = findViewById(R.id.spinner_language);
    }

    private void setupSpinners() {
        // Вес: кг / фунты
        ArrayAdapter<CharSequence> weightAdapter = ArrayAdapter.createFromResource(this,
                R.array.weight_units_array, android.R.layout.simple_spinner_item);
        weightAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWeightUnit.setAdapter(weightAdapter);

        // Длина: см / дюймы
        ArrayAdapter<CharSequence> lengthAdapter = ArrayAdapter.createFromResource(this,
                R.array.length_units_array, android.R.layout.simple_spinner_item);
        lengthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLengthUnit.setAdapter(lengthAdapter);

        // Дистанция: км / мили
        ArrayAdapter<CharSequence> distanceAdapter = ArrayAdapter.createFromResource(this,
                R.array.distance_units_array, android.R.layout.simple_spinner_item);
        distanceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDistanceUnit.setAdapter(distanceAdapter);

        // Тема: светлая / тёмная
        ArrayAdapter<CharSequence> themeAdapter = ArrayAdapter.createFromResource(this,
                R.array.theme_array, android.R.layout.simple_spinner_item);
        themeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTheme.setAdapter(themeAdapter);

        // Язык: русский / english
        ArrayAdapter<CharSequence> langAdapter = ArrayAdapter.createFromResource(this,
                R.array.language_array, android.R.layout.simple_spinner_item);
        langAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguage.setAdapter(langAdapter);
    }

    private void loadCurrentSettings() {
        // Загружаем из локальных настроек
        setSpinnerSelection(spinnerWeightUnit, preferencesManager.getWeightUnit(), "kg");
        setSpinnerSelection(spinnerLengthUnit, preferencesManager.getLengthUnit(), "cm");
        setSpinnerSelection(spinnerDistanceUnit, preferencesManager.getDistanceUnit(), "km");
        setSpinnerSelection(spinnerTheme, preferencesManager.getTheme(), "light");
        setSpinnerSelection(spinnerLanguage, preferencesManager.getLanguage(), "ru");

        // Также пробуем загрузить с сервера
        userRepository.getUserSettings(new UserRepository.GetSettingsCallback() {
            @Override
            public void onSuccess(ru.squidory.trainingdairymobile.data.model.UserSettingsResponse settings) {
                runOnUiThread(() -> {
                    if (settings.getWeightUnit() != null) {
                        setSpinnerSelection(spinnerWeightUnit, settings.getWeightUnit(), "kg");
                    }
                    if (settings.getLengthUnit() != null) {
                        setSpinnerSelection(spinnerLengthUnit, settings.getLengthUnit(), "cm");
                    }
                    if (settings.getDistanceUnit() != null) {
                        setSpinnerSelection(spinnerDistanceUnit, settings.getDistanceUnit(), "km");
                    }
                    if (settings.getTheme() != null) {
                        setSpinnerSelection(spinnerTheme, settings.getTheme(), "light");
                    }
                    if (settings.getLanguage() != null) {
                        setSpinnerSelection(spinnerLanguage, settings.getLanguage(), "ru");
                    }
                });
            }

            @Override
            public void onError(String error) {
                // Игнорируем - используем локальные настройки
            }
        });
    }

    private void setSpinnerSelection(Spinner spinner, String value, String defaultVal) {
        if (value == null) value = defaultVal;
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            String item = adapter.getItem(i).toString();
            if (item.toLowerCase(Locale.ROOT).contains(value.toLowerCase(Locale.ROOT))) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void setupListeners() {
        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                saveSettings();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        spinnerWeightUnit.setOnItemSelectedListener(listener);
        spinnerLengthUnit.setOnItemSelectedListener(listener);
        spinnerDistanceUnit.setOnItemSelectedListener(listener);
        spinnerTheme.setOnItemSelectedListener(listener);
        spinnerLanguage.setOnItemSelectedListener(listener);
    }

    private void saveSettings() {
        String weightUnit = spinnerWeightUnit.getSelectedItem().toString().toLowerCase(Locale.ROOT).contains("kg") ? "kg" : "lb";
        String lengthUnit = spinnerLengthUnit.getSelectedItem().toString().toLowerCase(Locale.ROOT).contains("см") ? "cm" : "inch";
        String distanceUnit = spinnerDistanceUnit.getSelectedItem().toString().toLowerCase(Locale.ROOT).contains("км") ? "km" : "miles";
        String theme = spinnerTheme.getSelectedItem().toString().toLowerCase(Locale.ROOT).contains("свет") ? "light" : "dark";
        String language = spinnerLanguage.getSelectedItem().toString().toLowerCase(Locale.ROOT).contains("рус") ? "ru" : "en";

        // Сохраняем локально
        preferencesManager.setWeightUnit(weightUnit);
        preferencesManager.setLengthUnit(lengthUnit);
        preferencesManager.setDistanceUnit(distanceUnit);
        preferencesManager.setTheme(theme);
        preferencesManager.setLanguage(language);

        // Отправляем на сервер
        UserSettingsRequest request = new UserSettingsRequest();
        request.setWeightUnit(weightUnit);
        request.setLengthUnit(lengthUnit);
        request.setDistanceUnit(distanceUnit);
        request.setTheme(theme);
        request.setLanguage(language);

        userRepository.updateUserSettings(request, new UserRepository.UpdateSettingsCallback() {
            @Override
            public void onSuccess(ru.squidory.trainingdairymobile.data.model.UserSettingsResponse settings) {
                runOnUiThread(() ->
                        Toast.makeText(SettingsActivity.this, R.string.success, Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() ->
                        Toast.makeText(SettingsActivity.this, "Настройки сохранены локально", Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
