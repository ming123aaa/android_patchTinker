package com.ohuang.patchtinker;

import android.content.Context;

public class ResApk {
    public static void toDexResApk(Context context, String patchApk, String dexApk, String tempPath,boolean isProtected,boolean isV2Patch) {
        if(isV2Patch){
            ResApkV2.toDexResApk(context, patchApk, dexApk, tempPath,isProtected);
        }else{
            ResApkV1.toDexResApk(context, patchApk, dexApk, tempPath);
        }
    }
}
