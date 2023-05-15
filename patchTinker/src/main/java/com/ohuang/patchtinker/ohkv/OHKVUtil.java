package com.ohuang.patchtinker.ohkv;

import android.content.Context;

import java.util.HashMap;

public class OHKVUtil {
    OHKV ohkv;


    private OHKVUtil(String name) {
        ohkv=new OHKV(name);
    }

    private static HashMap<String, OHKVUtil> SP_MAP = new HashMap();


    public static OHKVUtil getInstance(){
        return getInstance("OHKVUtil");
    }

    public static OHKVUtil getInstance(String name) {
        if (!SP_MAP.containsKey(name)){
            SP_MAP.put(name, new OHKVUtil(name));
        }
        return SP_MAP.get(name);
    }

    public synchronized void put(Context context, String key, Object object) {
        ohkv.put(context, key, object);
    }
    public synchronized Object get(Context context, String key, Object defaultObject) {
        return ohkv.get(context, key, defaultObject);
    }
}
