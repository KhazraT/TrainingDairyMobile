package ru.squidory.trainingdairymobile.data.remote;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

import java.util.List;

import ru.squidory.trainingdairymobile.data.model.ArticleResponse;

/**
 * API интерфейс для работы со статьями (информация)
 */
public interface ArticlesApi {
    /**
     * Получить все статьи (с возможностью фильтрации по категории)
     * GET /api/articles?category=TECHNIQUE
     */
    @GET("articles")
    Call<List<ArticleResponse>> getArticles(@Query("category") String category);

    /**
     * Получить статью по ID
     * GET /api/articles/{id}
     */
    @GET("articles/{id}")
    Call<ArticleResponse> getArticleById(@Path("id") Long id);
}
