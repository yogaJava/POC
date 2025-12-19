package com.xycm.poc.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.xycm.poc.R;
import com.xycm.poc.api.ApiClient;
import com.xycm.poc.api.config.TokenManager;
import com.xycm.poc.api.req.LoginBody;
import com.xycm.poc.api.resp.LoginResponse;
import com.xycm.poc.api.resp.UserInfoResponse;
import com.xycm.poc.constants.Constants;
import com.xycm.poc.util.Result;
import com.xycm.poc.util.SignUtil;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends BaseActivity {

    private TextInputEditText etUsername, etPassword;
    private TextInputLayout tilUsername, tilPassword;
    private MaterialButton btnLogin;
    private ProgressBar pbLoading;
    private CheckBox checkboxAgree;

    private Gson gson = new Gson();

    /**
     * 启动 InCallActivity
     * @param context 上下文，可以是 Application 或 Activity
     */
    public static void start(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
        initClick();

        String token = getSharedPreferences("app_config", MODE_PRIVATE)
                .getString("token", "");
        if (!TextUtils.isEmpty(token)) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }
    }

    private void initView() {
        tilUsername = findViewById(R.id.til_username);
        tilPassword = findViewById(R.id.til_password);

        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);

        btnLogin = findViewById(R.id.btn_login);
        pbLoading = findViewById(R.id.pb_loading);

        checkboxAgree = findViewById(R.id.checkbox_agree);
        LinearLayout agreeLayout = findViewById(R.id.agree_layout);
        // 点击整个布局切换状态
        agreeLayout.setOnClickListener(v -> checkboxAgree.setChecked(!checkboxAgree.isChecked()));

        // 设置用户名图标
        setStartIconSize(tilUsername, R.drawable.ic_user, 32);
        // 设置密码图标
        setStartIconSize(tilPassword, R.drawable.ic_password, 32);
    }

    /**
     * 设置 TextInputLayout 左侧图标并自定义大小
     *
     * @param layout      TextInputLayout
     * @param drawableRes Drawable资源
     * @param sizeDp      大小 dp
     */
    private void setStartIconSize(TextInputLayout layout, int drawableRes, int sizeDp) {
        Drawable icon = ContextCompat.getDrawable(this, drawableRes);
        if (icon != null) {
            int sizePx = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, sizeDp, getResources().getDisplayMetrics());
            icon.setBounds(0, 0, sizePx, sizePx);
            layout.setStartIconDrawable(icon);
        }
    }

    private void initClick() {
        btnLogin.setOnClickListener(v -> doLogin());
    }

    private void doLogin() {
        String username = etUsername.getText() == null ? "" : etUsername.getText().toString().trim();
        String password = etPassword.getText() == null ? "" : etPassword.getText().toString().trim();
        // 参数校验
        if (TextUtils.isEmpty(username)) {
            tilUsername.setError("请输入账号");
            return;
        } else {
            tilUsername.setError(null);
        }
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("请输入密码");
            return;
        } else {
            tilPassword.setError(null);
        }
        if (!checkboxAgree.isChecked()) {
            Toast.makeText(this, "请勾选同意协议", Toast.LENGTH_SHORT).show();
            return;
        }
        // UI 进入 loading 状态
        showLoading(true);
        // 调用登录接口
        loginRequest(username, password);
    }

    private void showLoading(boolean loading) {
        btnLogin.setEnabled(!loading);
        btnLogin.setText(loading ? "" : getString(R.string.btn_login));
        pbLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void loginRequest(String username, String password) {
        // 登录
        LoginBody body = new LoginBody(username, password);
        ApiClient.getApiService().login(body).enqueue(new Callback<Result<LoginResponse>>() {
            @Override
            public void onResponse(@NonNull Call<Result<LoginResponse>> call, @NonNull Response<Result<LoginResponse>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    showError("登录失败，请重试");
                    return;
                }
                Result<LoginResponse> result = response.body();
                if (Result.isSuccess(result)) {
                    // 保存 Token
                    saveToken(result.getData().getAccess_token());
                    onLoginSuccess(result.getData());
                } else {
                    showError(result.getMsg());
                }
            }

            @Override
            public void onFailure(Call<Result<LoginResponse>> call, Throwable t) {
                showLoading(false);
                showError("网络异常，请检查网络");
            }

        });
    }

    private void onLoginSuccess(LoginResponse loginResponse) {
        // 后续请求，自动带 Token
        ApiClient.getApiServiceWithToken().getUserInfo()
                .enqueue(new Callback<Result<UserInfoResponse>>() {
                             @Override
                             public void onResponse(@NonNull Call<Result<UserInfoResponse>> call,
                                                    @NonNull Response<Result<UserInfoResponse>> response) {
                                 showLoading(false);
                                 if (!response.isSuccessful() || response.body() == null) {
                                     showError("登录失败，请重试");
                                     return;
                                 }
                                 Result<UserInfoResponse> result = response.body();
                                 if (Result.isSuccess(result)) {
                                     // 保存 Poc 用户名和密码
                                     savePocCredentials(loginResponse.getPocUser(), loginResponse.getPocPassword());
                                     saveUserInfo(result.getData());
                                     // 跳转主页
                                     Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                     startActivity(intent);
                                     // 关闭登录页（防止返回）
                                     finish();
                                 } else {
                                     showError(result.getMsg());
                                 }
                             }

                             @Override
                             public void onFailure(@NonNull Call<Result<UserInfoResponse>> call, Throwable t) {
                                 showLoading(false);
                                 showError(t.getMessage());
                             }
                         }
                );
    }

    private void saveUserInfo(UserInfoResponse userInfo) {
        if (userInfo == null) {
            return;
        }
        Map<String, Object> object = new HashMap<>();
        object.put("type", "object");
        object.put("data", userInfo);
        TokenManager.getInstance().saveUserInfo(gson.toJson(object));
    }

    private void showError(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void saveToken(String token) {
        TokenManager.getInstance().saveToken(token);
    }

    /**
     * 保存 Poc 用户名和密码（可选）
     */
    private void savePocCredentials(String poc_user, String poc_password) {
        if (poc_user == null || poc_password == null) {
            return;
        }
        getSharedPreferences("app_prefs", MODE_PRIVATE)
                .edit()
                .putString("poc_user", poc_user)
                .putString("poc_password", SignUtil.decryptData(Constants.getAppSecret(), poc_password))
                .apply();
    }
}
