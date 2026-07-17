package com.ohuang.patchtinker.util;

import android.util.AttributeSet;

import java.util.Map;

public  class PatchAttributeSet implements AttributeSet {
    private AttributeSet mBase;
    private Map<Integer, Integer> mIdMap;

    public PatchAttributeSet(AttributeSet base, Map<Integer, Integer> idMap) {
        this.mBase = base;
        this.mIdMap = idMap;
    }

    // ========== 核心拦截方法 ==========

    // 这是最关键的拦截点：根据属性索引获取其资源ID值
    @Override
    public int getAttributeResourceValue(int index, int defaultValue) {
        int oldId = mBase.getAttributeResourceValue(index, defaultValue);
        return applyPatchId(oldId);
    }

    @Override
    public int getAttributeIntValue(int index, int defaultValue) {
        return  mBase.getAttributeIntValue(index,defaultValue);
    }

    @Override
    public int getAttributeUnsignedIntValue(int index, int defaultValue) {
        return mBase.getAttributeUnsignedIntValue(index, defaultValue);
    }

    @Override
    public float getAttributeFloatValue(int index, float defaultValue) {
        return mBase.getAttributeFloatValue(index, defaultValue);
    }

    @Override
    public String getIdAttribute() {
        return mBase.getIdAttribute();  
    }

    @Override
    public String getClassAttribute() {
        return mBase.getClassAttribute();  
    }

    @Override
    public int getIdAttributeResourceValue(int defaultValue) {
        return mBase.getIdAttributeResourceValue(defaultValue);
    }

    @Override
    public int getStyleAttribute() {
        return mBase.getStyleAttribute();   
    }

    // 根据属性命名空间和名称获取其资源ID值
    @Override
    public int getAttributeResourceValue(String namespace, String name, int defaultValue) {
        int oldId = mBase.getAttributeResourceValue(namespace, name, defaultValue);
        return applyPatchId(oldId);
    }

    @Override
    public int getAttributeIntValue(String namespace, String attribute, int defaultValue) {
        return mBase.getAttributeIntValue(namespace, attribute, defaultValue);
    }

    @Override
    public int getAttributeUnsignedIntValue(String namespace, String attribute, int defaultValue) {
        return mBase.getAttributeUnsignedIntValue(namespace, attribute, defaultValue);
    }

    @Override
    public float getAttributeFloatValue(String namespace, String attribute, float defaultValue) {
        return mBase.getAttributeFloatValue(namespace, attribute, defaultValue);
    }

    @Override
    public int getAttributeListValue(int index, String[] options, int defaultValue) {
        return mBase.getAttributeListValue(index, options, defaultValue);
    }

    @Override
    public boolean getAttributeBooleanValue(int index, boolean defaultValue) {
        return mBase.getAttributeBooleanValue(index, defaultValue); 
    }

    // 辅助方法：ID转换逻辑
    private int applyPatchId(int oldId) {
        if (oldId != 0 && mIdMap.containsKey(oldId)) {
            int newId = mIdMap.get(oldId);
            // 这里可以加日志
            return newId;
        }
        return oldId;
    }

    // ========== 所有其他方法直接委托给mBase ==========
    // 下面列举几个关键的，实际需要委托所有方法
    @Override
    public int getAttributeCount() {
        return mBase.getAttributeCount();
    }

    @Override
    public String getAttributeName(int index) {
        return mBase.getAttributeName(index);
    }

    @Override
    public String getAttributeValue(int index) {
        return mBase.getAttributeValue(index);
    }

    @Override
    public String getAttributeValue(String namespace, String name) {
        return mBase.getAttributeValue(namespace,name);
    }

    @Override
    public String getPositionDescription() {
        return mBase.getPositionDescription();
    }

    @Override
    public int getAttributeNameResource(int index) {
        return mBase.getAttributeNameResource(index);
    }

    @Override
    public int getAttributeListValue(String namespace, String attribute, String[] options, int defaultValue) {
        return mBase.getAttributeListValue( namespace,  attribute,  options,  defaultValue);
    }

    @Override
    public boolean getAttributeBooleanValue(String namespace, String attribute, boolean defaultValue) {
        return mBase.getAttributeBooleanValue(namespace,attribute,defaultValue);
    }


}