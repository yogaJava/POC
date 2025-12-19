package com.xycm.poc.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.xycm.poc.R;
import com.xycm.poc.webkit.config.CustomWebChromeClient;
import com.xycm.poc.webkit.config.CustomWebViewClient;
import com.xycm.poc.webkit.js.JSBridge;

public class LocalHtmlActivity extends BaseActivity {

    public WebView webView;

    @Override
    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_html);
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
        webView.setWebChromeClient(new CustomWebChromeClient(null));
    }

    private void loadUrlWithToken() {
        webView.postDelayed(() -> webView.loadUrl("file:///android_asset/www/index.html"), 50);
    }

}
