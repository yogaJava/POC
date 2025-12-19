package com.xycm.poc.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.mpttpnas.pnaslibraryapi.PnasCallUtil;
import com.xycm.poc.R;
import com.xycm.poc.manager.CallManager;

/**
 * 视频通话页面
 */
public class VideoCallActivity extends AppCompatActivity {

    public static void start(Context context) {
        Intent intent = new Intent(context, VideoCallActivity.class);
        if (!(context instanceof AppCompatActivity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call);
        Button hangupBtn = findViewById(R.id.hangup_button);
        hangupBtn.setOnClickListener(v -> {
            CallManager.getInstance().rejectCall();
            finish();
        });
        CallManager.getInstance().acceptCall();
        Button switchCameraBtn = findViewById(R.id.switch_camera_button);
        switchCameraBtn.setOnClickListener(v -> PnasCallUtil.getInstance().switchCamera());
    }
}
