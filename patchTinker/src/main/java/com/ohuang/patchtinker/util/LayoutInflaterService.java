package com.ohuang.patchtinker.util;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;

import com.ohuang.patchtinker.tinker.ShareReflectUtil;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

public class LayoutInflaterService {


    public static void replaceServices(Map<Integer, Integer> idMap){
        try {
            Class clazz=Class.forName("android.app.SystemServiceRegistry");
            Field systemServiceFetchers = ShareReflectUtil.findField(clazz, "SYSTEM_SERVICE_FETCHERS");
            Map<String,Object> servicesMap= (Map<String, Object>) systemServiceFetchers.get(null);
            Object oldFetcher = servicesMap.get(Context.LAYOUT_INFLATER_SERVICE);
            Object newFetcher = setServiceFetchers(oldFetcher, idMap);
            servicesMap.put(Context.LAYOUT_INFLATER_SERVICE,newFetcher);
        } catch (Throwable e) {
           e.printStackTrace();
        }
    }

    private static Object setServiceFetchers(Object object, Map<Integer, Integer> idMap) throws ClassNotFoundException {
        Class<?> aClass = Class.forName("android.app.SystemServiceRegistry$ServiceFetcher");
        return  Proxy.newProxyInstance(LayoutInflaterService.class.getClassLoader(),new Class[]{aClass}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                method.setAccessible(true);
                if (method.getName().equals("getService")){
                    Object result=method.invoke(object,args);
                    if (result instanceof LayoutInflater){
                        result= new PatchLayoutInflater((LayoutInflater)result,idMap);
                        Log.i("LayoutInflaterService","getPatchLayoutInflater()");
                    }

                    return result;
                }

                return method.invoke(object,args);
            }
        });

    }

}
