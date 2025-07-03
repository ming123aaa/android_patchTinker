package com.ohuang.patchtinker;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;


import com.ohuang.patchtinker.ohkv.OHKVUtil;
import com.ohuang.patchtinker.tinker.TinkerPatchUtil;
import com.ohuang.patchtinker.util.AndroidXmlUtil;
import com.ohuang.patchtinker.util.FileUtils;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * 补丁类
 */
public class PatchUtil {
    public static final String TAG = "PatchUtil";
    static final String SP_PatchUtil = "SP_PatchUtil";
    static final String SP_KEY_LoaderP2 = "LoaderP2";
    static final String SP_KEY_isLoader = "isLoader";
    static final String SP_KEY_version = "version";
    static final String SP_KEY_P2Version = "P2Version";

    static final String SP_KEY_isV2Patch = "isV2Patch";
    static final String SP_KEY_isV2PatchP2 = "isV2PatchP2";

    static final String SP_KEY_resEnable = "resEnable";
    static final String SP_KEY_resEnable2 = "resEnable2";
    static final String SP_KEY_installInfo = "installInfo";
    static final String SP_KEY_installInfo2 = "installInfo2";

    static final String Meta_KEY_version = "PatchTinker_Version";

    static Patch patch;
    static final String rootPath = "/ohPatch";
    static final String rootPath2 = "/ohPatch2";
    static final String dexPath = rootPath + "/dex.apk";
    static final String dexPath2 = rootPath2 + "/dex.apk";

    static final String lib = rootPath + "/lib";
    static final String lib2 = rootPath2 + "/lib";
    static final String temp = "/ohPatchTemp";
    private final PatchInfo patchInfo = new PatchInfo();

    private PatchUtil() {
    }

    private static final class InstanceHolder {
        static final PatchUtil instance = new PatchUtil();
    }

    public static PatchUtil getInstance() {
        return InstanceHolder.instance;
    }

    /**
     * 获取补丁信息
     *
     * @return
     */
    public PatchInfo getPatchInfo() {
        return patchInfo.copy();
    }

    /**
     * 不建议手动调用,建议使用PatchApplication或者TinkerApplication来初始化
     *
     * @param context
     */
    @Deprecated
    public void init(Application context) {
        hotUpdate(context);
    }

    private void hotUpdate(Application base) {
        patchInfo.patchTinkerVersion = AndroidXmlUtil.getMetaData(base, Meta_KEY_version);
        boolean isLoader = (boolean) OHKVUtil.getInstance(SP_PatchUtil).get(base, SP_KEY_isLoader, false);
        if (isLoader) {
            boolean isLoader2 = (boolean) OHKVUtil.getInstance(SP_PatchUtil).get(base, SP_KEY_LoaderP2, false);
            if (isLoader2) {
                checkPatch2(base);
            } else {
                checkPatch(base);
            }

        } else {
            patchInfo.msg = "不加载补丁";
        }
    }

    private void checkPatch(Application base) {
        boolean isResEnable = (boolean) OHKVUtil.getInstance(PatchUtil.SP_PatchUtil).get(base, SP_KEY_resEnable, false);
        String pVersion = (String) OHKVUtil.getInstance(SP_PatchUtil).get(base, SP_KEY_version, "");
        String installInfo = (String) OHKVUtil.getInstance(SP_PatchUtil).get(base, SP_KEY_installInfo, "");
        boolean isUseV2Patch = (boolean) OHKVUtil.getInstance(SP_PatchUtil).get(base, SP_KEY_isV2Patch, false);
        patchInfo.patchTinkerVersionForInstall = pVersion;
        patchInfo.installInfo = installInfo;
        patchInfo.isV2Patch=isUseV2Patch;
        if (pVersion.equals(patchInfo.patchTinkerVersion)) {
            patchInfo.isUpdate = true;
            if (isResEnable) {
                patchInfo.state = PatchInfo.State.LoadCodeAndRes;
            } else {
                patchInfo.state = PatchInfo.State.LoadCodeNoRes;
            }
            initPatch(base, isResEnable,isUseV2Patch);
        } else {
            patchInfo.isUpdate = false;
            patchInfo.state = PatchInfo.State.PatchVersionError;
            patchInfo.msg = "PatchTinker_Version版本发生变化不热更 当前包版本:" + patchInfo.patchTinkerVersion + " 安卓补丁包时的版本:" + pVersion;
        }
    }

    private void checkPatch2(Application base) {
        boolean isResEnable = (boolean) OHKVUtil.getInstance(PatchUtil.SP_PatchUtil).get(base, SP_KEY_resEnable2, false);
        String pVersion = (String) OHKVUtil.getInstance(SP_PatchUtil).get(base, SP_KEY_P2Version, "");
        String installInfo2 = (String) OHKVUtil.getInstance(SP_PatchUtil).get(base, SP_KEY_installInfo2, "");
        boolean isUseV2Patch = (boolean) OHKVUtil.getInstance(SP_PatchUtil).get(base, SP_KEY_isV2PatchP2, false);
        patchInfo.patchTinkerVersionForInstall = pVersion;
        patchInfo.installInfo = installInfo2;
        patchInfo.isV2Patch=isUseV2Patch;
        if (pVersion.equals(patchInfo.patchTinkerVersion)) {
            patchInfo.isUpdate = true;
            if (isResEnable) {
                patchInfo.state = PatchInfo.State.LoadCodeAndRes;
            } else {
                patchInfo.state = PatchInfo.State.LoadCodeNoRes;
            }
            initPatch2(base, isResEnable,isUseV2Patch);
        } else {
            patchInfo.isUpdate = false;
            patchInfo.state = PatchInfo.State.PatchVersionError;
            patchInfo.msg = "PatchTinker_Version版本发生变化不热更 当前包版本:" + patchInfo.patchTinkerVersion + " 安卓补丁包时的版本:" + pVersion;
        }
    }

    /**
     * android 14以后 动态加载apk需要设置成只读
     *
     * @param file
     */
    private void setPatchReadOnly(File file) {
        if (Build.VERSION.SDK_INT >= 34) {
            if (file.exists()) {
                file.setReadOnly();
            }
        }
    }

    private void setPatchWriteAble(File file) {
        if (Build.VERSION.SDK_INT >= 34) {
            if (file.exists()) {
                file.setWritable(true);
            }
        }
    }


    private void initPatch(Application base, boolean resEnable,boolean isV2Patch) {
        String dex_apk = base.getFilesDir().getAbsolutePath() + dexPath;
        Log.d(TAG, "initPatch: dex_pak=" + dex_apk);
        File f = new File(dex_apk);
        setPatchReadOnly(f);//设置只读
        String root = base.getFilesDir().getAbsolutePath() + rootPath;//lib和cache目录必须在/data/data/包名 的目录下！

        if (f.exists()) {
            patch = new Patch();
            TinkerPatchUtil.loadDexPatch(base, dex_apk, root, ProtectModeUtil.isProtect(base));  //dex热更
            libUpdate(base, root);

            if (resEnable) {
                ResPatch.getResPatch(base, base.getFilesDir().getAbsolutePath() + dexPath,isV2Patch);  //资源热更新
            }
        }
    }

    private void initPatch2(Application base, boolean resEnable,boolean isV2Patch) {
        String dex_apk = base.getFilesDir().getAbsolutePath() + dexPath2;
        Log.d(TAG, "initPatch: dex_pak=" + dex_apk);
        File f = new File(dex_apk);
        setPatchReadOnly(f);//设置只读
        String root = base.getFilesDir().getAbsolutePath() + rootPath2;//lib和cache目录必须在/data/data/包名 的目录下！
        if (f.exists()) {
            patch = new Patch();
            TinkerPatchUtil.loadDexPatch(base, dex_apk, root, ProtectModeUtil.isProtect(base)); //dex代码热更新
            libUpdate(base, root);//lib热更
            if (resEnable) {
                ResPatch.getResPatch(base, base.getFilesDir().getAbsolutePath() + dexPath2,isV2Patch);  //资源热更新
            }
        }
    }

    private void libUpdate(Application base, String root) {
        String[] supportedAbis = Build.SUPPORTED_ABIS;

        String soPath = null;
        File file1 = new File(base.getApplicationInfo().nativeLibraryDir);
        String name = file1.getName();
        switch (name) {
            case "arm64":
                if (contains(supportedAbis, "arm64-v8a")) {
                    File file = new File(root + File.separator + "lib/arm64-v8a");
                    if (file.exists()) {
                        soPath = root + File.separator + "lib/arm64-v8a";
                    }
                }
                break;
            case "arm":
                if (contains(supportedAbis, "armeabi-v7a")) {
                    File file = new File(root + File.separator + "lib/armeabi-v7a");
                    if (file.exists()) {
                        soPath = root + File.separator + "lib/armeabi-v7a";
                    }
                }
                if (soPath == null) {
                    if (contains(supportedAbis, "armeabi")) {
                        File file = new File(root + File.separator + "lib/armeabi");
                        if (file.exists()) {
                            soPath = root + File.separator + "lib/armeabi";
                        }
                    }
                }
                break;
            case "x86":
                if (contains(supportedAbis, "x86")) {
                    File file = new File(root + File.separator + "lib/x86");
                    if (file.exists()) {
                        soPath = root + File.separator + "lib/x86";
                    }
                }
                break;
        }
        if (soPath == null) {
            for (String supportedAbi : supportedAbis) {
                File file = new File(root + File.separator + "lib/" + supportedAbi);
                if (file.exists()) {
                    soPath = root + File.separator + "lib/" + supportedAbi;
                    break;
                }
            }
        }
        if (soPath != null) {
            Log.d(TAG, "init: soPath=" + soPath);
            patch.fn_patch_lib(base, soPath);  //so库热更新
        }
    }

    private boolean contains(Object[] objects, Object o) {
        for (Object object : objects) {
            if (o.equals(object)) {
                return true;
            }
        }
        return false;
    }



    /**
     * @param context
     * @param path        补丁包路径
     * @param resIsUpdate 资源热更新是否可用
     * @throws IOException
     */
    public void loadPatchApk(Context context, String path, boolean resIsUpdate,boolean isV2Patch,String installInfo) {
        long l = System.currentTimeMillis();
        String metaData = AndroidXmlUtil.getMetaData(context, Meta_KEY_version);
        if (TextUtils.isEmpty(metaData)) {
            throw new RuntimeException("需要设置一个name为PatchTinker_Version的<meta-data>数据  用于基准包版本判断");
        }
        if (installInfo== null){
            installInfo="";
        }
        File file1 = new File(path);
        if (!file1.exists()) {
            throw new RuntimeException("文件不存在:"+path);
        }
        boolean isLoader = (boolean) OHKVUtil.getInstance(SP_PatchUtil).get(context, SP_KEY_isLoader, false);
        if (isLoader) {
            boolean usePath2 = (boolean) OHKVUtil.getInstance(SP_PatchUtil).get(context, SP_KEY_LoaderP2, false);
            if (usePath2) { //如果现在加载的是补丁包在path2  新包就更新到path1
                deletePatch(context);
                updatePatch(context, path, resIsUpdate, context.getFilesDir().getAbsolutePath() + rootPath,isV2Patch);
                usePatch1(context, metaData, resIsUpdate,isV2Patch,installInfo);
                deletePatch2(context);
            } else {
                deletePatch2(context);
                updatePatch(context, path, resIsUpdate, context.getFilesDir().getAbsolutePath() + rootPath2,isV2Patch);
                usePatch2(context, metaData, resIsUpdate,isV2Patch,installInfo);
                deletePatch(context);
            }
        } else {
            unInstallPatchApk(context);
            updatePatch(context, path, resIsUpdate, context.getFilesDir().getAbsolutePath() + rootPath,isV2Patch);
            usePatch1(context, metaData, resIsUpdate,isV2Patch,installInfo);
        }

        Log.d(TAG, "loadPatchApk: 加载补丁耗时:" + (System.currentTimeMillis() - l) + "ms");
    }


    private void usePatch1(Context context, String version, boolean resEnable,boolean isV2Patch,String installInfo) {
        OHKVUtil.getInstance(SP_PatchUtil).put(context, SP_KEY_isLoader, true);
        OHKVUtil.getInstance(SP_PatchUtil).put(context, SP_KEY_LoaderP2, false);
        OHKVUtil.getInstance(SP_PatchUtil).put(context, SP_KEY_version, version);
        OHKVUtil.getInstance(SP_PatchUtil).put(context, SP_KEY_isV2Patch, isV2Patch);
        OHKVUtil.getInstance(SP_PatchUtil).put(context, SP_KEY_installInfo, installInfo);
        OHKVUtil.getInstance(PatchUtil.SP_PatchUtil).put(context, SP_KEY_resEnable, resEnable);
    }

    private void usePatch2(Context context, String version, boolean resEnable,boolean isV2Patch,String installInfo) {
        OHKVUtil.getInstance(SP_PatchUtil).put(context, SP_KEY_isLoader, true);
        OHKVUtil.getInstance(SP_PatchUtil).put(context, SP_KEY_LoaderP2, true);
        OHKVUtil.getInstance(SP_PatchUtil).put(context, SP_KEY_P2Version, version);
        OHKVUtil.getInstance(SP_PatchUtil).put(context, SP_KEY_isV2PatchP2, isV2Patch);
        OHKVUtil.getInstance(SP_PatchUtil).put(context, SP_KEY_installInfo2, installInfo);
        OHKVUtil.getInstance(PatchUtil.SP_PatchUtil).put(context, SP_KEY_resEnable2, resEnable);
    }


    private File deletePatch(Context context) {
        File oldRoot = new File(context.getFilesDir().getAbsolutePath() + rootPath);
        OHKVUtil.getInstance(SP_PatchUtil).put(context, SP_KEY_version, "");
        if (oldRoot.exists()) {
            setPatchWriteAble(new File(context.getFilesDir().getAbsolutePath() + dexPath2));
            FileUtils.delete(oldRoot);
        }
        return oldRoot;
    }

    private File deletePatch2(Context context) {
        File oldRoot = new File(context.getFilesDir().getAbsolutePath() + rootPath2);
        OHKVUtil.getInstance(SP_PatchUtil).put(context, SP_KEY_P2Version, "");
        if (oldRoot.exists()) {
            setPatchWriteAble(new File(context.getFilesDir().getAbsolutePath() + dexPath2));
            FileUtils.delete(oldRoot);
        }
        return oldRoot;
    }

    /**
     * 卸载补丁包
     *
     * @param context
     */
    public void unInstallPatchApk(Context context) {
        OHKVUtil.getInstance(SP_PatchUtil).put(context, SP_KEY_isLoader, false);
        OHKVUtil.getInstance(SP_PatchUtil).put(context, SP_KEY_LoaderP2, false);
        OHKVUtil.getInstance(SP_PatchUtil).put(context, SP_KEY_version, "");
        OHKVUtil.getInstance(SP_PatchUtil).put(context, SP_KEY_P2Version, "");
        deletePatch(context);
        deletePatch2(context);

    }

    private void updatePatch(Context context, String path, boolean resIsUpdate, String absoluteRootPath,boolean isV2Patch)  {
        String outApkPath = absoluteRootPath + "/dex.apk";
        File outApkFile = new File(outApkPath);
        if (outApkFile.exists()) {
            FileUtils.delete(outApkFile);
        }
        if (outApkFile.getParentFile() != null) {
            outApkFile.getParentFile().mkdirs();
        }
        deleteTempFile(context); //删除临时文件
        if (resIsUpdate) { //合成包含 dex和res 的补丁包
            ResApk.toDexResApk(context, path, outApkFile.getAbsolutePath(), context.getFilesDir().getAbsolutePath() + temp,ProtectModeUtil.isProtect(context),isV2Patch);
        } else { //合成只包含 dex的补丁包
            DexApk.toDexApk(context, path, outApkFile.getAbsolutePath(), context.getFilesDir().getAbsolutePath() + temp,ProtectModeUtil.isProtect(context),isV2Patch);
        }

        String libPath = absoluteRootPath + File.separator + "lib/";
        File file = new File(libPath);
        if (file.exists()) {
            FileUtils.delete(file);
            file.mkdirs();
        }
        copyApkLib(path, libPath); //复制lib的so库
        deleteTempFile(context); //删除临时文件
    }


    private void deleteTempFile(Context context) {
        File tempFile = new File(context.getFilesDir().getAbsolutePath() + PatchUtil.temp);
        if (tempFile.exists()) {
            FileUtils.delete(tempFile);
        }
    }


    /**
     * 复制so库
     *
     * @param apkPath
     * @param outPath
     */
    private void copyApkLib(String apkPath, String outPath) {
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

                if (zipEntryFileName.startsWith("lib/") && !zipEntry.isDirectory()) {
                    Log.d(TAG, "copyApkLib: " + zipEntryFileName);
                    String replace = zipEntryFileName.replace("lib/", outPath);
                    // 创建解压缩目录
                    File targetDir = new File(replace);
                    if (targetDir.getParentFile() != null) {
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
