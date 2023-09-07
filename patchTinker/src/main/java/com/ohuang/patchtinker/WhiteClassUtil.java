package com.ohuang.patchtinker;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class WhiteClassUtil {
    private static final String key = "PatchTinker_WhiteClassStartWith";
    private static final String keyEquals = "PatchTinker_WhiteClassEquals";


    public static List<String> getWhiteClassStartWith(Context context){
        String whiteProcessConfig = getWhiteClassConfig(context);
        if (TextUtils.isEmpty(whiteProcessConfig)) {
            return new ArrayList<>();
        }
        String[] split = whiteProcessConfig.split(",");
        List<String> list=new ArrayList<>();
        for (int i = 0; i < split.length; i++) {
            String s = split[i];
            if (TextUtils.isEmpty(s)) {
                continue;
            }
            list.add(s);
        }
        return list;
    }
    public static List<String> getWhiteClassEquals(Context context){
        String whiteProcessConfig = getWhiteClassEqualsConfig(context);
        if (TextUtils.isEmpty(whiteProcessConfig)) {
            return new ArrayList<>();
        }
        String[] split = whiteProcessConfig.split(",");
        List<String> list=new ArrayList<>();
        for (int i = 0; i < split.length; i++) {
            String s = split[i];
            if (TextUtils.isEmpty(s)) {
                continue;
            }
            list.add(s);
        }
        return list;
    }

    private static String getWhiteClassEqualsConfig(Context context) {
        try {
            ApplicationInfo ai = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(),
                            PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            if (bundle != null && bundle.containsKey(keyEquals)) {
                return bundle.getString(keyEquals);//className 是配置在xml文件中的。
            } else {

                return "";
            }
        } catch (PackageManager.NameNotFoundException e) {

            e.printStackTrace();
        }
        return "";
    }
    private static String getWhiteClassConfig(Context context) {
        try {
            ApplicationInfo ai = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(),
                            PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            if (bundle != null && bundle.containsKey(key)) {
                return bundle.getString(key);//className 是配置在xml文件中的。
            } else {

                return "";
            }
        } catch (PackageManager.NameNotFoundException e) {

            e.printStackTrace();
        }
        return "";
    }
}
