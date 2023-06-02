package com.ohuang.patchtinker;

import android.app.Application;
import android.content.Context;

public class PatchApplication extends Application {
    public static final String TAG = "App";




    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        if (ProcessCheck.check(base)) {
            PatchUtil.getInstance().init(this);
        }
        RefInvoke.invokeStaticMethod("com.ohuang.patchtinker.AppReplaceUtil","attachBaseContext"
                ,new Class[]{Context.class},new Object[]{base});
    }


    @Override
    public void onCreate() {
        super.onCreate();
        RefInvoke.invokeStaticMethod("com.ohuang.patchtinker.AppReplaceUtil","onCreate"
                ,new Class[]{Application.class},new Object[]{this});
    }


}
