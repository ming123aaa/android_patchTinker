package com.ohuang.patchtinker.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

public class AndroidXmlUtil {


    public static String getMetaData(Context context,String key) {
        try {
            ApplicationInfo ai = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(),
                            PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            if (bundle != null && bundle.containsKey(key)) {
                Object o = bundle.get(key);
                if (o instanceof String){
                    return (String) o;
                }else {
                    return ""+o;
                }
            } else {

                return "";
            }
        } catch (PackageManager.NameNotFoundException e) {

            e.printStackTrace();
        }
        return "";
    }
}
