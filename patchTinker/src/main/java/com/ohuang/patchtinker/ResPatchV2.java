package com.ohuang.patchtinker;

import android.content.Context;
import android.content.res.AssetManager;

import java.lang.reflect.Method;

public class ResPatchV2 {


    static boolean getResPatch(Context context, String str_ori) {
      return   tinkerResPatch(context, str_ori);
    }


    /**
     * tinker资源热更新方案
     *
     * @param context
     * @param str_ori
     */
    static boolean tinkerResPatch(Context context, String str_ori) {
        try {

            addAssetPath(getSystemResPatch(), str_ori);
//            TinkerResourcePatcher.isResourceCanPatch(context);
//            TinkerResourcePatcher.monkeyPatchExistingResources(context,str_ori,false);
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

     static AssetManager getSystemResPatch() throws Throwable {
        Method addAssetPath = AssetManager.class.getDeclaredMethod("getSystem");
        addAssetPath.setAccessible(true);
        return (AssetManager) addAssetPath.invoke(null);
    }

     static void addAssetPath(AssetManager assetManager, String path) throws Throwable {
        Method addAssetPath = assetManager.getClass().getDeclaredMethod("addAssetPath", String.class);
        addAssetPath.setAccessible(true);
        addAssetPath.invoke(assetManager, path);
    }

     static void addAssetPathAsSharedLibrary(AssetManager assetManager, String path) throws Throwable {
        Method addAssetPath = assetManager.getClass().getDeclaredMethod("addAssetPathAsSharedLibrary", String.class);
        addAssetPath.setAccessible(true);
        addAssetPath.invoke(assetManager, path);
    }


}
