package com.xycm.poc.api;

import com.xycm.poc.api.config.AuthInterceptor;
import com.xycm.poc.api.service.ApiService;
import com.xycm.poc.constants.Constants;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    // 必须以 / 结尾
    private static final String BASE_URL = Constants.BASE_URL;
    private static Retrofit retrofitNoToken;
    private static Retrofit retrofitWithToken;


    public static Retrofit getInstance() {
        if (retrofitNoToken == null) {
            // 日志拦截器（调试用）
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .addInterceptor(loggingInterceptor)
                    .build();

            retrofitNoToken = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofitNoToken;
    }

    /**
     * 其他请求，自动带 Token
     */
    public static ApiService getApiServiceWithToken() {
        if (retrofitWithToken == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .addInterceptor(new AuthInterceptor())
                    .addInterceptor(logging)
                    .build();

            retrofitWithToken = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofitWithToken.create(ApiService.class);
    }

    /**
     * 添加这个方法直接返回 ApiService
     */
    public static ApiService getApiService() {
        return getInstance().create(ApiService.class);
    }
}
