package com.xycm.poc.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.xycm.poc.R;
import com.xycm.poc.webkit.config.CustomWebChromeClient;
import com.xycm.poc.webkit.config.CustomWebViewClient;
import com.xycm.poc.webkit.js.JSBridge;

public class LocalHtmlActivity extends BaseActivity {

    public WebView webView;
    public JSBridge jsBridge;

    @Override
    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_html);
        webView = findViewById(R.id.webView);
        initWebView();
        jsBridge = new JSBridge(this, webView);
        webView.addJavascriptInterface(jsBridge, "AndroidNative");
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
        webView.setWebChromeClient(new CustomWebChromeClient(null));
    }

    private void loadUrlWithToken() {
        webView.postDelayed(() -> webView.loadUrl("file:///android_asset/local/index.html"), 50);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (jsBridge != null) {
            jsBridge.onActivityResult(requestCode, resultCode, data);
        }
    }


}
