package com.ohuang.patchtinker;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import com.ohuang.patchtinker.tinker.TinkerResourcePatcher;
import com.ohuang.patchtinker.util.PatchIdManager;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

public class ResPatchV2 {




    /**
     * tinker资源热更新方案
     *
     * @param context
     * @param str_ori
     */
    public static boolean tinkerResPatch(Context context, String str_ori) {
        try {

            PatchIdManager.loadResPatch(context,str_ori);


        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }






     public static void addAssetPathAsSharedLibrary(AssetManager assetManager, String path) throws Throwable {
        Method addAssetPath = assetManager.getClass().getDeclaredMethod("addAssetPathAsSharedLibrary", String.class);
        addAssetPath.setAccessible(true);
        addAssetPath.invoke(assetManager, path);
    }


}
