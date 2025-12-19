package com.xycm.poc.service;

import android.os.Bundle;
import android.os.RemoteException;

import com.mpttpnas.api.ITrunkingListener;
import com.mpttpnas.api.TrunkingCallSession;
import com.mpttpnas.api.TrunkingGroupContact;
import com.mpttpnas.api.TrunkingMessage;
import com.mpttpnas.api.TrunkingProfileState;
import com.xycm.poc.manager.CallManager;

import java.util.List;

/**
 * 状态监听器
 */
public class PnasCallListener extends ITrunkingListener.Stub {

    @Override
    public void onTrunkingCallStateChanged(TrunkingCallSession session) {
        // 1. 通话结束
        if (session.isAfterEnded()) {
            CallManager.getInstance().rejectCall();
            return;
        }

        // 2. 来电处理
        if (session.isIncoming() && session.isBeforeConfirmed()) {
            // 弹窗 + 响铃
            CallManager.getInstance().onIncomingCall(session);
            return;
        }

        // 3. 用户已接听
        if (session.isConfirmed()) {
            if (session.isVoiceCall()) {
                // 语音通话 → 启动语音通话界面
                CallManager.getInstance().startVoiceCallActivity();
            } else if (session.isVideoCall()) {
                // 视频通话 → 启动视频通话界面
                CallManager.getInstance().startVideoCallActivity();
            }
        }
    }


    @Override
    public void onTrunkingFloorStatusChanged(TrunkingCallSession trunkingCallSession) throws RemoteException {

    }

    @Override
    public void onVideoWindowSizeChanged(int i, int i1) throws RemoteException {

    }

    @Override
    public void onIncommingMessage(TrunkingMessage trunkingMessage) throws RemoteException {

    }

    @Override
    public void onSendMessageResult(TrunkingMessage trunkingMessage) throws RemoteException {

    }

    @Override
    public void onStunStatus(int i) throws RemoteException {

    }

    @Override
    public void onStandbyGroupInfoChanged(Bundle bundle) throws RemoteException {

    }

    @Override
    public void onStackStartSuccess(int i, int i1, int i2) throws RemoteException {

    }

    @Override
    public void onReceiveSilentModeChange(int i) throws RemoteException {

    }

    @Override
    public void onHardPttKeyStatusChanged(int i) throws RemoteException {

    }

    @Override
    public void onOprResponseEvent(int i, int i1, String s) throws RemoteException {

    }

    @Override
    public void onProgressEvent(int i, int i1, String s) throws RemoteException {

    }

    @Override
    public void onContactChanged(int i, String s) throws RemoteException {

    }

    @Override
    public void onFavoriteContactChanged(int i, String s) throws RemoteException {

    }

    @Override
    public void onUserConfigChanged() throws RemoteException {

    }

    @Override
    public void onSimChanged() throws RemoteException {

    }

    @Override
    public void onGroupAffiliactionNotifyResult(String s, boolean b) throws RemoteException {

    }

    @Override
    public void onUeChangePasswordNotifyResult(int i, boolean b) throws RemoteException {

    }

    @Override
    public void onCreateOrUpdateDynGroup(Bundle bundle) throws RemoteException {

    }

    @Override
    public void onDynamicGroupIncrementMessage(Bundle bundle) throws RemoteException {

    }

    @Override
    public void onBitrateReported(Bundle bundle) throws RemoteException {

    }

    @Override
    public void onUploadMediaMessage(long l, int i) throws RemoteException {

    }

    @Override
    public void onRemoteLogOperate(Bundle bundle) throws RemoteException {

    }

    @Override
    public void onMessageOverflow(int i, String s, boolean b) throws RemoteException {

    }

    @Override
    public void onListenCallStateChanged(int i) throws RemoteException {

    }

    @Override
    public void onSideKeyOperate(int i, int i1) throws RemoteException {

    }

    @Override
    public void onUVCCameraConnectCallback(boolean b) throws RemoteException {

    }

    @Override
    public void onTrunkingGroupCallListToBeSelectedInd(String[] strings) throws RemoteException {

    }

    @Override
    public void onTrunkingGroupsUpdateList(int i, List<TrunkingGroupContact> list) throws RemoteException {

    }

    @Override
    public void onTrunkingCurrentListeneGroup(TrunkingGroupContact trunkingGroupContact) throws RemoteException {

    }

    @Override
    public void onTrunkingFloorSpeakInfo(TrunkingCallSession trunkingCallSession) throws RemoteException {

    }

    @Override
    public void onRegistrationStatusChanged(TrunkingProfileState state) {
        // 注册成功 / 掉线
    }


}
