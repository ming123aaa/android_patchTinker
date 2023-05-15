package com.ohuang.patchtinker.tinker;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

public interface ApplicationLifeCycle {
    /**
     * Same as {@link Application#onCreate()}.
     */
    void onCreate();

    /**
     * Same as {@link Application#onLowMemory()}.
     */
    void onLowMemory();

    /**
     * Same as {@link Application#onTrimMemory(int level)}.
     * @param level
     */
    void onTrimMemory(int level);

    /**
     * Same as {@link Application#onTerminate()}.
     */
    void onTerminate();

    /**
     * Same as {@link Application#onConfigurationChanged(Configuration newconfig)}.
     */
    void onConfigurationChanged(Configuration newConfig);

    /**
     * Same as {@link Application#attachBaseContext(Context context)}.
     */
    void onBaseContextAttached(Context base);
}
