package ru.squidory.trainingdairymobile.util;

import android.util.Log;
import okhttp3.ResponseBody;
import retrofit2.Response;
import ru.squidory.trainingdairymobile.data.remote.NetworkClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Менеджер для полного экспорта и импорта данных пользователя.
 * Использует единый эндпоинт /api/export/full для получения всех данных (программы, упражнения, сессии с подходами).
 */
public class DataExportManager {

    private static final String TAG = "DataExportManager";

    /**
     * Экспортирует ВСЕ данные пользователя (программы, упражнения, сессии с подходами) в OutputStream.
     * Использует новый эндпоинт /api/export/full.
     *
     * @param os OutputStream для записи (например, из SAF)
     * @return true если успешно, false при ошибке
     */
    public static boolean exportFullData(OutputStream os) {
        try {
            Log.d(TAG, "Начало полного экспорта данных через /api/export/full...");

            // Вызов нового эндпоинта
            Response<ResponseBody> response = NetworkClient.getExportApi().getFullExport().execute();

            if (!response.isSuccessful() || response.body() == null) {
                String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                Log.e(TAG, "Ошибка экспорта: " + response.code() + " - " + errorBody);
                return false;
            }

            // Копируем данные из ResponseBody в OutputStream
            try (InputStream is = response.body().byteStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.flush();
            }

            Log.d(TAG, "Полный экспорт данных завершен успешно.");
            return true;

        } catch (IOException e) {
            Log.e(TAG, "Сетевая ошибка при экспорте данных", e);
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Непредвиденная ошибка при экспорте", e);
            return false;
        }
    }

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
