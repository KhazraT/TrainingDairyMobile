package ru.squidory.trainingdairymobile.util;

import android.util.Log;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Response;
import ru.squidory.trainingdairymobile.data.remote.NetworkClient;

import java.io.IOException;
import java.io.InputStream;

/**
 * Менеджер для импорта данных пользователя из файла.
 * Использует эндпоинт POST /api/import/full.
 */
public class DataImportManager {

    private static final String TAG = "DataImportManager";

    /**
     * Импортирует данные из InputStream (прочитанного из файла).
     * Отправляет JSON файл на сервер через POST /api/import/full.
     *
     * @param is InputStream файла экспорта
     * @return true если успешно, false при ошибке
     */
    public static boolean importFullData(InputStream is) {
        try {
            Log.d(TAG, "Начало импорта данных через /api/import/full...");

            // Читаем весь файл в память (для RequestBody)
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            byte[] fileBytes = baos.toByteArray();
            Log.d(TAG, "Прочитано " + fileBytes.length + " байт из файла экспорта.");

            // Создаем RequestBody для отправки
            okhttp3.RequestBody requestBody = okhttp3.RequestBody.create(
                    fileBytes,
                    okhttp3.MediaType.parse("application/json")
            );

            // Отправляем на сервер
            retrofit2.Response<okhttp3.ResponseBody> response =
                    NetworkClient.getImportApi().importFullData(requestBody).execute();

            if (response.isSuccessful()) {
                Log.d(TAG, "Импорт завершен успешно.");
                return true;
            } else {
                String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                Log.e(TAG, "Ошибка импорта: " + response.code() + " - " + errorBody);
                return false;
            }

        } catch (IOException e) {
            Log.e(TAG, "Сетевая ошибка при импорте данных", e);
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Непредвиденная ошибка при импорте", e);
            return false;
        }
    }
}
