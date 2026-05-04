package ru.squidory.trainingdairymobile.ui.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import ru.squidory.trainingdairymobile.R;
import ru.squidory.trainingdairymobile.util.DataExportManager;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Активность для экспорта данных пользователя.
 * Использует SAF (Storage Access Framework) для выбора места сохранения.
 * Вызывает /api/export/full для получения всех данных (программы, упражнения, сессии, подходы).
 */
public class ExportDataActivity extends AppCompatActivity {

    private static final int SAF_EXPORT_REQUEST_CODE = 1001;
    private RadioGroup rgFormat;
    private Button btnExport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_data);

        // Разрешаем сеть в главном потоке (для Android StrictMode)
        android.os.StrictMode.setThreadPolicy(
            new android.os.StrictMode.ThreadPolicy.Builder()
                .permitAll()
                .build()
        );

        rgFormat = findViewById(R.id.rg_format);
        btnExport = findViewById(R.id.btn_export);

        btnExport.setOnClickListener(v -> startExportWithSAF());
    }

    private void startExportWithSAF() {
        int formatId = rgFormat.getCheckedRadioButtonId();
        String format = (formatId == R.id.rb_json) ? "json" : "csv";

        if ("csv".equals(format)) {
            Toast.makeText(this, "Экспорт в CSV пока в разработке", Toast.LENGTH_SHORT).show();
            return;
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String fileName = "training_dairy_full_export_" + timestamp + ".json";

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, fileName);
        startActivityForResult(intent, SAF_EXPORT_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SAF_EXPORT_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                exportToUri(uri);
            }
        }
    }

    private void exportToUri(Uri uri) {
        new Thread(() -> {
            android.os.StrictMode.setThreadPolicy(
                new android.os.StrictMode.ThreadPolicy.Builder()
                    .permitAll()
                    .build()
            );

            try {
                android.util.Log.d("ExportDataActivity", "Начало полного экспорта...");
                OutputStream os = getContentResolver().openOutputStream(uri);
                if (os == null) {
                    runOnUiThread(() -> Toast.makeText(ExportDataActivity.this, "Ошибка: не удалось открыть файл", Toast.LENGTH_LONG).show());
                    return;
                }

                boolean success = DataExportManager.exportFullData(os);
                os.close();

                runOnUiThread(() -> {
                    if (success) {
                        Toast.makeText(ExportDataActivity.this, "Полный экспорт завершён успешно", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(ExportDataActivity.this, "Ошибка при экспорте данных", Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                android.util.Log.e("ExportDataActivity", "Export error", e);
                runOnUiThread(() -> Toast.makeText(ExportDataActivity.this, "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }
}
