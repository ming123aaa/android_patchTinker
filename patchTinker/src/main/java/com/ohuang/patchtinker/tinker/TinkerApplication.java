package com.ohuang.patchtinker.tinker;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.text.TextUtils;

import com.ohuang.patchtinker.PatchUtil;
import com.ohuang.patchtinker.ProcessCheck;

import java.lang.reflect.Constructor;

public abstract class TinkerApplication extends Application {
    private ApplicationLike applicationLike = null;


    public abstract String getApplicationLikeClassName();

    private synchronized void ensureDelegate() {
        if (applicationLike == null) {
            applicationLike = createDelegate();
        }
    }

    private ApplicationLike createDelegate() {
        try {
            // Use reflection to create the delegate so it doesn't need to go into the primary dex.
            String applicationLikeClassName = getApplicationLikeClassName();
            if (TextUtils.isEmpty(applicationLikeClassName)) {
                applicationLikeClassName = "com.ohuang.patchtinker.tinker.EmptyApplicationLike";
            }
            // And we can also patch it
            Class<?> delegateClass = Class.forName(applicationLikeClassName, false, getClassLoader());
            Constructor<?> constructor = delegateClass.getConstructor(Application.class);
            return (ApplicationLike) constructor.newInstance(this);
        } catch (Throwable e) {
            throw new RuntimeException("createDelegate failed", e);
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        if (isEnablePatch(base)) {
            PatchUtil.getInstance().init(this);
        }
        ensureDelegate();
        applicationLike.onBaseContextAttached(base);
    }

    public boolean isEnablePatch(Context base) {
        return checkProcessPatchEnable(base);
    }

    public boolean checkProcessPatchEnable(Context base) {
        return ProcessCheck.check(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ensureDelegate();
        applicationLike.onCreate();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if (applicationLike != null) {
            applicationLike.onTerminate();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (applicationLike != null) {
            applicationLike.onLowMemory();
        }
    }

    @TargetApi(14)
    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (applicationLike != null) {
            applicationLike.onTrimMemory(level);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (applicationLike != null) {
            applicationLike.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public Resources getResources() {
        Resources resources = super.getResources();
        if (applicationLike != null) {
            return applicationLike.getResources(resources);
        }
        return resources;
    }

    @Override
    public ClassLoader getClassLoader() {
        ClassLoader classLoader = super.getClassLoader();
        if (applicationLike != null) {
            return applicationLike.getClassLoader(classLoader);
        }
        return classLoader;
    }

    @Override
    public AssetManager getAssets() {
        AssetManager assetManager = super.getAssets();
        if (applicationLike != null) {
            return applicationLike.getAssets(assetManager);
        }
        return assetManager;
    }

    @Override
    public Object getSystemService(String name) {
        Object service = super.getSystemService(name);
        if (applicationLike != null) {
            return applicationLike.getSystemService(name, service);
        }
        return service;
    }

    @Override
    public Context getBaseContext() {
        Context base = super.getBaseContext();
        if (applicationLike != null) {
            return applicationLike.getBaseContext(base);
        }
        return base;
    }

    public ApplicationLike getApplicationLike() {
        return applicationLike;
    }
}
