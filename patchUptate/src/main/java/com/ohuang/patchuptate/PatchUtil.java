package com.ohuang.patchuptate;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.ohuang.base.FileUtils;
import com.ohuang.base.SPUtil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * 补丁类
 */
public class PatchUtil {
    public static final String TAG = "PatchUtil";
    public static Patch patch;
    public static final String rootPath="/ohPatch";
    public static final String dexPath=rootPath+"/dex.apk";
    public static final String resApk=rootPath+"/res.apk";
    public static final String lib=rootPath+"/lib";
    public static final String temp=rootPath+"/temp";

    private PatchUtil() {
    }

    private static final class InstanceHolder {
        static final PatchUtil instance = new PatchUtil();
    }

    public static PatchUtil getInstance() {
        return InstanceHolder.instance;
    }

    public void init(Context context){
        hotUpdate(context);
    }

    public void hotUpdate(Context base) {
        Log.d(TAG, "init: supportedAbis="+ Arrays.toString(Build.SUPPORTED_ABIS));
        Log.d(TAG, "init: nativeLibraryDir="+base.getApplicationInfo().nativeLibraryDir);
        Log.d(TAG, "init: nativeLibraryDir="+new File(base.getApplicationInfo().nativeLibraryDir).getName());
        boolean isLoader = (boolean) SPUtil.getInstance("PatchUtil").get(base, "isLoader", false);
        if (!isLoader) { //没有补丁或者补丁没有完全加载
            Log.d(TAG, "init: 没有补丁或上次补丁加载失败");
            return;
        }
        String str_patch_apk = base.getFilesDir().getAbsolutePath() +dexPath;

        File f = new File(str_patch_apk);

        String str_lib_cache_dir = base.getFilesDir().getAbsolutePath()+rootPath;//lib和cache目录必须在/data/data/包名 的目录下！

        if (f.exists()) {
            Log.d(TAG, "init: 开始热更新");
            patch = new Patch();
            patch.fn_patch_dex(base, str_patch_apk, str_lib_cache_dir + File.separator + "adpatchcache"); //dex热更新
            String[] supportedAbis = Build.SUPPORTED_ABIS;
            Log.d(TAG, "init: supportedAbis="+ Arrays.toString(supportedAbis));
            String soPath=  null;
            File file1 = new File(base.getApplicationInfo().nativeLibraryDir);
            String name = file1.getName();
            switch (name){
                case "arm64":
                    if (contains(supportedAbis,"arm64-v8a")){
                        File file = new File(str_lib_cache_dir + File.separator + "lib/arm64-v8a" );
                        if (file.exists()) {
                            soPath = str_lib_cache_dir + File.separator + "lib/arm64-v8a";
                        }
                    }
                    break;
                case "arm":
                    if (contains(supportedAbis,"armeabi-v7a")){
                        File file = new File(str_lib_cache_dir + File.separator + "lib/armeabi-v7a" );
                        if (file.exists()) {
                            soPath = str_lib_cache_dir + File.separator + "lib/armeabi-v7a";
                        }
                    }
                    if (soPath==null){
                        if (contains(supportedAbis,"armeabi")){
                            File file = new File(str_lib_cache_dir + File.separator + "lib/armeabi" );
                            if (file.exists()) {
                                soPath = str_lib_cache_dir + File.separator + "lib/armeabi";
                            }
                        }
                    }
                    break;
                case "x86":
                    if (contains(supportedAbis,"x86")){
                        File file = new File(str_lib_cache_dir + File.separator + "lib/x86" );
                        if (file.exists()) {
                            soPath = str_lib_cache_dir + File.separator + "lib/x86";
                        }
                    }
                    break;
            }
            if (soPath==null) {
                for (String supportedAbi : supportedAbis) {
                    File file = new File(str_lib_cache_dir + File.separator + "lib/" + supportedAbi);
                    if (file.exists()) {
                        soPath = str_lib_cache_dir + File.separator + "lib/" + supportedAbi;
                        break;
                    }
                }
            }
            if (soPath!=null) {
                Log.d(TAG, "init: soPath="+soPath);
                patch.fn_patch_lib(base, soPath);  //so库热更新
            }
            ResPatch.getResPatch(base, base.getFilesDir().getAbsolutePath() + resApk);  //资源热更新

        }

    }

    public boolean contains(Object[] objects,Object o){
        for (Object object : objects) {
            if (o.equals(object)){
                return true;
            }
        }
        return false;
    }

    /**
     * 加载补丁
     *
     * @param context
     * @param path
     */
    public void loadPatchApk(Context context, String path) throws IOException {
        File file1 = new File(path);
        if (!file1.exists()){
            return;
        }
        SPUtil.getInstance("PatchUtil").put(context, "isLoader", false);
        String str_patch_apk = context.getFilesDir().getAbsolutePath() + dexPath;
        File file2 = new File(str_patch_apk);
        if (file2.exists()){
            file2.delete();
        }
        if (file2.getParentFile()!=null){
            file2.getParentFile().mkdirs();
        }
        FileUtils.copyFileUsingFileStreams(file1, file2);
        String str_lib_cache_dir = context.getFilesDir().getAbsolutePath()+rootPath;//lib和cache目录必须在/data/data/包名 的目录下！
        String libPath = str_lib_cache_dir + File.separator + "lib/";
        File file = new File(libPath);
        if (file.exists()) {
            FileUtils.delete(file);
            file.mkdirs();
        }
        copyApkLib(path,libPath);
        toResApk(context,path);

        SPUtil.getInstance("PatchUtil").put(context, "isLoader", true);
    }
    
    private void toResApk(Context context,String path) throws IOException {
//        File file1 = new File(path);
//        String str_patch_apk = context.getFilesDir().getAbsolutePath() + resApk;
//        File file2 = new File(str_patch_apk);
//        if (file2.exists()){
//            file2.delete();
//        }
//        if (file2.getParentFile()!=null){
//            file2.getParentFile().mkdirs();
//        }
//        FileUtils.copyFileUsingFileStreams(file1, file2);

        ResApk.toResApk(context,path);
        File file = new File(context.getFilesDir().getAbsolutePath() + PatchUtil.temp);
        if (file.exists()) {
            file.delete();
        }
    }


    private void copyApkLib(String apkPath,String outPath){
        // 要进行解压缩的zip文件
        File zipFile = new File(apkPath);

        // 1.创建解压缩目录
        // 获取zip文件的名称
        String zipFileName = zipFile.getName();

        // 2.解析读取zip文件
        try (ZipInputStream in = new ZipInputStream(new FileInputStream(zipFile))) {
            // 遍历zip文件中的每个子文件
            ZipEntry zipEntry = null;
            while ((zipEntry = in.getNextEntry()) != null) {
                // 获取zip压缩包中的子文件名称
                String zipEntryFileName = zipEntry.getName();

                if (zipEntryFileName.startsWith("lib/")&& !zipEntry.isDirectory()) {
                    Log.d(TAG, "copyApkLib: "+zipEntryFileName);
                    String replace = zipEntryFileName.replace("lib/", outPath);
                    // 创建解压缩目录
                    File targetDir = new File(replace);
                    if (targetDir.getParentFile()!=null) {
                        targetDir.getParentFile().mkdirs(); // 创建目录
                    }
                    // 创建该文件的输出流

                    // 输出流定义在try()块，结束自动清空缓冲区并关闭
                    try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(replace))) {

                        // 读取该子文件的字节内容
                        byte[] buff = new byte[1024];
                        int len = -1;
                        while ((len = in.read(buff)) != -1) {
                            bos.write(buff, 0, len);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
