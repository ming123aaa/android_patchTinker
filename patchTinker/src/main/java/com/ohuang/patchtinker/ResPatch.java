package com.ohuang.patchtinker;

import android.content.Context;

import com.ohuang.patchtinker.ohkv.OHKVUtil;
import com.ohuang.patchtinker.tinker.TinkerResourcePatcher;

public class ResPatch {




     static void getResPatch(Context context, String str_ori) {
        if (!isIsEnable(context)){
            return;
        }
        tinkerResPatch(context, str_ori);

    }

     static boolean isIsEnable(Context context) {
        return (boolean) OHKVUtil.getInstance(PatchUtil.SP_PatchUtil).get(context,"resEnable",false);
    }

     static void setIsEnable(Context context,boolean isEnable) {
         OHKVUtil.getInstance(PatchUtil.SP_PatchUtil).put(context,"resEnable",isEnable);
    }

    /**
     * tinker资源热更新方案
     * @param context
     * @param str_ori
     */
     static void tinkerResPatch(Context context,String str_ori){
        try {
            TinkerResourcePatcher.isResourceCanPatch(context);
            TinkerResourcePatcher.monkeyPatchExistingResources(context,str_ori,false);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }



}
