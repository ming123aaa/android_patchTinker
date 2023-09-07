package com.ohuang.patchtinker.tinker;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.os.Build;

import com.ohuang.patchtinker.WhiteClassUtil;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import dalvik.system.DexFile;
import dalvik.system.PathClassLoader;

/**
 * Created by zhangshaowen on 16/7/24.
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
class AndroidNClassLoader extends PathClassLoader {
    private static final String TAG = "Tinker.NClassLoader";

    private final PathClassLoader originClassLoader;
    private String applicationClassName;

    private List<String> whiteClassStartWith;//白名单class startWith
    private List<String> whiteClassEquals;//白名单class equals

    private AndroidNClassLoader(String dexPath, PathClassLoader parent, Application application) {
        super(dexPath, parent.getParent());
        originClassLoader = parent;
        whiteClassStartWith= WhiteClassUtil.getWhiteClassStartWith(application);
        whiteClassEquals= WhiteClassUtil.getWhiteClassEquals(application);
        String name = application.getClass().getName();
        if (!name.equals("android.app.Application")) {
            applicationClassName = name;
        }
    }

    @SuppressWarnings("unchecked")
    private static Object recreateDexPathList(Object originalDexPathList, ClassLoader newDefiningContext) throws Exception {
        final Field dexElementsField = ShareReflectUtil.findField(originalDexPathList, "dexElements");
        final Object[] dexElements = (Object[]) dexElementsField.get(originalDexPathList);
        final Field nativeLibraryDirectoriesField = ShareReflectUtil.findField(originalDexPathList, "nativeLibraryDirectories");
        final List<File> nativeLibraryDirectories = (List<File>) nativeLibraryDirectoriesField.get(originalDexPathList);

        final StringBuilder dexPathBuilder = new StringBuilder();
        final Field dexFileField = ShareReflectUtil.findField(dexElements.getClass().getComponentType(), "dexFile");

        boolean isFirstItem = true;
        for (Object dexElement : dexElements) {
            final DexFile dexFile = (DexFile) dexFileField.get(dexElement);
            if (dexFile == null) {
                continue;
            }
            if (isFirstItem) {
                isFirstItem = false;
            } else {
                dexPathBuilder.append(File.pathSeparator);
            }
            dexPathBuilder.append(dexFile.getName());
        }

        final String dexPath = dexPathBuilder.toString();

        final StringBuilder libraryPathBuilder = new StringBuilder();
        isFirstItem = true;
        for (File libDir : nativeLibraryDirectories) {
            if (libDir == null) {
                continue;
            }
            if (isFirstItem) {
                isFirstItem = false;
            } else {
                libraryPathBuilder.append(File.pathSeparator);
            }
            libraryPathBuilder.append(libDir.getAbsolutePath());
        }

        final String libraryPath = libraryPathBuilder.toString();

        final Constructor<?> dexPathListConstructor = ShareReflectUtil.findConstructor(originalDexPathList, ClassLoader.class, String.class, String.class, File.class);
        return dexPathListConstructor.newInstance(newDefiningContext, dexPath, libraryPath, null);
    }

    private static AndroidNClassLoader createAndroidNClassLoader(PathClassLoader originalClassLoader, Application application) throws Exception {
        //let all element ""
        final AndroidNClassLoader androidNClassLoader = new AndroidNClassLoader("", originalClassLoader, application);
        final Field pathListField = ShareReflectUtil.findField(originalClassLoader, "pathList");
        final Object originPathList = pathListField.get(originalClassLoader);

        // To avoid 'dex file register with multiple classloader' exception on Android O, we must keep old
        // dexPathList in original classloader so that after the newly loaded base dex was bound to
        // AndroidNClassLoader we can still load class in base dex from original classloader.

        Object newPathList = recreateDexPathList(originPathList, androidNClassLoader);

        // Update new classloader's pathList.
        pathListField.set(androidNClassLoader, newPathList);

        return androidNClassLoader;
    }

    private static void reflectPackageInfoClassloader(Application application, ClassLoader reflectClassLoader) throws Exception {
        String defBase = "mBase";
        String defPackageInfo = "mPackageInfo";
        String defClassLoader = "mClassLoader";

        Context baseContext = (Context) ShareReflectUtil.findField(application, defBase).get(application);
        Object basePackageInfo = ShareReflectUtil.findField(baseContext, defPackageInfo).get(baseContext);
        Field classLoaderField = ShareReflectUtil.findField(basePackageInfo, defClassLoader);
        Thread.currentThread().setContextClassLoader(reflectClassLoader);
        classLoaderField.set(basePackageInfo, reflectClassLoader);
    }

    public static AndroidNClassLoader inject(PathClassLoader originClassLoader, Application application) throws Exception {
        AndroidNClassLoader classLoader = createAndroidNClassLoader(originClassLoader, application);
        reflectPackageInfoClassloader(application, classLoader);
        return classLoader;
    }

//    public static String getLdLibraryPath(ClassLoader loader) throws Exception {
//        String nativeLibraryPath;
//
//        nativeLibraryPath = (String) loader.getClass()
//            .getMethod("getLdLibraryPath", new Class[0])
//            .invoke(loader, new Object[0]);
//
//        return nativeLibraryPath;
//    }

    public Class<?> findClass(String name) throws ClassNotFoundException {
        // loader class use default pathClassloader to load
        if ((name != null
                && name.startsWith("com.ohuang.patchtinker"))
                || (applicationClassName != null && applicationClassName.equals(name))||match(name)) {
            return originClassLoader.loadClass(name);
        }
        Class<?> clazz;
        clazz = super.findClass(name);
        return clazz;
    }

    public boolean match(String name){
        for (int i = 0; i < whiteClassStartWith.size(); i++) {
            if (name.startsWith(whiteClassStartWith.get(i))){
                return true;
            }
        }
        for (int i = 0; i < whiteClassEquals.size(); i++) {
            if (name.equals(whiteClassEquals.get(i))){
                return true;
            }
        }
        return false;
    }

    @Override
    public String findLibrary(String name) {
        return super.findLibrary(name);
    }
}