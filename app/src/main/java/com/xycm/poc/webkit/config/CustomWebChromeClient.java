package com.xycm.poc.webkit.config;

import android.net.Uri;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.xycm.poc.ui.MainActivity;

public class CustomWebChromeClient extends WebChromeClient {

    private MainActivity activity;

    public CustomWebChromeClient(MainActivity activity) {
        this.activity = activity;
    }

    @Override
    public boolean onShowFileChooser(WebView webView,
                                     ValueCallback<Uri[]> filePathCallback,
                                     FileChooserParams fileChooserParams) {
        activity.openFileChooser(filePathCallback);
        return true;
    }
}
