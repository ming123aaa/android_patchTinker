package com.ohuang.patchtinker;

import android.content.Context;

public class ResPatch {

    static void getResPatch(Context context, String str_ori, boolean isV2Patch) {

        if(isV2Patch){
            ResPatchV2.tinkerResPatch(context, str_ori);
        }else{
            ResPatchV1.tinkerResPatch(context, str_ori);
        }

    }
}
