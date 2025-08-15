package com.ohuang.patchtinker.util;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

import java.lang.reflect.Method;
import java.util.List;

public class ProcessUtil {


    /**
     * 判断该进程ID是否属于该进程名
     *
     * @param context
     * @param pid     进程ID
     * @param p_name  进程名
     * @return true属于该进程名
     */

    public static boolean isPidOfProcessName(Context context, int pid, String p_name) {

        if (p_name == null) {
            return false;
        }
        boolean isMain = false;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//遍历所有进程
        for (ActivityManager.RunningAppProcessInfo process : am.getRunningAppProcesses()) {
            if (process.pid == pid) {
//进程ID相同时判断该进程名是否一致
                if (process.processName.equals(p_name)) {
                    isMain = true;
                }
                break;

            }

        }

        return isMain;

    }


    /**
     * 获取主进程名
     *
     * @param context 上下文
     * @return 主进程名
     */

    public static String getMainProcessName(Context context) {

        return context.getPackageName();

    }


    /**
     * 判断是否主进程
     *
     * @param context 上下文
     * @return true是主进程
     */

    public static boolean isMainProcess(Context context) {
        return getMainProcessName(context).equals(getCurrentProcessName(context));

    }


    public static String getCurrentProcessName(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            String processName = Application.getProcessName();
            if (!TextUtils.isEmpty(processName)) {
                return processName;
            }
        } else {
            try {
                Class<?> activityThread = null;
                activityThread = Class.forName("android.app.ActivityThread");
                Method currentProcessName = activityThread.getDeclaredMethod("currentProcessName");
                String processName = (String) currentProcessName.invoke(null);
                if (!TextUtils.isEmpty(processName)) {
                    return processName;
                }
            } catch (Throwable e) {

            }

        }

        return getCurrentProcessNameByActivityManager(context);
    }

    public static String getCurrentProcessNameByActivityManager(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (am != null) {
            List<ActivityManager.RunningAppProcessInfo> runningAppList = am.getRunningAppProcesses();
            if (runningAppList != null) {
                for (ActivityManager.RunningAppProcessInfo processInfo : runningAppList) {
                    if (processInfo.pid == pid) {
                        return processInfo.processName;
                    }
                }
            }
        }

        return "";
    }
}
