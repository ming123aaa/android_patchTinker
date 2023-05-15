package com.ohuang.patchtinker;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

public class Patch {
    public void fn_patch_lib(Context context, String str_dir) {
        try {
            if (context == null) {
                return;
            }

            //创建*lib.os文件目录
            File file_obj = new File(str_dir);
            if (!file_obj.exists()) {
                file_obj.mkdirs();
            }

            PathClassLoader pathClassLoader = (PathClassLoader) context.getClassLoader();
            Object pathList = getPathList(pathClassLoader);

            //nativeLibraryDirectories
            Field field_nativeLibraryDirectories = pathList.getClass().getDeclaredField("nativeLibraryDirectories");
            field_nativeLibraryDirectories.setAccessible(true);
            ArrayList<File> array_list_nativeLibraryDirectories = (ArrayList<File>) field_nativeLibraryDirectories.get(pathList);
            array_list_nativeLibraryDirectories = fn_change_array_list_file(array_list_nativeLibraryDirectories, file_obj);
            field_nativeLibraryDirectories.set(pathList, array_list_nativeLibraryDirectories);

            //systemNativeLibraryDirectories
            Field field_systemNativeLibraryDirectories = pathList.getClass().getDeclaredField("systemNativeLibraryDirectories");
            field_systemNativeLibraryDirectories.setAccessible(true);
            ArrayList<File> array_list_systemNativeLibraryDirectories = (ArrayList<File>) field_systemNativeLibraryDirectories.get(pathList);

            //nativeLibraryPathElements
            ArrayList<File> array_list_nativeLibraryPathElements = new ArrayList<File>();
            array_list_nativeLibraryPathElements.clear();
            array_list_nativeLibraryPathElements.addAll(array_list_nativeLibraryDirectories);
            array_list_nativeLibraryPathElements.addAll(array_list_systemNativeLibraryDirectories);
            Object[] o_list = null;
            if (o_list == null) {
                try {
                    Method makePathElements = findMethod(pathList, "makePathElements", List.class);
                    o_list = (Object[]) makePathElements.invoke(pathList, array_list_nativeLibraryPathElements);
                } catch (Exception e) {
                    o_list = null;
                }
            }
            if (o_list == null) {
                try {
                    ArrayList<IOException> suppressedExceptions = new ArrayList<IOException>();
                    Method makePathElements = findMethod(pathList, "makePathElements", List.class, File.class, List.class);
                    o_list = (Object[]) makePathElements.invoke(
                            pathList,
                            array_list_nativeLibraryPathElements,
                            null,
                            suppressedExceptions);
                } catch (Exception e) {
                    o_list = null;
                }
            }
            if (o_list != null) {
                Field field_nativeLibraryPathElements = pathList.getClass().getDeclaredField("nativeLibraryPathElements");
                field_nativeLibraryPathElements.setAccessible(true);
                field_nativeLibraryPathElements.set(pathList, o_list);
            }
        } catch (Exception e) {
            Log.d("xfhsm", "fn_patch_lib-try::" + e.toString());
        }
    }


    public void fn_patch_dex(Context context, String str_ori, String str_obj) {
        try {
            if (context == null) {
                return;
            }
            //创建dex文件输出目录
            File file_obj = new File(str_obj);
            if (!file_obj.exists()) {
                file_obj.mkdirs();
            }
            //加载
            PathClassLoader pathLoader = (PathClassLoader) context.getClassLoader();
            DexClassLoader dexLoader = new DexClassLoader(
                    str_ori,//来源apk文件路径
                    str_obj,//存放dex的解压目录（用于jar、zip、apk格式的补丁）并且此路径只能是app的内部路径（data/data/包名/XXX）
                    null,//加载dex时需要的库的目录
                    pathLoader// 父类加载器
            );
            //合并
            Object obj_dexPathList = getPathList(dexLoader);
            Object obj_pathPathList = getPathList(pathLoader);
            Object obj_leftDexElements = getDexElements(obj_dexPathList);
            Object obj_rightDexElements = getDexElements(obj_pathPathList);
            Object dexElements = combineArray(obj_leftDexElements, obj_rightDexElements);
            //重写给PathList里面的Element[] dexElements;赋值
            Object pathList = getPathList(pathLoader);//一定要重新获取，不要用pathPathList，会报错
            setField(pathList, pathList.getClass(), "dexElements", dexElements);
        } catch (Exception e) {
            Log.d("xfhsm", "fn_patch_dex-try::" + e.toString());
        }
    }

    //反射得到类加载器中的pathList对象
    private Object getPathList(Object baseDexClassLoader) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        return getField(baseDexClassLoader, Class.forName("dalvik.system.BaseDexClassLoader"), "pathList");
    }

    //反射得到pathList中的dexElements
    private Object getDexElements(Object pathList) throws NoSuchFieldException, IllegalAccessException {
        return getField(pathList, pathList.getClass(), "dexElements");
    }

    //反射得到对象中的属性值
    private Object getField(Object obj, Class<?> cl, String field) throws NoSuchFieldException, IllegalAccessException {
        Field localField = cl.getDeclaredField(field);
        localField.setAccessible(true);
        return localField.get(obj);
    }

    //反射给对象中的属性重新赋值
    private void setField(Object obj, Class<?> cl, String field, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field declaredField = cl.getDeclaredField(field);
        declaredField.setAccessible(true);
        declaredField.set(obj, value);
    }

    //对象数组合并
    private Object combineArray(Object arrayLhs, Object arrayRhs) {
        Class<?> componentType = arrayLhs.getClass().getComponentType();
        int i = Array.getLength(arrayLhs);// 得到左数组长度（补丁数组）
        int j = Array.getLength(arrayRhs);// 得到原dex数组长度
        int k = i + j;// 得到总数组长度（补丁数组+原dex数组）
        Object result = Array.newInstance(componentType, k);// 创建一个类型为componentType，长度为k的新数组
        System.arraycopy(arrayLhs, 0, result, 0, i);
        System.arraycopy(arrayRhs, 0, result, i, j);
        return result;
    }



    private ArrayList<File> fn_change_array_list_file(ArrayList<File> array_list_file, File dir) {
        if (array_list_file == null) {
            array_list_file = new ArrayList<>(2);
        }
        //如果已经存在，删除
        final Iterator<File> libDirIt = array_list_file.iterator();
        while (libDirIt.hasNext()) {
            final File libDir = libDirIt.next();
            if (dir.equals(libDir)) {
                libDirIt.remove();
                break;
            }
        }
        array_list_file.add(0, dir);//添加到第一个
        return array_list_file;
    }

    private Method findMethod(Object instance, String name, Class<?>... parameterTypes) throws NoSuchMethodException {
        for (Class<?> clazz = instance.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            try {
                Method method = clazz.getDeclaredMethod(name, parameterTypes);
                if (!method.isAccessible()) {
                    method.setAccessible(true);
                }
                return method;
            } catch (NoSuchMethodException e) {
            }
        }
        throw new NoSuchMethodException("Method "
                + name
                + " with parameters "
                + Arrays.asList(parameterTypes)
                + " not found in " + instance.getClass());
    }

}





