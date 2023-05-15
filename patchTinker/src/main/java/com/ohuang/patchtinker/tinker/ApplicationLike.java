package com.ohuang.patchtinker.tinker;

import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;

public abstract class ApplicationLike implements ApplicationLifeCycle{

    Application application;

    public ApplicationLike(Application application){
        this.application=application;
    }

    public Application getApplication() {
        return application;
    }
    @Override
    public void onCreate() {

    }

    @Override
    public void onLowMemory() {

    }

    @Override
    public void onTrimMemory(int level) {

    }

    @Override
    public void onTerminate() {

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

    }

    @Override
    public void onBaseContextAttached(Context base) {

    }

    public Resources getResources(Resources resources) {
        return resources;
    }

    public ClassLoader getClassLoader(ClassLoader classLoader) {
        return classLoader;
    }

    public AssetManager getAssets(AssetManager assetManager) {
        return assetManager;
    }

    public Object getSystemService(String name, Object service) {
        return service;
    }

    public Context getBaseContext(Context base) {
        return base;
    }
}
