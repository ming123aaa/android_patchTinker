package com.ohuang.patchtinker;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

public class ProtectModeUtil {
    private static final String key = "PatchTinker_isProtect";

    public static Boolean isProtect(Context context) {
        try {
            ApplicationInfo ai = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(),
                            PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            if (bundle != null && bundle.containsKey(key)) {
                return bundle.getBoolean(key);//className 是配置在xml文件中的。
            } else {
                return false;
            }
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        return false;
    }
}
