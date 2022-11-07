package com.ohuang.hotupdate;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflexUtil {


    public static Object invoke(Object o, Class<?> className, String methodName, Class<?>[] parameterTypes, Object[] objects) {
        try {
            Method method = className.getMethod(methodName, parameterTypes);
            return method.invoke(o, objects);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static Object invoke(Object o, ClassLoader classLoader, String className, String methodName, Class<?>[] parameterTypes, Object[] objects) {
        try {
            Class<?> aClass = classLoader.loadClass(className);
            invoke(o, aClass, methodName, parameterTypes, objects);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object invoke(Object o, String className, String methodName, Class<?>[] parameterTypes, Object[] objects) {
        try {
            Class<?> aClass = Class.forName(className);
            invoke(o, aClass, methodName, parameterTypes, objects);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static void setField(Object o, Object value,Class<?> className, String filedName) {
        Field declaredField = null;
        try {
            declaredField = className.getDeclaredField(filedName);
            declaredField.setAccessible(true);
            declaredField.set(o,value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
;
    }
    public static void setField(Object o, Object value,ClassLoader classLoader, String className, String filedName) {
        Class<?> aClass = null;
        try {
            aClass = classLoader.loadClass(className);
            setField(o,value, aClass, filedName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    public static void setField(Object o, String value ,String className, String filedName) {
        Class<?> aClass = null;
        try {
            aClass = Class.forName(className);
           setField(o,value, aClass, filedName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    public static Object getField(Object o, Class<?> className, String filedName) {
        Field declaredField = null;
        try {
            declaredField = className.getDeclaredField(filedName);
            declaredField.setAccessible(true);
            return declaredField.get(o);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object getField(Object o, ClassLoader classLoader, String className, String filedName) {
        Class<?> aClass = null;
        try {
            aClass = classLoader.loadClass(className);
            return getField(o, aClass, filedName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object getField(Object o, String className, String filedName) {
        Class<?> aClass = null;
        try {
            aClass = Class.forName(className);
            return getField(o, aClass, filedName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
