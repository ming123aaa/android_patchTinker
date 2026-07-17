package com.ohuang.patchtinker.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import java.util.Map;

public class PatchLayoutFactory implements LayoutInflater.Factory2 {
    private LayoutInflater.Factory2 mBaseFactory;
    private Map<Integer, Integer> mIdMap;

    public PatchLayoutFactory(LayoutInflater.Factory2 baseFactory, Map<Integer, Integer> idMap) {
        this.mBaseFactory = baseFactory;
        this.mIdMap = idMap;
    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        // 用我们的PatchAttributeSet包装一下attrs


        // 继续调用原有的Factory2，把包装后的属性传下去
        if (mBaseFactory != null) {
            View view = mBaseFactory.onCreateView(parent, name, context, attrs);

            return view;
        }
        return null;
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {

        if (mBaseFactory != null) {
            return mBaseFactory.onCreateView(name, context, attrs);
        }
        return null;
    }
}
