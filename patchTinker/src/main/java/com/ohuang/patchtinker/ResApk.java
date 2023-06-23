package com.ohuang.patchtinker;

import android.content.Context;
import android.util.Log;


import com.ohuang.patchtinker.util.CopyFileUtil;
import com.ohuang.patchtinker.util.FileUtils;
import com.ohuang.patchtinker.util.ZipUtil;

import java.io.File;

public class ResApk {
    public static final String TAG = "ResApk";

    public static void toDexResApk(Context context, String patchApk, String dexApk, String tempPath) {
        long time = System.currentTimeMillis();
        String baseApk = context.getApplicationInfo().sourceDir;
        mergeDexApk(baseApk, patchApk, dexApk, tempPath);
        Log.d(TAG, "toDexResApk: baseApkPath="+baseApk+"  patchPath="+patchApk);
        Log.d(TAG, "toDexResApk: 耗时"+(System.currentTimeMillis()-time)+"ms");

    }

    private static void mergeDexApk(String apkPath, String patchPath, String outDexPath, String tmpPath) {
        ZipUtil.upZipByZipIntercept(apkPath, tmpPath + "/baseDex", new ZipUtil.ZipIntercept() {
            @Override
            public boolean isCopy(String fileName) {
                if (fileName.startsWith("lib/") ) {
                    return false;
                }
                return true;
            }
        });
        ZipUtil.upZipByZipIntercept(patchPath, tmpPath + "/patchDex", new ZipUtil.ZipIntercept() {
            @Override
            public boolean isCopy(String fileName) {
                if (fileName.startsWith("lib/") ) {
                    return false;
                }
                return true;
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
        CopyFileUtil.renamePathAllFile(tmpPath+"/baseDex"
                , tmpPath + "/patchDex", "", false);
        ZipUtil.toZip(outDexPath, tmpPath + "/patchDex", true, new ZipUtil.ZipCompressIntercept() {
            @Override
            public boolean canCompress(String name) {
                if (name.startsWith("res/raw/") || name.startsWith("assets/")) {
                    return false;
                }
                return true;
            }
        });

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

//     static void toResApk(Context context, String apkPath) {
//        long l = System.currentTimeMillis();
//        //解压补丁包
//        ZipUtil.upZipByDir(apkPath, context.getFilesDir().getAbsolutePath() + PatchUtil.temp + "/resApk/res", "res/");
//        ZipUtil.upZipByDir(apkPath, context.getFilesDir().getAbsolutePath() + PatchUtil.temp + "/resApk/assets", "assets/");
//        String resources = context.getFilesDir().getAbsolutePath() + PatchUtil.temp + "/resApk/resources.arsc";
//        FileUtils.createParentDir(resources);
//        ZipUtil.upZipByName(apkPath, resources, "resources.arsc");
//        //解压原包
//        String baseApk = context.getApplicationInfo().sourceDir;
//        ZipUtil.upZipByDir(baseApk, context.getFilesDir().getAbsolutePath() + PatchUtil.temp + "/baseApk/res", "res/");
//        ZipUtil.upZipByDir(baseApk, context.getFilesDir().getAbsolutePath() + PatchUtil.temp + "/baseApk/assets", "assets/");
//        String resources2 = context.getFilesDir().getAbsolutePath() + PatchUtil.temp + "/baseApk/resources.arsc";
//        FileUtils.createParentDir(resources2);
//        ZipUtil.upZipByName(baseApk, resources2, "resources.arsc");
//        Log.d(TAG, "toResApk: 资源解压耗时"+(System.currentTimeMillis()-l)+"ms");
//        l = System.currentTimeMillis();
//        //资源文件复制
//        CopyFileUtil.copyPathAllFile(
//                context.getFilesDir().getAbsolutePath() + PatchUtil.temp + "/resApk", context.getFilesDir().getAbsolutePath() + PatchUtil.temp + "/baseApk", "", true);
//        Log.d(TAG, "toResApk: 资源复制耗时"+(System.currentTimeMillis()-l)+"ms");
//        l = System.currentTimeMillis();
//        //转res.apk
//        ZipUtil.toZip(context.getFilesDir().getAbsolutePath() + PatchUtil.resApk, context.getFilesDir().getAbsolutePath() + PatchUtil.temp + "/baseApk", true);
//        Log.d(TAG, "toResApk: zip压缩耗时:"+(System.currentTimeMillis()-l)+"ms");
//    }
}
