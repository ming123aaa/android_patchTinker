package com.ohuang.hotupdate;

import android.content.Context;

public class PatchVersion {


    public static long getVersion(){
       return BuildConfig.PATCH_VERSION;
    }
    public static String getVersionForString(Context context){
       return context.getResources().getString(R.string.patch_version);
    }
}
