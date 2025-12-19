package com.xycm.poc.app;

import android.app.Application;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.mpttpnas.pnaslibraryapi.PnasApplicationUtil;
import com.xycm.poc.api.config.TokenManager;
import com.xycm.poc.manager.CallManager;
import com.xycm.poc.util.AppStateUtil;


public class MyApplication extends Application implements LifecycleObserver {

    private static MyApplication instance;

    public static MyApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        // 初始化 PNAS
        PnasApplicationUtil.getInstance().initApplication(this);
        TokenManager.getInstance().init(this);
        // 监听前后台（官方推荐）
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        CallManager.getInstance().init(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onAppForeground() {
        AppStateUtil.setForeground(true);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onAppBackground() {
        AppStateUtil.setForeground(false);
    }

}
