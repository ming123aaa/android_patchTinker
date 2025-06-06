package com.ohuang.patchtinker;

import android.content.Context;

public class DexApk {
    public static void toDexApk(Context context, String patchApk, String dexApk, String tempPath, boolean isProtected,boolean isV2Patch) {
        if(isV2Patch){
            DexApkV2.toDexApk(context, patchApk, dexApk, tempPath,isProtected);
        }else{
            DexApkV1.toDexApk(context, patchApk, dexApk, tempPath);
        }
    }
}
