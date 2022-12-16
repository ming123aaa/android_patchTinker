package com.ohuang.patchuptate;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.loader.AssetsProvider;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.ohuang.patchuptate.tinker.TinkerResourcePatcher;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.EventListener;

public class ResPatch {

    public static Resources sm_resources = null;

    public static boolean isEnable=true;
    public static void getResPatch(Context context, String str_ori) {
        if (!isEnable){
            return;
        }
        tinkerResPatch(context, str_ori);
//        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.P) {
//            fn_patch_res(context, str_ori);
//        }else {
//            hook_patch_res(context,str_ori);
//        }
    }

    /**
     * tinker资源热更新方案
     * @param context
     * @param str_ori
     */
    public static void tinkerResPatch(Context context,String str_ori){
        try {
            TinkerResourcePatcher.isResourceCanPatch(context);
            TinkerResourcePatcher.monkeyPatchExistingResources(context,str_ori,false);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static void fn_patch_res(Context context, String str_ori) {
        try {
            Class cAm = Class.forName("android.content.res.AssetManager");
            AssetManager objAm = (AssetManager) cAm.newInstance();
            Class<?> cApkAssets = Class.forName("android.content.res.ApkAssets");

            Method getApkPaths = cAm.getDeclaredMethod("getApkAssets");
            Object[] invoke = (Object[]) getApkPaths.invoke(context.getAssets());

            Method method = cAm.getDeclaredMethod("addAssetPath", String.class);
            method.invoke(objAm, str_ori);
            for (Object o : invoke) {
                Method getAssetPath = cApkAssets.getDeclaredMethod("getAssetPath");
                Object invoke1 = getAssetPath.invoke(o);
                method.invoke(objAm, invoke1);
            }
            sm_resources = new Resources(objAm, context.getResources().getDisplayMetrics(), context.getResources().getConfiguration());
            str_res_path = str_ori;
        } catch (Exception e) {
            Log.d("xfhsm", "fn_patch_res-try::" + e.toString());
        }
    }




    private static void hook_patch_res(Context context, String str_ori) {
        try {
            Class cAm = Class.forName("android.content.res.AssetManager");
            AssetManager objAm = (AssetManager) context.getAssets();
            Method method = cAm.getDeclaredMethod("addAssetPath", String.class);
            method.invoke(objAm, str_ori);


            AssetManager assetManager= (AssetManager) cAm.newInstance();
            method.invoke(assetManager,str_ori);
            method.invoke(assetManager,context.getApplicationInfo().sourceDir);
            sm_resources = new Resources(assetManager, context.getResources().getDisplayMetrics(), context.getResources().getConfiguration());
            str_res_path = str_ori;
        } catch (Exception e) {
            Log.d("xfhsm", "fn_patch_res-try::" + e.toString());
        }
    }




    private static String str_res_path = "";

    public static Resources get_patch_res(Context context) {
        if (TextUtils.isEmpty(str_res_path)) {
            return null;
        }
        try {
            Class cAm = Class.forName("android.content.res.AssetManager");
            AssetManager objAm = (AssetManager) cAm.newInstance();

            Method method = cAm.getDeclaredMethod("addAssetPath", String.class);
            method.invoke(objAm, str_res_path);
            return new Resources(objAm, context.getResources().getDisplayMetrics(), context.getResources().getConfiguration());
        } catch (Exception e) {
            Log.d("xfhsm", "get_patch_res-try::" + e.toString());
        }
        return null;
    }

    public static void replaceActivityResources(Activity activity, Resources resources) {
        if (!isEnable){
            return;
        }

        try {
            Class<?> aClass = Class.forName("android.view.ContextThemeWrapper");
            Field mResources = aClass.getDeclaredField("mResources");
            mResources.setAccessible(true);

            if (resources != null) {
                mResources.set(activity, resources);
            }

        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        hookAppCompatActivityResource(activity, resources);
        hookAppCompatActivityResourceIsMyApp(activity, resources);
    }

    private static void hookAppCompatActivityResource(Activity activity,Resources resources){
        try {
            Class<?> aClass = Class.forName("androidx.core.app.AppCompatActivity");

            Field mResources = aClass.getDeclaredField("mResources");
            mResources.setAccessible(true);

            if (resources != null) {
                mResources.set(activity, resources);
            }

        } catch (Exception e) {

        }

    }

    private static void hookAppCompatActivityResourceIsMyApp(Activity activity,Resources resources){
        try {
            Class<?> aClass = AppCompatActivity.class;
            Field mResources = aClass.getDeclaredField("mResources");
            mResources.setAccessible(true);
            if (resources != null) {
                mResources.set(activity, resources);
            }

        } catch (Exception e) {

        }

    }


    public Resources getResources() {
        return sm_resources;
    }

    public AssetManager getAssets() {
        return sm_resources == null ? null : sm_resources.getAssets();
    }

}
