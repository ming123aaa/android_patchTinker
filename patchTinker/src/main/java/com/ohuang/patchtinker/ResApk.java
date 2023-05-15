package com.ohuang.patchtinker;

import android.content.Context;


import com.ohuang.patchtinker.util.CopyFileUtil;
import com.ohuang.patchtinker.util.FileUtils;
import com.ohuang.patchtinker.util.ZipUtil;

public class ResApk {

    public static void toResApk(Context context, String apkPath) {
        //解压补丁包
        ZipUtil.upZipByDir(apkPath, context.getFilesDir().getAbsolutePath() + PatchUtil.temp + "/resApk/res", "res/");
        ZipUtil.upZipByDir(apkPath, context.getFilesDir().getAbsolutePath() + PatchUtil.temp + "/resApk/assets", "assets/");
        String resources = context.getFilesDir().getAbsolutePath() + PatchUtil.temp + "/resApk/resources.arsc";
        FileUtils.createParentDir(resources);
        ZipUtil.upZipByName(apkPath, resources, "resources.arsc");
        //解压原包
        String baseApk = context.getApplicationInfo().sourceDir;
        ZipUtil.upZipByDir(baseApk, context.getFilesDir().getAbsolutePath() + PatchUtil.temp + "/baseApk/res", "res/");
        ZipUtil.upZipByDir(baseApk, context.getFilesDir().getAbsolutePath() + PatchUtil.temp + "/baseApk/assets", "assets/");
        String resources2 = context.getFilesDir().getAbsolutePath() + PatchUtil.temp + "/baseApk/resources.arsc";
        FileUtils.createParentDir(resources2);
        ZipUtil.upZipByName(baseApk, resources2, "resources.arsc");

        //资源文件复制
        CopyFileUtil.copyPathAllFile(context.getFilesDir().getAbsolutePath() + PatchUtil.temp + "/baseApk"
                ,context.getFilesDir().getAbsolutePath() + PatchUtil.temp + "/resApk","",false);

        //转res.apk
        ZipUtil.toZip(context.getFilesDir().getAbsolutePath() + PatchUtil.resApk, context.getFilesDir().getAbsolutePath() + PatchUtil.temp + "/resApk", true);

    }
}
