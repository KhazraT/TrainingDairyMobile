package ru.squidory.trainingdairymobile.ui.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import ru.squidory.trainingdairymobile.R;
import ru.squidory.trainingdairymobile.util.DataImportManager;

/**
 * Активность для импорта данных пользователя из файла.
 * Использует SAF (Storage Access Framework) для выбора файла без разрешений.
 * Вызывает POST /api/import/full для восстановления данных на новом аккаунте.
 */
public class ImportDataActivity extends AppCompatActivity {

    private static final int SAF_IMPORT_REQUEST_CODE = 200;
    private Button btnSelectFile;
    private Button btnImport;
    private Uri selectedFileUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_data);

        // Разрешаем сеть в главном потоке (для Android StrictMode)
        android.os.StrictMode.setThreadPolicy(
            new android.os.StrictMode.ThreadPolicy.Builder()
                .permitAll()
                .build()
        );

        btnSelectFile = findViewById(R.id.btn_select_file);
        btnImport = findViewById(R.id.btn_import);

        btnSelectFile.setOnClickListener(v -> pickFile());
        btnImport.setOnClickListener(v -> performImport());
    }

    private void pickFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*"); // Принимаем любые файлы, проверим JSON внутри
        startActivityForResult(intent, SAF_IMPORT_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SAF_IMPORT_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            selectedFileUri = data.getData();
            if (selectedFileUri != null) {
                Toast.makeText(this, "Файл выбран: " + selectedFileUri.getLastPathSegment(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void performImport() {
        if (selectedFileUri == null) {
            Toast.makeText(this, "Сначала выберите файл", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            android.os.StrictMode.setThreadPolicy(
                new android.os.StrictMode.ThreadPolicy.Builder()
                    .permitAll()
                    .build()
            );

            try {
                android.util.Log.d("ImportDataActivity", "Начало импорта из файла...");
                java.io.InputStream is = getContentResolver().openInputStream(selectedFileUri);
                if (is == null) {
                    runOnUiThread(() -> Toast.makeText(ImportDataActivity.this, "Ошибка: не удалось открыть файл", Toast.LENGTH_LONG).show());
                    return;
                }

                boolean success = DataImportManager.importFullData(is);
                is.close();

                runOnUiThread(() -> {
                    if (success) {
                        Toast.makeText(ImportDataActivity.this, "Импорт завершён успешно. Данные восстановлены.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(ImportDataActivity.this, "Ошибка при импорте данных. Проверьте формат файла.", Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                android.util.Log.e("ImportDataActivity", "Import error", e);
                runOnUiThread(() -> Toast.makeText(ImportDataActivity.this, "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }
}
