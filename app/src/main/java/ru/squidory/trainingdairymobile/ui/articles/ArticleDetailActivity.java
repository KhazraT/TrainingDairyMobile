package ru.squidory.trainingdairymobile.ui.articles;

import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import ru.squidory.trainingdairymobile.R;
import ru.squidory.trainingdairymobile.data.model.ArticleResponse;
import ru.squidory.trainingdairymobile.data.repository.ArticlesRepository;

/**
 * Экран детального просмотра статьи
 */
public class ArticleDetailActivity extends AppCompatActivity {

    private ArticlesRepository repository;
    private ProgressBar progressBar;
    private TextView titleView, categoryView, dateView, contentView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail);

        repository = ArticlesRepository.getInstance();

        // Инициализация UI
        progressBar = findViewById(R.id.progressBar);
        titleView = findViewById(R.id.articleTitle);
        categoryView = findViewById(R.id.articleCategory);
        dateView = findViewById(R.id.articleDate);
        contentView = findViewById(R.id.articleContent);

        // Получение ID статьи из интента
        Long articleId = getIntent().getLongExtra("article_id", -1);
        if (articleId == -1) {
            Toast.makeText(this, "Ошибка: статья не найдена", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadArticle(articleId);
    }

    private void loadArticle(Long articleId) {
        progressBar.setVisibility(android.view.View.VISIBLE);

        repository.getArticleById(articleId, new ArticlesRepository.ArticleCallback() {
            @Override
            public void onSuccess(ArticleResponse article) {
                progressBar.setVisibility(android.view.View.GONE);
                displayArticle(article);
            }

            @Override
            public void onError(String errorMessage) {
                progressBar.setVisibility(android.view.View.GONE);
                Toast.makeText(ArticleDetailActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void displayArticle(ArticleResponse article) {
        titleView.setText(article.getTitle() != null ? article.getTitle() : "Без названия");

        // Категория
        String category = article.getCategory();
        if (category != null) {
            categoryView.setText(getReadableCategory(category));
            categoryView.setVisibility(android.view.View.VISIBLE);
        } else {
            categoryView.setVisibility(android.view.View.GONE);
        }

        // Дата
        String date = article.getCreatedAt();
        if (date != null && !date.isEmpty()) {
            if (date.contains("T")) {
                date = date.substring(0, date.indexOf("T"));
            }
            dateView.setText("Опубликовано: " + date);
            dateView.setVisibility(android.view.View.VISIBLE);
        } else {
            dateView.setVisibility(android.view.View.GONE);
        }

        // Контент
        String content = article.getContent() != null ? article.getContent() : "Контент отсутствует";
        // Заменяем экранированные переносы строк на реальные
        content = content.replace("\\n", "\n").replace("\\t", "\t");
        contentView.setText(content);
    }

    private String getReadableCategory(String category) {
        if (category == null) return "Без категории";
        switch (category.toUpperCase()) {
            case "TECHNIQUE": return "Техника";
            case "THEORY": return "Теория";
            case "NUTRITION": return "Питание";
            case "RECOVERY": return "Восстановление";
            case "PROGRAMMING": return "Составление программ";
            case "FAQ": return "FAQ";
            default: return category;
        }
    }
}
