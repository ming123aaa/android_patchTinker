package com.ohuang.patchtinker.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.AssetFileDescriptor;
import android.content.res.XmlResourceParser;
import android.graphics.Movie;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import java.io.InputStream;
import java.util.Map;

public  class ResourcesRouter extends Resources {


    Map<Integer,Integer> ids;

    /**
     * Create a new Resources object on top of an existing set of assets in an
     * AssetManager.
     *
     * @param assets  Previously created AssetManager.
     * @param metrics Current display metrics to consider when
     *                selecting/computing resource values.
     * @param config  Desired device configuration to consider when
     *                selecting/computing resource values (optional).
     * @deprecated Resources should not be constructed by apps.
     * See {@link Context#createConfigurationContext(Configuration)}.
     */
    public ResourcesRouter(AssetManager assets, DisplayMetrics metrics, Configuration config, Map<Integer,Integer> ids) {
        super(assets, metrics, config);
        this.ids=ids;
    }


    public int replaceId(int id){
        if (ids.containsKey(id)){
            return ids.get( id );
        }
        return id;
    }





    @Override
    public boolean getBoolean(int id) throws NotFoundException {
        return super.getBoolean(replaceId(id));
    }


    @Override
    public CharSequence getQuantityText(int id, int quantity) throws NotFoundException {
        return super.getQuantityText(replaceId(id), quantity);
    }

    @Override
    public CharSequence getText(int id) throws NotFoundException {
        return super.getText(replaceId(id));
    }

    @Override
    public CharSequence getText(int id, CharSequence def) {
        return super.getText(replaceId(id), def);
    }

    @Override
    public CharSequence[] getTextArray(int id) throws NotFoundException {
        return super.getTextArray(replaceId(id));
    }

    @Override
    public ColorStateList getColorStateList(int id, Theme theme) throws NotFoundException {
        return super.getColorStateList(replaceId(id), theme);
    }

    @Override
    public Drawable getDrawable(int id, Theme theme) throws NotFoundException {
        return super.getDrawable(replaceId(id), theme);
    }

    @Override
    public Drawable getDrawableForDensity(int id, int density, Theme theme) {
        return super.getDrawableForDensity(replaceId(id), density, theme);
    }

    @Override
    public float getDimension(int id) throws NotFoundException {
        return super.getDimension(replaceId(id));
    }

    @Override
    public float getFloat(int id) {
        return super.getFloat(replaceId(id));
    }

    @Override
    public float getFraction(int id, int base, int pbase) {
        return super.getFraction(replaceId(id), base, pbase);
    }

    @Override
    public int getColor(int id, Theme theme) throws NotFoundException {
        return super.getColor(replaceId(id), theme);
    }

    @Override
    public int getDimensionPixelOffset(int id) throws NotFoundException {
        return super.getDimensionPixelOffset(replaceId(id));
    }

    @Override
    public int getDimensionPixelSize(int id) throws NotFoundException {
        return super.getDimensionPixelSize(replaceId(id));
    }

    @Override
    public int getInteger(int id) throws NotFoundException {
        return super.getInteger(replaceId(id));
    }

    @Override
    public int[] getIntArray(int id) throws NotFoundException {
        return super.getIntArray(replaceId(id));
    }

    @Override
    public String getResourceEntryName(int resid) throws NotFoundException {
        return super.getResourceEntryName(replaceId(resid));
    }

    @Override
    public String getResourceName(int resid) throws NotFoundException {
        return super.getResourceName(replaceId(resid));
    }

    @Override
    public String getQuantityString(int id, int quantity) throws NotFoundException {
        return super.getQuantityString(replaceId(id), quantity);
    }

    @Override
    public String getResourcePackageName(int resid) throws NotFoundException {
        return super.getResourcePackageName(replaceId(resid));
    }

    @Override
    public String getString(int id) throws NotFoundException {
        return super.getString(replaceId(id));
    }

    @Override
    public String getQuantityString(int id, int quantity, Object... formatArgs) throws NotFoundException {
        return super.getQuantityString(replaceId(id), quantity, formatArgs);
    }

    @Override
    public String getString(int id, Object... formatArgs) throws NotFoundException {
        return super.getString(replaceId(id), formatArgs);
    }

    @Override
    public String[] getStringArray(int id) throws NotFoundException {
        return super.getStringArray(replaceId(id));
    }

    @Override
    public String getResourceTypeName(int resid) throws NotFoundException {
        return super.getResourceTypeName(replaceId(resid));
    }

    @Override
    public Typeface getFont(int id) throws NotFoundException {
        return super.getFont(replaceId(id));
    }

    @Override
    public void getValue(int id, TypedValue outValue, boolean resolveRefs) throws NotFoundException {
        super.getValue(replaceId(id), outValue, resolveRefs);
    }

    @Override
    public void getValueForDensity(int id, int density, TypedValue outValue, boolean resolveRefs) throws NotFoundException {
        super.getValueForDensity(replaceId(id), density, outValue, resolveRefs);
    }

    @Override
    public XmlResourceParser getLayout(int id) throws NotFoundException {
        return super.getLayout(replaceId(id));
    }


    @Override
    public XmlResourceParser getXml(int id) throws NotFoundException {
        return super.getXml(replaceId(id));
    }

    @Override
    public XmlResourceParser getAnimation(int id) throws NotFoundException {
        return super.getAnimation(replaceId(id));
    }

    @Override
    public InputStream openRawResource(int id) throws NotFoundException {
        return super.openRawResource(replaceId(id));
    }

    @Override
    public InputStream openRawResource(int id, TypedValue value) throws NotFoundException {
        return super.openRawResource(replaceId(id), value);
    }

    @Override
    public AssetFileDescriptor openRawResourceFd(int id) throws NotFoundException {
        return super.openRawResourceFd(replaceId(id));
    }

    @Override
    public int getIdentifier(String name, String defType, String defPackage) {
        int identifier = super.getIdentifier(name, defType, defPackage);
        return replaceId(identifier);
    }

    @Override
    public ColorStateList getColorStateList(int id) throws NotFoundException {
        return super.getColorStateList(replaceId(id));
    }

    @Override
    public Drawable getDrawable(int id) throws NotFoundException {
        return super.getDrawable(replaceId(id));
    }

    @Override
    public int getColor(int id) throws NotFoundException {
        return super.getColor(replaceId(id));
    }

    @Override
    public Drawable getDrawableForDensity(int id, int density) throws NotFoundException {
        return super.getDrawableForDensity(replaceId(id), density);
    }

    @Override
    public Movie getMovie(int id) throws NotFoundException {
        return super.getMovie(replaceId(id));
    }

    @Override
    public void getValue(String name, TypedValue outValue, boolean resolveRefs) throws NotFoundException {
        super.getValue(name, outValue, resolveRefs);
    }
    
    
}