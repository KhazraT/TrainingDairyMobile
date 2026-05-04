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

}
