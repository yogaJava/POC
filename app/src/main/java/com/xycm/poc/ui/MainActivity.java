package com.xycm.poc.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.mpttpnas.pnaslibraryapi.PnasConfigUtil;
import com.mpttpnas.pnaslibraryapi.PnasSDK;
import com.mpttpnas.pnaslibraryapi.PnasUserUtil;
import com.mpttpnas.pnaslibraryapi.callback.StackStartSuccessCallbackEvent;
import com.xycm.poc.R;
import com.xycm.poc.api.config.TokenManager;
import com.xycm.poc.constants.Constants;
import com.xycm.poc.service.PnasCallListener;
import com.xycm.poc.webkit.config.CustomWebChromeClient;
import com.xycm.poc.webkit.config.CustomWebViewClient;
import com.xycm.poc.webkit.js.JSBridge;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import cn.hutool.core.util.StrUtil;

public class MainActivity extends BaseActivity {

    private final Handler handler = new Handler(Looper.getMainLooper());

    public WebView webView;


    @Override
    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        initSdk();
        webView = findViewById(R.id.webView);
        initWebView();
        webView.addJavascriptInterface(new JSBridge(this), "AndroidNative");
        loadUrlWithToken();
    }

    private void initWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        webView.setWebViewClient(new CustomWebViewClient());
        webView.setWebChromeClient(new CustomWebChromeClient());
    }

    private void loadUrlWithToken() {
        // String baseUrl = Constants.H5_URL;
        String baseUrl = Constants.DEBUG_H5_URL;
        String loadUrl = baseUrl + "/xfh5/#/pages/index";
        String token = TokenManager.getInstance().getToken();
        String userInfo = TokenManager.getInstance().getUserInfo();
        String preloadJs = buildPreloadJs(token, userInfo);
        String html = "<!DOCTYPE html><html><head>" +
                "<meta charset='utf-8'>" +
                "<script>" + preloadJs + "</script>" +
                "</head>" +
                "<body></body></html>";
        webView.loadDataWithBaseURL(baseUrl, html, "text/html",
                "UTF-8", null
        );
        webView.postDelayed(() -> webView.loadUrl(loadUrl), 50);
    }

    private String buildPreloadJs(String token, String userInfo) {
        StringBuilder js = new StringBuilder();
        js.append("(function(){");

        if (StrUtil.isNotBlank(token)) {
            js.append("localStorage.setItem('App-Token', '")
                    .append(token)
                    .append("');");
        }

        if (StrUtil.isNotBlank(userInfo)) {
            js.append("localStorage.setItem('userInfo', '")
                    .append(userInfo.replace("'", "\\'"))
                    .append("');");
        }

        js.append("})();");
        return js.toString();
    }


    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();

    }

    private void initSdk() {
        PnasConfigUtil.getInstance().setUseHttps(true);
        PnasConfigUtil.getInstance().setUseDMSConfig(true);
        PnasConfigUtil.getInstance().setLogLevel(6);
        PnasSDK.getInstance().init(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStackStartSuccessCallbackEvent(StackStartSuccessCallbackEvent event) {
        if (event.getIsSuccess() == 1) {
            setupService();
        }
    }

    private void setupService() {
        String poc_user = getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getString("poc_user", "");
        String poc_password = getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getString("poc_password", "");
        if (StrUtil.isBlank(poc_user)
                || StrUtil.isBlank(poc_password)) {
            System.out.println(poc_user);
            return;
        }
        handler.postDelayed(() -> {
            try {
                // 登录
                // PnasUserUtil.getInstance().login(Constants.LOGIN_UDN, Constants.LOGIN_PWD, Constants.SERVER, null);
                PnasUserUtil.getInstance().login(poc_user, poc_password, Constants.SERVER, null);
                if (PnasSDK.getInstance().getService() != null) {
                    // 设置呼叫监听器
                    PnasSDK.getInstance().getService().setListener(new PnasCallListener());
                } else {
                    // service 仍然为 null，延迟重试
                    handler.postDelayed(this::setupService, 500);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }, 500);
    }

}
