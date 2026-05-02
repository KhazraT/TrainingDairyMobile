package ru.squidory.trainingdairymobile.data.remote.api;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * API для импорта данных пользователя.
 * Эндпоинт: POST /api/import/full
 */
public interface ImportApi {

    /**
     * Импортировать полные данные пользователя из JSON.
     * POST /api/import/full
     *
     * @param body RequestBody содержащий JSON файл экспорта
     * @return ResponseBody с результатом импорта
     */
    @POST("import/full")
    Call<ResponseBody> importFullData(@Body RequestBody body);
}
