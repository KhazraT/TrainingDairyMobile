package ru.squidory.trainingdairymobile.ui.statistics;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import ru.squidory.trainingdairymobile.R;
import ru.squidory.trainingdairymobile.ui.main.BaseFragment;

/**
 * Фрагмент раздела "Статистика".
 * В разработке...
 */
public class StatisticsFragment extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        TextView textView = new TextView(getContext());
        textView.setText(R.string.fragment_statistics);
        textView.setTextSize(24);
        
        int padding = getResources().getDimensionPixelSize(R.dimen.fragment_padding);
        textView.setPadding(padding, padding, padding, padding);
        textView.setGravity(android.view.Gravity.CENTER);
        
        return textView;
    }

    @Override
    public int getMenuItemId() {
        return R.id.navigation_statistics;
    }

    @Override
    public String getTitle() {
        return getString(R.string.fragment_statistics);
    }
}
