package com.xycm.poc.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

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

import java.io.File;
import java.io.IOException;

import cn.hutool.core.util.StrUtil;

public class MainActivity extends BaseActivity {

    private final Handler handler = new Handler(Looper.getMainLooper());

    public WebView webView;


    public static final int FILE_CHOOSER_REQUEST_CODE = 1001;
    private static final int PERMISSION_REQUEST_CODE = 2001;
    private ValueCallback<Uri[]> filePathCallback;
    private Uri cameraUri;


    /**
     * 启动 MainActivity
     *
     * @param context 上下文，可以是 Application 或 Activity
     */
    public static void start(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

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
        webView.setWebChromeClient(new CustomWebChromeClient(this));
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

    // --- 文件选择回调 ---
    public void openFileChooser(ValueCallback<Uri[]> callback) {
        this.filePathCallback = callback;
        requestPermissionsAndShowChooser();
    }

    private void requestPermissionsAndShowChooser() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_CODE);
                return;
            }
        }
        showFileChooser();
    }

    private void showFileChooser() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (photoFile != null) {
            cameraUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
        }
        Intent pickIntent = new Intent(Intent.ACTION_GET_CONTENT);
        pickIntent.addCategory(Intent.CATEGORY_OPENABLE);
        pickIntent.setType("*/*");
        Intent chooser = Intent.createChooser(pickIntent, "选择文件或拍照");
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{takePictureIntent});
        }
        startActivityForResult(chooser, FILE_CHOOSER_REQUEST_CODE);
    }

    private File createImageFile() throws IOException {
        String fileName = "IMG_" + System.currentTimeMillis();
        File storageDir = getExternalFilesDir("Pictures");
        return new File(storageDir, fileName + ".jpg");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == FILE_CHOOSER_REQUEST_CODE) {
            if (filePathCallback != null) {
                Uri[] results = null;
                if (resultCode == RESULT_OK) {
                    if (data == null || data.getData() == null) {
                        if (cameraUri != null) {
                            results = new Uri[]{cameraUri};
                        }
                    } else {
                        results = new Uri[]{data.getData()};
                    }
                }
                filePathCallback.onReceiveValue(results);
                filePathCallback = null;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            showFileChooser();
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
