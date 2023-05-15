package com.ohuang.patchtinker;

import android.util.Log;

public class LogUtil {

    public static final String TAG = "loadDex";

    public static void info(String msg){
        Log.i(TAG,msg);
    }

    public static void error(String msg){
        Log.e(TAG,msg);
    }

    public static void warn(String msg){
        Log.w(TAG,msg);
    }
}
