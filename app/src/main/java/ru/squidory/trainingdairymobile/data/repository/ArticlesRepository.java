package ru.squidory.trainingdairymobile.data.repository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.squidory.trainingdairymobile.data.model.ArticleResponse;
import ru.squidory.trainingdairymobile.data.remote.ArticlesApi;
import ru.squidory.trainingdairymobile.data.remote.NetworkClient;

import java.util.List;

/**
 * Репозиторий для работы со статьями (информация)
 */
public class ArticlesRepository {
    private static volatile ArticlesRepository instance;
    private final ArticlesApi articlesApi;

    private ArticlesRepository() {
        articlesApi = NetworkClient.getArticlesApi();
    }

    public static ArticlesRepository getInstance() {
        if (instance == null) {
            synchronized (ArticlesRepository.class) {
                if (instance == null) {
                    instance = new ArticlesRepository();
                }
            }
        }
        return instance;
    }

    /**
     * Получить все статьи (с фильтрацией по категории)
     */
    public void getArticles(String category, ArticlesCallback callback) {
        articlesApi.getArticles(category).enqueue(new Callback<List<ArticleResponse>>() {
            @Override
            public void onResponse(Call<List<ArticleResponse>> call, Response<List<ArticleResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Ошибка загрузки статей: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<ArticleResponse>> call, Throwable t) {
                callback.onError("Ошибка сети: " + t.getMessage());
            }
        });
    }

    /**
     * Получить статью по ID
     */
    public void getArticleById(Long id, ArticleCallback callback) {
        articlesApi.getArticleById(id).enqueue(new Callback<ArticleResponse>() {
            @Override
            public void onResponse(Call<ArticleResponse> call, Response<ArticleResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Статья не найдена: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ArticleResponse> call, Throwable t) {
                callback.onError("Ошибка сети: " + t.getMessage());
            }
        });
    }

    // Callback interfaces
    public interface ArticlesCallback {
        void onSuccess(List<ArticleResponse> articles);
        void onError(String errorMessage);
    }

    public interface ArticleCallback {
        void onSuccess(ArticleResponse article);
        void onError(String errorMessage);
    }
}
