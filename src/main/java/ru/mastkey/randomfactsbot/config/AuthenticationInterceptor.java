package ru.mastkey.randomfactsbot.config;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class AuthenticationInterceptor implements Interceptor {
    private final String token;


    public AuthenticationInterceptor(String token) {
        this.token = token;
    }

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        Request request = chain.request()
                .newBuilder()
                .header("Authorization", "Bearer" + token)
                .build();
        return chain.proceed(request);
    }

}