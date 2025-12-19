package com.xycm.poc.webkit.js;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.xycm.poc.api.config.TokenManager;
import com.xycm.poc.ui.CameraQRScannerActivity;
import com.xycm.poc.ui.FaceCaptureActivity;
import com.xycm.poc.ui.LoginActivity;

public class JSBridge {

    private final Context context;

    private final Handler handler = new Handler(Looper.getMainLooper());

    public JSBridge(Context context) {
        this.context = context;
    }

    @JavascriptInterface
    public void showToast(String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public String getToken() {
        return TokenManager.getInstance().getToken();
    }

    @JavascriptInterface
    public void refreshToken(String token) {
        TokenManager.getInstance().saveToken(token);
    }

    @JavascriptInterface
    public void onTokenExpired() {
        handler.post(() -> {
            TokenManager.getInstance().clearToken();
            TokenManager.getInstance().clearUserInfo();
            if (context instanceof Activity) {
                LoginActivity.start((Activity) context);
            }
        });
    }

    /**
     * 人脸识别
     */
    @JavascriptInterface
    public void startFaceCapture() {
        handler.post(() -> {
            try {
                Intent intent = new Intent(context, FaceCaptureActivity.class);
                if (!(context instanceof Activity)) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                context.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 扫描二维码
     */
    @JavascriptInterface
    public void showQRScanner() {
        handler.post(() -> {
            try {
                Intent intent = new Intent(context, CameraQRScannerActivity.class);
                if (!(context instanceof Activity)) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                context.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

}