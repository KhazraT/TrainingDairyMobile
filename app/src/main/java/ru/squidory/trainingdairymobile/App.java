package ru.squidory.trainingdairymobile;

import android.app.Application;
import android.util.Log;
import timber.log.Timber;

public class App extends Application {

    private static App instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        
        // Инициализация логирования
        Timber.plant(new Timber.DebugTree());
        Log.d("App", "Application started");
    }

    public static App getInstance() {
        return instance;
    }
}
