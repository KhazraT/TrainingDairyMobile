package ru.squidory.trainingdairymobile;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import ru.squidory.trainingdairymobile.data.repository.AuthRepository;
import ru.squidory.trainingdairymobile.ui.auth.LoginActivity;
import ru.squidory.trainingdairymobile.ui.main.MainActivity;

/**
 * Splash экран для проверки авторизации при запуске приложения.
 * Перенаправляет пользователя на главный экран или экран входа.
 */
public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 500; // Задержка 500мс для показа заставки
    private boolean hasNavigated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Небольшая задержка для плавного перехода
        new Handler(Looper.getMainLooper()).postDelayed(this::checkAuthAndNavigate, SPLASH_DELAY);
    }

    private void checkAuthAndNavigate() {
        if (hasNavigated) return;
        hasNavigated = true;

        AuthRepository authRepository = AuthRepository.getInstance();

        Intent intent;
        if (authRepository.isLoggedIn()) {
            // Пользователь авторизован - переходим на главный экран
            intent = new Intent(this, MainActivity.class);
        } else {
            // Пользователь не авторизован - переходим на экран входа
            intent = new Intent(this, LoginActivity.class);
        }

        // Отключаем анимацию перехода
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Не вызываем checkAuthAndNavigate() здесь чтобы избежать двойной навигации
    }
}
