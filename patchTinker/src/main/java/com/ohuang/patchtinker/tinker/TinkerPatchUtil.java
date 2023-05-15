package com.ohuang.patchtinker.tinker;

import android.app.Application;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import dalvik.system.PathClassLoader;

public class TinkerPatchUtil {

    /**
     * 加载DexPatch
     * @param context
     * @param apkPath
     * @param rootPath
     */
    public static void loadDexPatch(Application context, String apkPath, String rootPath) {
        File file = new File(apkPath);

        if (file.exists()) {
            List<File> data=new ArrayList<>();
            data.add(file);
            final String optimizeDexDirectory = rootPath + "/odex/";
            String optimizedPath = optimizedPathFor(file, new File(optimizeDexDirectory));
            PathClassLoader classLoader = (PathClassLoader) context.getClassLoader();
            try {
                SystemClassLoaderAdder.installDexes(context, classLoader, new File(optimizedPath),data);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * change the jar file path as the makeDexElements do
     * Android O change its path
     *
     * @param path
     * @param optimizedDirectory
     * @return
     */
     static String optimizedPathFor(File path, File optimizedDirectory) {
        if (ShareTinkerInternals.isAfterAndroidO()) {
            // dex_location = /foo/bar/baz.jar
            // odex_location = /foo/bar/oat/<isa>/baz.odex

            String currentInstructionSet;
            try {
                currentInstructionSet = ShareTinkerInternals.getCurrentInstructionSet();
            } catch (Exception e) {
                throw new RuntimeException("getCurrentInstructionSet fail:", e);
            }

            File parentFile = path.getParentFile();
            String fileName = path.getName();
            int index = fileName.lastIndexOf('.');
            if (index > 0) {
                fileName = fileName.substring(0, index);
            }

            String result = parentFile.getAbsolutePath() + "/oat/"
                    + currentInstructionSet + "/" + fileName + ShareConstants.ODEX_SUFFIX;
            return result;
        }

        String fileName = path.getName();
        if (!fileName.endsWith(ShareConstants.DEX_SUFFIX)) {
            int lastDot = fileName.lastIndexOf(".");
            if (lastDot < 0) {
                fileName += ShareConstants.DEX_SUFFIX;
            } else {
                StringBuilder sb = new StringBuilder(lastDot + 4);
                sb.append(fileName, 0, lastDot);
                sb.append(ShareConstants.DEX_SUFFIX);
                fileName = sb.toString();
            }
        }

        File result = new File(optimizedDirectory, fileName);
        return result.getPath();
    }
}
