package com.ironbear775.musichunter;

import android.app.Application;

import cat.ereza.customactivityoncrash.config.CaocConfig;

/**
 * Created by ironbear775 on 2018/1/3.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CaocConfig.Builder.create()
                .enabled(false)
                .apply();
    }
}
