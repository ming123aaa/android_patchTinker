package com.ohuang.patchtinker;

import android.content.Context;
import android.util.Log;

import com.ohuang.patchtinker.util.CopyFileUtil;
import com.ohuang.patchtinker.util.ZipUtil;

import java.io.File;

public class ResApkV2 {
    public static final String TAG = "ResApk";

    public static void toDexResApk(Context context, String patchApk, String dexApk, String tempPath, boolean isProtected) {
        long time = System.currentTimeMillis();
        String baseApk = context.getApplicationInfo().sourceDir;
        if (isProtected) {
            mergeDexProtectedApk(patchApk, dexApk, tempPath);
        } else {
            mergeDexApk(baseApk, patchApk, dexApk, tempPath);
        }
        Log.d(TAG, "toDexResApk: baseApkPath=" + baseApk + "  patchPath=" + patchApk);
        Log.d(TAG, "toDexResApk: 耗时" + (System.currentTimeMillis() - time) + "ms");

    }

    private static void mergeDexProtectedApk(String patchPath, String outDexPath, String tmpPath) {
        CopyFileUtil.copyFile(patchPath,outDexPath);
    }

    private static void mergeDexApk(String apkPath, String patchPath, String outDexPath, String tmpPath) {
        ZipUtil.upZipByZipIntercept(apkPath, tmpPath + "/baseDex", new ZipUtil.ZipIntercept() {
            @Override
            public boolean isCopy(String fileName) {
                if (fileName.startsWith("classes") && fileName.endsWith(".dex")) { //只解压classes.dex
                    return true;
                }
                return false;
            }
        });
        ZipUtil.upZipByZipIntercept(patchPath, tmpPath + "/patchDex", new ZipUtil.ZipIntercept() {
            @Override
            public boolean isCopy(String fileName) {
                if (fileName.startsWith("lib/")) {  //lib文件不处理
                    return false;
                }
                return true;
            }
        });
        File patchDex = new File(tmpPath + "/patchDex");
        final int[] classesNum = {FindDex.sortDex(patchDex)};
        File baseDex = new File(tmpPath + "/baseDex");
        classesNum[0]++;
        FindDex.findSortDex(baseDex, new FindDex.CallBack() {
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

        ZipUtil.toZip(outDexPath, tmpPath + "/patchDex", true, new ZipUtil.ZipCompressIntercept() {
            @Override
            public boolean canCompress(String name) {
                if (name.startsWith("res/raw/") || name.startsWith("assets/")) { //不需要压缩的文件
                    return false;
                }
                return true;
            }
        });

    }




}
