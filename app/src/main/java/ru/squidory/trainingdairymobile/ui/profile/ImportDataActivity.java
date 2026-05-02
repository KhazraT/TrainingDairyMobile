package ru.squidory.trainingdairymobile.ui.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import ru.squidory.trainingdairymobile.R;
import ru.squidory.trainingdairymobile.util.DataImportManager;

/**
 * Активность для импорта данных из файла.
 * Использует SAF (Storage Access Framework) для выбора файла без разрешений.
 * Поддерживает форматы JSON и CSV.
 */
public class ImportDataActivity extends AppCompatActivity {

    private static final int FILE_PICK_CODE = 200;
    private RadioGroup rgSource;
    private Button btnSelectFile;
    private Button btnImport;
    private Uri selectedFileUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_data);

        rgSource = findViewById(R.id.rg_source);
        btnSelectFile = findViewById(R.id.btn_select_file);
        btnImport = findViewById(R.id.btn_import);

        btnSelectFile.setOnClickListener(v -> pickFile());
        btnImport.setOnClickListener(v -> performImport());
    }

    private void pickFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Выберите файл для импорта"), FILE_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_PICK_CODE && resultCode == RESULT_OK && data != null) {
            selectedFileUri = data.getData();
            Toast.makeText(this, "Файл выбран: " + selectedFileUri.getLastPathSegment(), Toast.LENGTH_SHORT).show();
        }
    }

    private void performImport() {
        if (selectedFileUri == null) {
            Toast.makeText(this, "Сначала выберите файл", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success = DataImportManager.importDataFromUri(this, selectedFileUri);
        if (success) {
            Toast.makeText(this, "Данные успешно импортированы", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Ошибка при импорте данных", Toast.LENGTH_LONG).show();
        }
    }
}
