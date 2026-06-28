package com.example;

import android.content.Context;
import java.util.concurrent.TimeUnit;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

public class NetworkManager {

    private static NetworkManager instance;
    private final OkHttpClient okHttpClient;
    private final Retrofit retrofit;

    private NetworkManager(Context context) {
        Context appContext = context.getApplicationContext();

        // 10 MB Cache
        Cache cache = new Cache(appContext.getCacheDir(), 10 * 1024 * 1024);

        this.okHttpClient = new OkHttpClient.Builder()
                .cache(cache)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(new HeaderInterceptor(appContext))
                .build();

        this.retrofit = new Retrofit.Builder()
                .baseUrl("https://api.appxlearn.com/")
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create())
                .build();
    }

    public static synchronized NetworkManager getInstance(Context context) {
        if (instance == null) {
            instance = new NetworkManager(context);
        }
        return instance;
    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }
}
