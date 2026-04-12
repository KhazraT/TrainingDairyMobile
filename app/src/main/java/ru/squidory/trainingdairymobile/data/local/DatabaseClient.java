package ru.squidory.trainingdairymobile.data.local;

import android.content.Context;
import androidx.room.Room;
import ru.squidory.trainingdairymobile.App;
import ru.squidory.trainingdairymobile.data.local.dao.ActiveSessionDao;
import ru.squidory.trainingdairymobile.data.local.dao.ExerciseDao;
import ru.squidory.trainingdairymobile.data.local.dao.ProgramDao;
import ru.squidory.trainingdairymobile.data.local.dao.SessionDao;
import ru.squidory.trainingdairymobile.data.local.dao.UserDao;

public class DatabaseClient {

    private static AppDatabase database;
    private static ExerciseDao exerciseDao;
    private static ProgramDao programDao;
    private static SessionDao sessionDao;
    private static ActiveSessionDao activeSessionDao;
    private static UserDao userDao;

    private static AppDatabase getDatabase() {
        if (database == null) {
            database = Room.databaseBuilder(
                    App.getInstance(),
                    AppDatabase.class,
                    "training_diary_database"
            )
            .fallbackToDestructiveMigration()
            .build();
        }
        return database;
    }

    public static ExerciseDao getExerciseDao() {
        if (exerciseDao == null) {
            exerciseDao = getDatabase().exerciseDao();
        }
        return exerciseDao;
    }

    public static ProgramDao getProgramDao() {
        if (programDao == null) {
            programDao = getDatabase().programDao();
        }
        return programDao;
    }

    public static SessionDao getSessionDao() {
        if (sessionDao == null) {
            sessionDao = getDatabase().sessionDao();
        }
        return sessionDao;
    }

    public static ActiveSessionDao getActiveSessionDao() {
        if (activeSessionDao == null) {
            activeSessionDao = getDatabase().activeSessionDao();
        }
        return activeSessionDao;
    }

    public static UserDao getUserDao() {
        if (userDao == null) {
            userDao = getDatabase().userDao();
        }
        return userDao;
    }
}
