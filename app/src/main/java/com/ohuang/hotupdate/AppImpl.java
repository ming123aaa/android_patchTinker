package com.ohuang.hotupdate;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.ohuang.patchtinker.tinker.ApplicationLike;

public class AppImpl extends ApplicationLike {
    public static final String TAG="AppImpl";
    public AppImpl(Application application) {
        super(application);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: 新代码");
    }

    @Override
    public void onBaseContextAttached(Context base) {
        super.onBaseContextAttached(base);
        Log.d(TAG, "onBaseContextAttached: 新代码");
    }
}
