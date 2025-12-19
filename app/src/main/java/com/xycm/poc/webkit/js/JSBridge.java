package com.xycm.poc.webkit.js;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.xycm.poc.api.config.TokenManager;
import com.xycm.poc.constants.Constants;
import com.xycm.poc.ui.CameraQRScannerActivity;
import com.xycm.poc.ui.FaceCaptureActivity;
import com.xycm.poc.ui.LoginActivity;
import com.xycm.poc.util.Result;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

public class JSBridge {

    private final WeakReference<Activity> activityRef;
    private final WeakReference<WebView> webViewRef;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    /**
     * 扫码 / 人脸等回调缓存
     */
    private static final Map<Integer, String> callbackMap = new HashMap<>();

    private Gson gson = new Gson();

    public JSBridge(Activity activity, WebView webView) {
        this.activityRef = new WeakReference<>(activity);
        this.webViewRef = new WeakReference<>(webView);
    }

    // ==========================
    // 基础能力
    // ==========================

    @JavascriptInterface
    public void showToast(String msg) {
        mainHandler.post(() -> Toast.makeText(activityRef.get(), msg, Toast.LENGTH_SHORT).show());
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
        mainHandler.post(() -> {
            TokenManager.getInstance().clearToken();
            TokenManager.getInstance().clearUserInfo();
            Activity act = activityRef.get();
            if (act != null) {
                LoginActivity.start(act);
            }
        });
    }

    // ==========================
    // 统一调用入口
    // ==========================

    @JavascriptInterface
    public void invoke(String action, String params, String callbackId) {
        Activity act = activityRef.get();
        if (act == null) return;

        switch (action) {
            case "scanQRCode":
                openQRScanner(callbackId);
                break;

            case "faceCapture":
                openFaceCapture(callbackId);
                break;

            default:
                sendError(callbackId, "未知 action: " + action);
                break;
        }
    }

    // ==========================
    // 扫码
    // ==========================

    private void openQRScanner(String callbackId) {
        Activity act = activityRef.get();
        if (act == null) return;

        Intent intent = new Intent(act, CameraQRScannerActivity.class);
        callbackMap.put(Constants.REQ_QR_SCAN, callbackId);
        act.startActivityForResult(intent, Constants.REQ_QR_SCAN);
    }

    // ==========================
    // 人脸采集
    // ==========================

    private void openFaceCapture(String callbackId) {
        Activity act = activityRef.get();
        if (act == null) return;

        Intent intent = new Intent(act, FaceCaptureActivity.class);
        callbackMap.put(Constants.REQ_FACE, callbackId);
        act.startActivityForResult(intent, Constants.REQ_FACE);
    }

    // ==========================
    // Activity 回调入口
    // ==========================

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        String callbackId = callbackMap.remove(requestCode);
        if (callbackId == null) return;

        if (resultCode != Activity.RESULT_OK || data == null) {
            sendError(callbackId, "用户取消");
            return;
        }

        String result = data.getStringExtra("result");
        sendSuccess(callbackId, result);
    }

    // ==========================
    // 回调 H5
    // ==========================

    private void sendSuccess(String callbackId, String result) {
        try {
            JSONObject data = new JSONObject();
            data.put("result", result);
            callJs(callbackId, gson.toJson(Result.ok(data)));
        } catch (Exception e) {
            sendError(callbackId, e.getMessage());
        }
    }

    private void sendError(String callbackId, String msg) {
        try {
            callJs(callbackId, gson.toJson(Result.fail(new JSONObject(), msg)));
        } catch (Exception ignored) {
        }
    }

    private void callJs(String callbackId, String json) {
        WebView webView = webViewRef.get();
        if (webView == null) return;
        mainHandler.post(() ->
                webView.evaluateJavascript("window.NativeBridgeCallback && NativeBridgeCallback('"
                                + callbackId + "', " + json + ")",
                        null
                )
        );
    }
}
