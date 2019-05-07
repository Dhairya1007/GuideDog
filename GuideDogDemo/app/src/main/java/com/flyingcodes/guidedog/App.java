package com.flyingcodes.guidedog;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

/**
 * project_name:GuideDogDemo
 * package_name:com.flyingcodes.guidedog
 * info:
 * be use for:
 * create_by:haojie
 * version: 1.0
 * create_day：2018/11/15
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            LeakCanary.install(this);
        }
    }
}
