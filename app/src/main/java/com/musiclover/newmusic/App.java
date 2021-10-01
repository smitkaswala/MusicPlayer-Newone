package com.musiclover.newmusic;

import android.app.Application;

import com.musiclover.newmusic.util.PreferenceUtil;


public class App extends Application {
    private static App mInstance;
    public static final boolean HIDE_INCOMPLETE_FEATURES = true;

    public static synchronized App getInstance() {
        return mInstance;
    }

    public PreferenceUtil getPreferencesUtility() {
        return PreferenceUtil.getInstance(App.this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}