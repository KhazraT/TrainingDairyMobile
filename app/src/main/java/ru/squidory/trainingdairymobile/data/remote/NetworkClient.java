package ru.squidory.trainingdairymobile.data.remote;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.squidory.trainingdairymobile.data.remote.api.AuthApi;
import ru.squidory.trainingdairymobile.data.remote.api.ExerciseApi;
import ru.squidory.trainingdairymobile.data.remote.api.ProgramApi;
import ru.squidory.trainingdairymobile.data.remote.api.SessionApi;
import ru.squidory.trainingdairymobile.data.remote.api.StatisticsApi;
import ru.squidory.trainingdairymobile.data.remote.api.UserApi;
import ru.squidory.trainingdairymobile.util.Constants;

public class NetworkClient {

    private static Retrofit retrofit;
    private static AuthApi authApi;
    private static ExerciseApi exerciseApi;
    private static ProgramApi programApi;
    private static SessionApi sessionApi;
    private static StatisticsApi statisticsApi;
    private static UserApi userApi;

    private static Retrofit getRetrofit() {
        if (retrofit == null) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static AuthApi getAuthApi() {
        if (authApi == null) {
            authApi = getRetrofit().create(AuthApi.class);
        }
        return authApi;
    }

    public static ExerciseApi getExerciseApi() {
        if (exerciseApi == null) {
            exerciseApi = getRetrofit().create(ExerciseApi.class);
        }
        return exerciseApi;
    }

    public static ProgramApi getProgramApi() {
        if (programApi == null) {
            programApi = getRetrofit().create(ProgramApi.class);
        }
        return programApi;
    }

    public static SessionApi getSessionApi() {
        if (sessionApi == null) {
            sessionApi = getRetrofit().create(SessionApi.class);
        }
        return sessionApi;
    }

    public static StatisticsApi getStatisticsApi() {
        if (statisticsApi == null) {
            statisticsApi = getRetrofit().create(StatisticsApi.class);
        }
        return statisticsApi;
    }

    public static UserApi getUserApi() {
        if (userApi == null) {
            userApi = getRetrofit().create(UserApi.class);
        }
        return userApi;
    }
}
