package ru.squidory.trainingdairymobile.data.remote;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.squidory.trainingdairymobile.data.remote.AuthAuthenticator;
import ru.squidory.trainingdairymobile.data.remote.api.AntiGSessionApi;
import ru.squidory.trainingdairymobile.data.remote.api.AuthApi;
import ru.squidory.trainingdairymobile.data.remote.api.ExerciseApi;
import ru.squidory.trainingdairymobile.data.remote.api.ExportApi;
import ru.squidory.trainingdairymobile.data.remote.api.ImportApi;
import ru.squidory.trainingdairymobile.data.remote.api.ProgramApi;
import ru.squidory.trainingdairymobile.data.remote.api.SessionApi;
import ru.squidory.trainingdairymobile.data.remote.api.StatisticsApi;
import ru.squidory.trainingdairymobile.data.remote.api.UserApi;
import ru.squidory.trainingdairymobile.data.remote.ArticlesApi;
import ru.squidory.trainingdairymobile.util.Constants;

public class NetworkClient {

    private static Retrofit retrofit;
    private static AuthApi authApi;
    private static AntiGSessionApi antiGSessionApi;
    private static ExerciseApi exerciseApi;
    private static ExportApi exportApi;
    private static ImportApi importApi;
    private static ProgramApi programApi;
    private static SessionApi sessionApi;
    private static StatisticsApi statisticsApi;
    private static UserApi userApi;
    private static ArticlesApi articlesApi;

    private static Retrofit getRetrofit() {
        if (retrofit == null) {
            // Логирование для отладки
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Interceptor для авторизации
            AuthInterceptor authInterceptor = new AuthInterceptor();
            AuthAuthenticator authAuthenticator = new AuthAuthenticator();

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                    .addInterceptor(loggingInterceptor)
                    .addInterceptor(authInterceptor)
                    .authenticator(authAuthenticator)
                    .build();

            Gson gson = new GsonBuilder()
                    .serializeNulls()
                    .registerTypeAdapter(OffsetDateTime.class, new TypeAdapter<OffsetDateTime>() {
                        private final DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

                        @Override
                        public void write(JsonWriter out, OffsetDateTime value) throws IOException {
                            if (value == null) {
                                out.nullValue();
                            } else {
                                out.value(formatter.format(value));
                            }
                        }

                        @Override
                        public OffsetDateTime read(JsonReader in) throws IOException {
                            if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
                                in.nextNull();
                                return null;
                            }
                            return OffsetDateTime.parse(in.nextString(), formatter);
                        }
                    })
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
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

    public static AntiGSessionApi getAntiGSessionApi() {
        if (antiGSessionApi == null) {
            antiGSessionApi = getRetrofit().create(AntiGSessionApi.class);
        }
        return antiGSessionApi;
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

    public static ExportApi getExportApi() {
        if (exportApi == null) {
            exportApi = getRetrofit().create(ExportApi.class);
        }
        return exportApi;
    }

    public static ImportApi getImportApi() {
        if (importApi == null) {
            importApi = getRetrofit().create(ImportApi.class);
        }
        return importApi;
    }

    public static ArticlesApi getArticlesApi() {
        if (articlesApi == null) {
            articlesApi = getRetrofit().create(ArticlesApi.class);
        }
        return articlesApi;
    }
}
