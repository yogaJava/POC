package com.xycm.poc.webkit.js;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

public class JSBridge {

    private final Context context;

    public JSBridge(Context context) {
        this.context = context;
    }

    @JavascriptInterface
    public void showToast(String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public String getToken() {
        return context.getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getString("token", "");
    }

    @JavascriptInterface
    public void refreshToken(String token) {
        context.getSharedPreferences("app_prefs", MODE_PRIVATE)
                .edit()
                .putString("token", token)
                .apply();
    }

}