package ru.squidory.trainingdairymobile.ui.articles;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.chip.ChipGroup;

import java.util.List;

import ru.squidory.trainingdairymobile.R;
import ru.squidory.trainingdairymobile.data.model.ArticleResponse;
import ru.squidory.trainingdairymobile.data.repository.ArticlesRepository;

/**
 * Фрагмент статей (Информация)
 */
public class ArticlesFragment extends Fragment {

    private ArticlesRepository repository;
    private ArticlesAdapter adapter;
    private View emptyStateLayout;
    private android.widget.ProgressBar progressBar;
    private ChipGroup chipGroup;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = ArticlesRepository.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_articles, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Инициализация UI
        androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefreshLayout =
                view.findViewById(R.id.swipeRefreshLayout);
        androidx.recyclerview.widget.RecyclerView recyclerView =
                view.findViewById(R.id.articlesRecyclerView);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        progressBar = view.findViewById(R.id.progressBar);
        chipGroup = view.findViewById(R.id.categoryChipGroup);

        // Настройка RecyclerView
        adapter = new ArticlesAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Обработка клика по статье
        adapter.setOnArticleClickListener(article -> {
            android.content.Intent intent = new android.content.Intent(getContext(),
                    ArticleDetailActivity.class);
            intent.putExtra("article_id", article.getId());
            startActivity(intent);
        });

        // Настройка фильтра категорий
        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String category = getCategoryFromChipId(checkedId);
            loadArticles(category);
        });

        // Настройка SwipeRefresh
        swipeRefreshLayout.setOnRefreshListener(() -> {
            String category = getCategoryFromChipId(
                    chipGroup.getCheckedChipId());
            loadArticles(category);
            swipeRefreshLayout.setRefreshing(false);
        });

        // Сброс чипа на "Все" после восстановления состояния
        chipGroup.post(() -> {
            chipGroup.setOnCheckedChangeListener(null);
            chipGroup.clearCheck();
            chipGroup.check(R.id.chipAll);
            chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
                String category = getCategoryFromChipId(checkedId);
                loadArticles(category);
            });
            loadArticles(null);
        });
    }

    private String getCategoryFromChipId(int chipId) {
        if (chipId == R.id.chipTechnique) return "TECHNIQUE";
        if (chipId == R.id.chipTheory) return "THEORY";
        if (chipId == R.id.chipNutrition) return "NUTRITION";
        if (chipId == R.id.chipRecovery) return "RECOVERY";
        if (chipId == R.id.chipProgramming) return "PROGRAMMING";
        if (chipId == R.id.chipFaq) return "FAQ";
        return null; // Все
    }

    private void loadArticles(String category) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        emptyStateLayout.setVisibility(View.GONE);

        repository.getArticles(category, new ArticlesRepository.ArticlesCallback() {
            @Override
            public void onSuccess(List<ArticleResponse> articles) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                adapter.setArticles(articles);
                if (articles == null || articles.isEmpty()) {
                    emptyStateLayout.setVisibility(View.VISIBLE);
                } else {
                    emptyStateLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
