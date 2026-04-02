package ru.squidory.trainingdairymobile.ui.main;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import ru.squidory.trainingdairymobile.R;
import ru.squidory.trainingdairymobile.data.repository.AuthRepository;
import ru.squidory.trainingdairymobile.ui.articles.ArticlesFragment;
import ru.squidory.trainingdairymobile.ui.auth.LoginActivity;
import ru.squidory.trainingdairymobile.ui.exercises.ExercisesFragment;
import ru.squidory.trainingdairymobile.ui.profile.ProfileFragment;
import ru.squidory.trainingdairymobile.ui.statistics.StatisticsFragment;
import ru.squidory.trainingdairymobile.ui.trainings.TrainingsFragment;

/**
 * Главная Activity приложения.
 * Содержит Bottom Navigation с 5 вкладками:
 * - Тренировки
 * - Статистика
 * - Упражнения
 * - Информация
 * - Профиль
 */
public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;
    private AuthRepository authRepository;

    // Фрагменты
    private final TrainingsFragment trainingsFragment = new TrainingsFragment();
    private final StatisticsFragment statisticsFragment = new StatisticsFragment();
    private final ExercisesFragment exercisesFragment = new ExercisesFragment();
    private final ArticlesFragment articlesFragment = new ArticlesFragment();
    private final ProfileFragment profileFragment = new ProfileFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        authRepository = AuthRepository.getInstance();

        initViews();
        setupNavigation();
        
        // Загружаем первый фрагмент по умолчанию
        if (savedInstanceState == null) {
            loadFragment(trainingsFragment);
            bottomNavigation.setSelectedItemId(R.id.navigation_trainings);
        }
    }

    private void initViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void setupNavigation() {
        bottomNavigation.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                
                int itemId = item.getItemId();
                if (itemId == R.id.navigation_trainings) {
                    selectedFragment = trainingsFragment;
                } else if (itemId == R.id.navigation_statistics) {
                    selectedFragment = statisticsFragment;
                } else if (itemId == R.id.navigation_exercises) {
                    selectedFragment = exercisesFragment;
                } else if (itemId == R.id.navigation_articles) {
                    selectedFragment = articlesFragment;
                } else if (itemId == R.id.navigation_profile) {
                    selectedFragment = profileFragment;
                }
                
                if (selectedFragment != null) {
                    loadFragment(selectedFragment);
                    return true;
                }
                
                return false;
            }
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    /**
     * Выход из аккаунта.
     * Вызывается из ProfileFragment.
     */
    public void logout() {
        authRepository.logout();
        
        // Переход на экран входа
        LoginActivity.startActivityClearTop(this);
        finish();
    }
}
