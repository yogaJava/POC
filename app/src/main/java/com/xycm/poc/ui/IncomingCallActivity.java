package com.xycm.poc.ui;

import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.xycm.poc.R;
import com.xycm.poc.manager.CallManager;

public class IncomingCallActivity extends AppCompatActivity {

    private Button btnAccept, btnHangup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call);

        btnAccept = findViewById(R.id.btn_accept);
        btnHangup = findViewById(R.id.btn_hangup);

        btnAccept.setOnClickListener(v -> CallManager.getInstance().acceptCall());
        btnHangup.setOnClickListener(v -> CallManager.getInstance().rejectCall());

        // 显示锁屏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            );
        }
    }
}
