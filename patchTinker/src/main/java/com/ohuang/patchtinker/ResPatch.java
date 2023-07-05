package com.ohuang.patchtinker;

import android.content.Context;

import com.ohuang.patchtinker.ohkv.OHKVUtil;
import com.ohuang.patchtinker.tinker.TinkerResourcePatcher;

public class ResPatch {




     static void getResPatch(Context context, String str_ori) {
        tinkerResPatch(context, str_ori);

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
