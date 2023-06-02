package com.ohuang.patchtinker;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;

import com.ohuang.patchtinker.util.ProcessUtil;

import java.util.List;

public class ProcessCheck {

    private static final String key = "PatchTinker_WhiteProcess";

    /**
     * 检查进程是否需要热更
     *
     * @param context
     * @return
     */
    public static boolean check(Context context) {
        String currentProcessNameByActivityManager = ProcessUtil.getCurrentProcessNameByActivityManager(context);
        if (TextUtils.isEmpty(currentProcessNameByActivityManager)) {
            return true;
        }
        return !isWhiteProcess(context, currentProcessNameByActivityManager);
    }

    /**
     * 白名单的不热更
     * @param context
     * @param processName
     * @return
     */
    private static boolean isWhiteProcess(Context context, String processName) {
        String whiteProcessConfig = getWhiteProcessConfig(context);
        if (TextUtils.isEmpty(whiteProcessConfig)) {
            return false;
        }
        String[] split = whiteProcessConfig.split(",");
        for (int i = 0; i < split.length; i++) {
            String s = split[i];
            if (TextUtils.isEmpty(s)) {
                continue;
            }
            if (s.startsWith(":")) {
                if (processName.endsWith(s)) {
                    return true;
                }
            }
            if (processName.equals(s)) {
                return true;
            }
        }
        return false;
    }

    private static String getWhiteProcessConfig(Context context) {
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
