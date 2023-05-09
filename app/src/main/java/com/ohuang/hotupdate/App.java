package com.ohuang.hotupdate;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;



import com.ohuang.patchuptate.LoadDexUtil;
import com.ohuang.patchuptate.Patch;
import com.ohuang.patchuptate.PatchUtil;
import com.ohuang.patchuptate.RefInvoke;
import com.ohuang.patchuptate.ResPatch;

import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.HashMap;

public class App extends Application {
    public static final String TAG = "App";




    @Override
    protected void attachBaseContext(Context base) {

        PatchUtil.getInstance().init(base);
        super.attachBaseContext(base);
        RefInvoke.invokeStaticMethod("com.ohuang.hotupdate.AppImpl","attachBaseContext"
                ,new Class[]{Context.class},new Object[]{base});

    }


    @Override
    public void onCreate() {
        super.onCreate();
        RefInvoke.invokeStaticMethod("com.ohuang.hotupdate.AppImpl","onCreate"
                ,new Class[]{Application.class},new Object[]{this});
    }


}
