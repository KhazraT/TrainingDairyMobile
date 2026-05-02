package ru.squidory.trainingdairymobile.data.remote.api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;

/**
 * API для полного экспорта и импорта данных пользователя.
 * Эндпоинты: /api/export/full
 */
public interface ExportApi {

    /**
     * Получить полный экспорт всех данных пользователя (программы, упражнения, сессии с подходами).
     * GET /api/export/full
     *
     * @return ResponseBody (JSON строка) для сохранения в файл.
     */
    @GET("export/full")
    Call<ResponseBody> getFullExport();
}
