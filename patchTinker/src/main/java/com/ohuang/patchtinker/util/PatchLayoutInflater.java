package com.ohuang.patchtinker.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.xmlpull.v1.XmlPullParser;

import java.util.Map;

public class PatchLayoutInflater extends LayoutInflater {
    private static final String TAG = "PatchLayoutInflater";
    private LayoutInflater mBase;
    private Map<Integer, Integer> mIdMap;

    protected PatchLayoutInflater(LayoutInflater original, Map<Integer, Integer> idMap) {
        super(original.getContext());
        this.mBase = original;
        this.mIdMap = idMap;
    }

    public static PatchLayoutInflater createLayoutInflater(LayoutInflater original, Map<Integer, Integer> idMap){
        if (original instanceof PatchLayoutInflater){
            return (PatchLayoutInflater) original;
        }
         return new PatchLayoutInflater(original, idMap);
    }

    
    @Override
    public LayoutInflater cloneInContext(Context newContext) {
        LayoutInflater layoutInflater = mBase.cloneInContext(newContext);
        if (layoutInflater instanceof PatchLayoutInflater){
            return layoutInflater;
        }
        return new PatchLayoutInflater(layoutInflater, mIdMap);
    }
    
    // ========== 核心：拦截 inflate ==========


    public int replaceId(int id){
        if (mIdMap.containsKey(id)){
            return mIdMap.get( id );
        }
        return id;
    }


    @Override
    public View inflate(int resource, ViewGroup root, boolean attachToRoot) {
        // 也拦截布局文件本身的ID（如果你需要替换整个布局）
        int newResId = replaceId(resource);
        if (newResId != resource) {
            Log.d(TAG, "布局替换: 0x" + Integer.toHexString(resource)
                  + " → 0x" + Integer.toHexString(newResId));
        }
        return mBase.inflate(newResId, root, attachToRoot);
    }
    
    @Override
    public View inflate(XmlPullParser parser, ViewGroup root, boolean attachToRoot) {

        return mBase.inflate(parser, root, attachToRoot);
    }
    
    // ========== 所有其他方法委托给 base ==========
    
    @Override
    public void setFactory(Factory factory) {
        mBase.setFactory(factory);
    }
    
    @Override
    public void setFactory2(Factory2 factory) {
        if (factory instanceof PatchLayoutFactory) {
            mBase.setFactory2(factory);
        }else {
            mBase.setFactory2(new PatchLayoutFactory(factory,mIdMap));
        }
    }

    
    @Override
    public void setFilter(Filter filter) {
        mBase.setFilter(filter);
    }
    
    @Override
    public Filter getFilter() {
        return mBase.getFilter();
    }

}