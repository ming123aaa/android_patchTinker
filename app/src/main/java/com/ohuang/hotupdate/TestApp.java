package com.ohuang.hotupdate;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.ohuang.patchtinker.util.ProcessUtil;

public class TestApp extends Application {

public static final String TAG="TestApp";



    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        Log.d(TAG, "attachBaseContext: 新代码");

    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: 新代码");


    }

    @Override
    public void registerActivityLifecycleCallbacks(ActivityLifecycleCallbacks callback) {
        super.registerActivityLifecycleCallbacks(callback);
    }
}
