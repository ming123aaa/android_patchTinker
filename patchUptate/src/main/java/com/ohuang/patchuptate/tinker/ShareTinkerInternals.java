/*
 * Tencent is pleased to support the open source community by making Tinker available.
 *
 * Copyright (C) 2016 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ohuang.patchuptate.tinker;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhangshaowen on 16/3/10.
 */
public class ShareTinkerInternals {
    private static final String  TAG                   = "Tinker.TinkerInternals";
    private static final boolean VM_IS_ART             = isVmArt(System.getProperty("java.vm.version"));
    private static final boolean VM_IS_JIT             = isVmJitInternal();
    private static final String  PATCH_PROCESS_NAME    = ":patch";

    private static       Boolean isPatchProcess        = null;
    private static       Boolean isARKHotRunning       = null;
    /**
     * or you may just hardcode them in your app
     */
    private static final String[]  processName           = {null};
    private static       String    tinkerID              = null;
    private static       String    currentInstructionSet = null;

    public static boolean isVmArt() {
        return VM_IS_ART || VERSION.SDK_INT >= 21;
    }

    public static boolean isVmJit() {
        return VM_IS_JIT && VERSION.SDK_INT < 24;
    }

    public static boolean isArkHotRuning() {
        if (isARKHotRunning != null) {
            return isARKHotRunning;
        }
        isARKHotRunning = false;
        Class<?> arkApplicationInfo = null;
        try {
            arkApplicationInfo = ClassLoader.getSystemClassLoader()
                .getParent().loadClass("com.huawei.ark.app.ArkApplicationInfo");
            Method isRunningInArkHot = null;
            isRunningInArkHot = arkApplicationInfo.getDeclaredMethod("isRunningInArk");
            isRunningInArkHot.setAccessible(true);
            isARKHotRunning = (Boolean) isRunningInArkHot.invoke(null);
        } catch (ClassNotFoundException e) {
            ShareTinkerLog.i(TAG, "class not found exception");
        } catch (NoSuchMethodException e) {
            ShareTinkerLog.i(TAG, "no such method exception");
        } catch (SecurityException e) {
            ShareTinkerLog.i(TAG, "security exception");
        } catch (IllegalAccessException e) {
            ShareTinkerLog.i(TAG, "illegal access exception");
        } catch (InvocationTargetException e) {
            ShareTinkerLog.i(TAG, "invocation target exception");
        } catch (IllegalArgumentException e) {
            ShareTinkerLog.i(TAG, "illegal argument exception");
        }
        return isARKHotRunning;
    }

    public static boolean isAfterAndroidO() {
        return VERSION.SDK_INT > 25;
    }

    public static String getCurrentInstructionSet() {
        if (currentInstructionSet != null) {
            return currentInstructionSet;
        }

        try {
            Class<?> clazz = Class.forName("dalvik.system.VMRuntime");
            Method currentGet = clazz.getDeclaredMethod("getCurrentInstructionSet");
            currentGet.setAccessible(true);
            currentInstructionSet = (String) currentGet.invoke(null);
        } catch (Throwable ignored) {
            switch (Build.CPU_ABI) {
                case "armeabi":
                case "armeabi-v7a":
                    currentInstructionSet = "arm";
                    break;
                case "arm64-v8a":
                    currentInstructionSet = "arm64";
                    break;
                case "x86":
                    currentInstructionSet = "x86";
                    break;
                case "x86_64":
                    currentInstructionSet = "x86_64";
                    break;
                case "mips":
                    currentInstructionSet = "mips";
                    break;
                case "mips64":
                    currentInstructionSet = "mips64";
                    break;
                default:
                    throw new IllegalStateException("Unsupported abi: " + Build.CPU_ABI);
            }
        }
        ShareTinkerLog.d(TAG, "getCurrentInstructionSet:" + currentInstructionSet);
        return currentInstructionSet;
    }

    public static boolean is32BitEnv() {
        final String currISA = getCurrentInstructionSet();
        return "arm".equals(currISA) || "x86".equals(currISA) || "mips".equals(currISA);
    }

    public static boolean isSystemOTA(String lastFingerPrint) {
        String currentFingerprint = Build.FINGERPRINT;
        if (lastFingerPrint == null
            || lastFingerPrint.equals("")
            || currentFingerprint == null
            || currentFingerprint.equals("")) {
            ShareTinkerLog.d(TAG, "fingerprint empty:" + lastFingerPrint + ",current:" + currentFingerprint);
            return false;
        } else {
            if (lastFingerPrint.equals(currentFingerprint)) {
                ShareTinkerLog.d(TAG, "same fingerprint:" + currentFingerprint);
                return false;
            } else {
                ShareTinkerLog.d(TAG, "system OTA,fingerprint not equal:" + lastFingerPrint + "," + currentFingerprint);
                return true;
            }
        }
    }



    public static boolean isNullOrNil(final String object) {
        if ((object == null) || (object.length() <= 0)) {
            return true;
        }
        return false;
    }








    public static void killAllOtherProcess(Context context) {
        final ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (am == null) {
            return;
        }
        List<RunningAppProcessInfo> appProcessList = am
            .getRunningAppProcesses();

        if (appProcessList == null) {
            return;
        }
        // NOTE: getRunningAppProcess() ONLY GIVE YOU THE PROCESS OF YOUR OWN PACKAGE IN ANDROID M
        // BUT THAT'S ENOUGH HERE
        for (RunningAppProcessInfo ai : appProcessList) {
            // KILL OTHER PROCESS OF MINE
            if (ai.uid == android.os.Process.myUid() && ai.pid != android.os.Process.myPid()) {
                android.os.Process.killProcess(ai.pid);
            }
        }

    }

    public static void killProcessExceptMain(Context context) {
        final ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (am == null) {
            return;
        }
        List<RunningAppProcessInfo> appProcessList = am.getRunningAppProcesses();
        if (appProcessList != null) {
            // NOTE: getRunningAppProcess() ONLY GIVE YOU THE PROCESS OF YOUR OWN PACKAGE IN ANDROID M
            // BUT THAT'S ENOUGH HERE
            for (RunningAppProcessInfo ai : appProcessList) {
                if (ai.uid != android.os.Process.myUid()) {
                    continue;
                }
                if (ai.processName.equals(context.getPackageName())) {
                    continue;
                }
                android.os.Process.killProcess(ai.pid);
            }
        }
    }




    /**
     * vm whether it is art
     *
     * @return
     */
    private static boolean isVmArt(String versionString) {
        boolean isArt = false;
        if (versionString != null) {
            Matcher matcher = Pattern.compile("(\\d+)\\.(\\d+)(\\.\\d+)?").matcher(versionString);
            if (matcher.matches()) {
                try {
                    int major = Integer.parseInt(matcher.group(1));
                    int minor = Integer.parseInt(matcher.group(2));
                    isArt = (major > 2)
                        || ((major == 2)
                        && (minor >= 1));
                } catch (NumberFormatException e) {
                    // let isMultidexCapable be false
                }
            }
        }
        return isArt;
    }

    private static boolean isVmJitInternal() {
        try {
            Class<?> clazz = Class.forName("android.os.SystemProperties");
            Method mthGet = clazz.getDeclaredMethod("get", String.class);

            String jit = (String) mthGet.invoke(null, "dalvik.vm.usejit");
            String jitProfile = (String) mthGet.invoke(null, "dalvik.vm.usejitprofiles");

            //usejit is true and usejitprofiles is null
            if (!isNullOrNil(jit) && isNullOrNil(jitProfile) && jit.equals("true")) {
                return true;
            }
        } catch (Throwable e) {
            ShareTinkerLog.e(TAG, "isVmJitInternal ex:" + e);
        }
        return false;
    }

    public static boolean isNewerOrEqualThanVersion(int apiLevel, boolean includePreviewVer) {
        if (includePreviewVer && VERSION.SDK_INT >= 23) {
            return VERSION.SDK_INT >= apiLevel
                    || ((VERSION.SDK_INT == apiLevel - 1) && VERSION.PREVIEW_SDK_INT > 0);
        } else {
            return VERSION.SDK_INT >= apiLevel;
        }
    }

    public static boolean isOlderOrEqualThanVersion(int apiLevel, boolean includePreviewVer) {
        if (includePreviewVer && VERSION.SDK_INT >= 23) {
            return VERSION.SDK_INT <= apiLevel
                    || ((VERSION.SDK_INT == apiLevel - 1) && VERSION.PREVIEW_SDK_INT > 0);
        } else {
            return VERSION.SDK_INT <= apiLevel;
        }
    }


}
