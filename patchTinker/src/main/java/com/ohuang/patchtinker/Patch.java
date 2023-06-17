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




    //反射得到类加载器中的pathList对象
    private Object getPathList(Object baseDexClassLoader) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        return getField(baseDexClassLoader, Class.forName("dalvik.system.BaseDexClassLoader"), "pathList");
    }



    //反射得到对象中的属性值
    private Object getField(Object obj, Class<?> cl, String field) throws NoSuchFieldException, IllegalAccessException {
        Field localField = cl.getDeclaredField(field);
        localField.setAccessible(true);
        return localField.get(obj);
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





