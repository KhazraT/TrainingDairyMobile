package ru.squidory.trainingdairymobile.ui.articles;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ru.squidory.trainingdairymobile.R;
import ru.squidory.trainingdairymobile.data.model.ArticleResponse;

/**
 * Адаптер для списка статей
 */
public class ArticlesAdapter extends RecyclerView.Adapter<ArticlesAdapter.ArticleViewHolder> {

    private List<ArticleResponse> articles = new ArrayList<>();
    private OnArticleClickListener listener;

    public interface OnArticleClickListener {
        void onArticleClick(ArticleResponse article);
    }

    public void setArticles(List<ArticleResponse> articles) {
        this.articles = articles != null ? articles : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setOnArticleClickListener(OnArticleClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_article, parent, false);
        return new ArticleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleViewHolder holder, int position) {
        ArticleResponse article = articles.get(position);
        holder.bind(article);
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    class ArticleViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleView;
        private final TextView categoryView;
        private final TextView dateView;

        public ArticleViewHolder(@NonNull View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.articleTitle);
            categoryView = itemView.findViewById(R.id.articleCategory);
            dateView = itemView.findViewById(R.id.articleDate);

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onArticleClick(articles.get(getAdapterPosition()));
                }
            });
        }

        public void bind(ArticleResponse article) {
            titleView.setText(article.getTitle() != null ? article.getTitle() : "Без названия");

            // Отображение категории с читаемым названием
            String category = article.getCategory();
            categoryView.setText(getReadableCategory(category));

            // Отображение даты создания
            String date = article.getCreatedAt();
            if (date != null && !date.isEmpty()) {
                // Обрезаем время, оставляем только дату (YYYY-MM-DD)
                if (date.contains("T")) {
                    date = date.substring(0, date.indexOf("T"));
                }
                dateView.setText(date);
                dateView.setVisibility(View.VISIBLE);
            } else {
                dateView.setVisibility(View.GONE);
            }
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
}
