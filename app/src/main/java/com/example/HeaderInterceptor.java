package com.example;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class HeaderInterceptor implements Interceptor {

    private final Context context;

    public HeaderInterceptor(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        
        // Get unique device ID safely
        String deviceId = "unknown";
        try {
            deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            if (deviceId == null) deviceId = "unknown_device";
        } catch (Exception e) {
            deviceId = "unknown_error";
        }

        Request.Builder builder = originalRequest.newBuilder()
                .header("X-Domain-Id", DomainManager.DOMAIN_ID)
                .header("X-App-Version", "1.0")
                .header("X-App-Name", "LearningApp")
                .header("X-Platform", "Android")
                .header("X-Device-Id", deviceId)
                .header("X-Android-Version", Build.VERSION.RELEASE)
                .header("X-Device-Model", Build.MODEL)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json");

        // Keep or append User-Agent
        String originalUa = originalRequest.header("User-Agent");
        if (originalUa != null) {
            builder.header("User-Agent", originalUa + " LearningApp/1.0");
        } else {
            builder.header("User-Agent", "LearningApp/1.0 Android/" + Build.VERSION.RELEASE);
        }

        return chain.proceed(builder.build());
    }
}
