package com.xycm.poc.api.config;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        Request.Builder builder = original.newBuilder();
        String token = TokenManager.getInstance().getToken();
        if (token != null && !token.isEmpty()) {
            builder.header("authorization", "Bearer " + token);
        }
        return chain.proceed(builder.build());
    }
}
