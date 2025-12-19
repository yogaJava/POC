package com.xycm.poc.ui;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.xycm.poc.R;
import com.xycm.poc.manager.CallManager;

/**
 * 语音通话页面（InCallActivity）
 * - 显示来电者信息
 * - 提供挂断、静音等功能
 */
public class InCallActivity extends AppCompatActivity {

    private Handler handler = new Handler(Looper.getMainLooper());
    private long startTime;

    /**
     * 启动 InCallActivity
     * @param context 上下文，可以是 Application 或 Activity
     */
    public static void start(Context context) {
        Intent intent = new Intent(context, InCallActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setShowWhenLocked(true);
        setTurnScreenOn(true);

        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        );

        setContentView(R.layout.activity_in_call);

        startTime = System.currentTimeMillis();
        startTimer();

        findViewById(R.id.btn_hangup).setOnClickListener(v -> {
            CallManager.getInstance().rejectCall();
            finish();
        });
    }

    private void startTimer() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                long sec = (System.currentTimeMillis() - startTime) / 1000;
                long min = sec / 60;
                sec = sec % 60;

                TextView tv = findViewById(R.id.tvTimer);
                tv.setText(String.format("%02d:%02d", min, sec));

                handler.postDelayed(this, 1000);
            }
        }, 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
