package com.xycm.poc.manager;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.mpttpnas.api.TrunkingCallSession;
import com.mpttpnas.pnaslibraryapi.PnasCallUtil;
import com.xycm.poc.service.IncomingCallForegroundService;
import com.xycm.poc.ui.CallActivity;
import com.xycm.poc.ui.VideoCallActivity;
import com.xycm.poc.ui.VoiceCallActivity;

/**
 * 呼叫管理器
 */
public class CallManager {

    private TrunkingCallSession currentSession;

    private Context appContext;

    private CallManager() {
    }

    private static final class InstanceHolder {
        static final CallManager instance = new CallManager();
    }

    public static CallManager getInstance() {
        return InstanceHolder.instance;
    }

    public void init(Context context) {
        this.appContext = context.getApplicationContext();
        // 注册 SDK 来电监听
        // ITrunkingService service = ...;
        // service.setListener(new PnasCallListener());
    }

    /**
     * 收到来电
     */
    public void onIncomingCall(TrunkingCallSession session) {
        Log.d("CallManager", "收到来电：" + session.getCaller());
        currentSession = session;

        // 启动前台服务显示通知
        IncomingCallForegroundService.start(appContext, session.getCaller());

        // 弹出 CallActivity（锁屏/点亮屏幕）
        Intent intent = new Intent(appContext, CallActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        appContext.startActivity(intent);
    }


    /**
     * 接听语音
     */
    public void acceptCall() {
        if (currentSession != null && currentSession.isIncoming() && currentSession.isBeforeConfirmed()) {
            PnasCallUtil.getInstance().acceptCall(currentSession.getCallId());
            IncomingCallForegroundService.stop(appContext);
        }
    }

    /**
     * 接听视频
     */
    public void acceptVideoCall() {
        if (currentSession != null && currentSession.isIncoming() && currentSession.isBeforeConfirmed()) {
            PnasCallUtil.getInstance().acceptCall(currentSession.getCallId());
            IncomingCallForegroundService.stop(appContext);
        }
    }

    /**
     * 拒接 / 挂断
     */
    public void rejectCall() {
        if (currentSession != null) {
            PnasCallUtil.getInstance().hangupCall(currentSession.getCallId(),
                    currentSession.isOngoing() ?
                            PnasCallUtil.HangupStatus.HANGUP :
                            PnasCallUtil.HangupStatus.DECLINE);
            IncomingCallForegroundService.stop(appContext);
            currentSession = null;
        }
    }

//    public void startCallActivity(TrunkingCallSession session) {
//        currentSession = session;
//        Intent intent = new Intent(appContext, CallActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        appContext.startActivity(intent);
//    }

    public void startVoiceCallActivity() {
        if (appContext == null) return;
        Intent intent = new Intent(appContext, VoiceCallActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        appContext.startActivity(intent);
    }

    public void startVideoCallActivity() {
        if (appContext == null) {
            return;
        }
        Intent intent = new Intent(appContext, VideoCallActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        appContext.startActivity(intent);
    }

    public TrunkingCallSession getCurrentSession() {
        return currentSession;
    }
}
