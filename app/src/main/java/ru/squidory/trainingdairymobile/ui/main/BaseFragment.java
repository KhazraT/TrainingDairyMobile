package ru.squidory.trainingdairymobile.ui.main;

import androidx.fragment.app.Fragment;

/**
 * Базовый класс для всех фрагментов главной навигации.
 */
public abstract class BaseFragment extends Fragment {

    /**
     * Возвращает ID элемента меню, связанного с этим фрагментом.
     */
    public abstract int getMenuItemId();

    /**
     * Возвращает заголовок фрагмента.
     */
    public abstract String getTitle();
}
