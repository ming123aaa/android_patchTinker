package com.ohuang.patchuptate.ohkv;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

public class OHKV {
    String kvName;

    public OHKV(String kvName) {
        this.kvName = kvName;
    }

    public void put(Context context, String key, Object object) {

        JSONObject jsonObject = DataSp.readData(context, kvName);
        try {
            jsonObject.put(key, object);
        } catch (JSONException e) {

        }
        DataSp.saveData(context, kvName, jsonObject);
    }

    /**
     * 得到保存数据的方法，我们根据默认值得到保存的数据的具体类型，然后调用相对于的方法获取值
     *
     * @param context
     * @param key
     * @param defaultObject
     * @return
     */
    public Object get(Context context, String key, Object defaultObject) {
        JSONObject jsonObject = DataSp.readData(context, kvName);
        if (defaultObject instanceof String) {
            return jsonObject.optString(key, (String) defaultObject);
        } else if (defaultObject instanceof Integer) {
            return jsonObject.optInt(key, (Integer) defaultObject);
        } else if (defaultObject instanceof Boolean) {
            return jsonObject.optBoolean(key, (Boolean) defaultObject);
        } else if (defaultObject instanceof Long) {
            return jsonObject.optLong(key, (Long) defaultObject);
        }else if (defaultObject instanceof Double){
            return jsonObject.optDouble(key, (Double) defaultObject);
        }else {
            Object opt = jsonObject.opt(key);
            if (opt==null) {
                return defaultObject;
            }else {
                return opt;
            }
        }
    }
}
