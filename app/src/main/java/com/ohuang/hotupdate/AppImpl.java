package com.ohuang.hotupdate;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;



import com.ohuang.patchuptate.LoadDexUtil;
import com.ohuang.patchuptate.ResPatch;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class AppImpl {
    private static Application app;
    private static final String APP_KEY = "Application_Name";
    public static final String TAG = "AppImpl";

    public static void attachBaseContext(Context context, Application application) {
        app = LoadDexUtil.makeApplication(getSrcApplicationClassName(context));

    }


    public static void onCreate(Application application) {

        if (app != null) {

            LoadDexUtil.replaceAndRunMainApplication(app);
            app.registerActivityLifecycleCallbacks(new SimpleActivityLifecycleCallbacks() {


                @Override
                public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                    super.onActivityCreated(activity, savedInstanceState);
                    ResPatch.replaceActivityResources(activity, ResPatch.sm_resources);
                    Log.d(TAG, "onActivityCreated: "+activity);
                }
            });
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
