package com.ohuang.patchtinker;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

 class AppReplaceUtil {
    private static Application app;
    private static final String APP_KEY = "Application_Name";
    public static final String TAG = "AppImpl";

    public static void attachBaseContext(Context context) {
        app = ReplaceApp.makeApplication(getSrcApplicationClassName(context));
    }


    public static void onCreate(Application application) {
        Application myApp = application;
        if (app != null) {
            ReplaceApp.replaceAndRunMainApplication(app);
            myApp = app;

        }

    }


    /**
     * 获取原application的类名
     *
     * @return 返回类名
     */
    private static String getSrcApplicationClassName(Context context) {
        try {
            ApplicationInfo ai = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(),
                            PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            if (bundle != null && bundle.containsKey(APP_KEY)) {
                return bundle.getString(APP_KEY);//className 是配置在xml文件中的。
            } else {

                return "";
            }
        } catch (PackageManager.NameNotFoundException e) {

            e.printStackTrace();
        }
        return "";
    }

}
