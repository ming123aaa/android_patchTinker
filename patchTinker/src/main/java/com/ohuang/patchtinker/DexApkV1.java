package com.ohuang.patchtinker;

import android.content.Context;
import android.util.Log;

import com.ohuang.patchtinker.util.ZipUtil;

import java.io.File;

public class DexApkV1 {
    public static final String TAG = "DexApk";

    public static void toDexApk(Context context, String patchApk, String dexApk, String tempPath) {
        long time = System.currentTimeMillis();
        String baseApk = context.getApplicationInfo().sourceDir;
        mergeDexApk(baseApk, patchApk, dexApk, tempPath);
        Log.d(TAG, "toDexApk: 耗时"+(System.currentTimeMillis()-time)+"ms");
    }

    private static void mergeDexApk(String apkPath, String patchPath, String outDexPath, String tmpPath) {
        ZipUtil.upZipByZipIntercept(apkPath, tmpPath + "/baseDex", new ZipUtil.ZipIntercept() {
            @Override
            public boolean isCopy(String fileName) {
                if (fileName.startsWith("classes") && fileName.endsWith(".dex")) {
                    return true;
                }
                return false;
            }
        });
        ZipUtil.upZipByZipIntercept(patchPath, tmpPath + "/patchDex", new ZipUtil.ZipIntercept() {
            @Override
            public boolean isCopy(String fileName) {
                if (fileName.startsWith("classes") && fileName.endsWith(".dex")) {
                    return true;
                }
                return false;
            }
        });
        File patchDex = new File(tmpPath + "/patchDex");
        final int[] classesNum = {findClassesNum(patchDex)};
        File baseDex = new File(tmpPath + "/baseDex");
        classesNum[0]++;
        findClassForOrder(baseDex, new CallBack() {
            @Override
            public void call(File file) {
                if (classesNum[0] == 1) {
                    file.renameTo(new File(tmpPath + "/patchDex/classes.dex"));
                } else {
                    file.renameTo(new File(tmpPath + "/patchDex/classes" + classesNum[0] + ".dex"));
                }
                classesNum[0]++;
            }
        });
        ZipUtil.toZip(outDexPath, tmpPath + "/patchDex", true);

    }

    private static int findClassesNum(File file) {
        String absolutePath = file.getAbsolutePath();
        int num = 0;
        File file1 = new File(absolutePath + "/classes.dex");
        if (!file1.exists()) {
            return num;
        }
        num = 2;
        while (new File(absolutePath + "/classes" + num + ".dex").exists()) {
            num++;
        }
        return num - 1;
    }

    private static void findClassForOrder(File file, CallBack callBack) {
        String absolutePath = file.getAbsolutePath();
        int num = 0;
        File file1 = new File(absolutePath + "/classes.dex");
        if (!file1.exists()) {
            return;
        } else {
            callBack.call(file1);
        }
        num = 2;
        while (new File(absolutePath + "/classes" + num + ".dex").exists()) {
            callBack.call(new File(absolutePath + "/classes" + num + ".dex"));
            num++;
        }

    }

    private interface CallBack {
        void call(File file);
    }
}
