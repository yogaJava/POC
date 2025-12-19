package com.xycm.poc.ui;

import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.xycm.poc.R;
import com.xycm.poc.manager.CallManager;

public class CallActivity extends AppCompatActivity {

    private Button btnHangup;
    private Button btnAccept;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);
        // 设置锁屏也能显示
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            );
        }
        btnAccept = findViewById(R.id.btn_accept);
        btnHangup = findViewById(R.id.btn_hangup);
        btnAccept.setOnClickListener(v -> CallManager.getInstance().startVideoCallActivity());
        btnHangup.setOnClickListener(view -> {
            CallManager.getInstance().rejectCall();
            finish();
        });
    }


    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0); // 关闭时也不动画
    }
}
