package com.xycm.poc.api.config;

import android.content.Context;

public class TokenManager {

    private Context context;

    private String token;

    private String userInfo;

    private TokenManager() {

    }

    private static final class InstanceHolder {
        static final TokenManager instance = new TokenManager();
    }

    public static synchronized TokenManager getInstance() {
        return InstanceHolder.instance;
    }

    public void init(Context context) {
        this.context = context;
    }

    public void saveToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return this.token;
    }

    public void clearToken() {
        this.token = null;
    }

    public void saveUserInfo(String userInfo) {
        this.userInfo = userInfo;
    }

    public String getUserInfo() {
        return this.userInfo;
    }

}
