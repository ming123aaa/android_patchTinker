package com.ohuang.patchtinker;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;

import com.ohuang.patchtinker.util.AndroidXmlUtil;

public class PatchTinker {
    static final String Meta_KEY_version = "PatchTinker_Version";


    private PatchTinker() {
    }

    private static final class InstanceHolder {
        static final PatchTinker instance = new PatchTinker();
    }

    public static PatchTinker getInstance() {
        return PatchTinker.InstanceHolder.instance;
    }

    /**
     * 初始化补丁
     * 使用patchTinker的application不需要手动调用这个方法
     * 初始化补丁  最好在application中attachBaseContext调用
     *
     * @param context
     */
    @Deprecated
    public void initPatch(Application context) {
        PatchUtil.getInstance().init(context);
    }

    /**
     * 通过 installPatch 传的
     * 补丁安装信息
     */
    public String getInstallInfo() {
        return getPatchInfo().installInfo;
    }

    /**
     * 获取补丁加载结果
     */
    public boolean isLoadPatchSuccess() {
        return PatchUtil.getInstance().getPatchInfo().isLoadPatchSuccess;
    }



    /**
     * 获取补丁加载结果
     * @return
     */
    public PatchInfo getPatchInfo() {
        return PatchUtil.getInstance().getPatchInfo();
    }

    /***
     *
     * @param context
     * @param patchFilePath 补丁包路径
     * @param isV2Patch  是否是V2版本的补丁
     */
    public boolean installPatch(Context context, String patchFilePath,
                                boolean isV2Patch) throws Exception {
        return installPatch(context, patchFilePath, true, isV2Patch, "", true);
    }

    /**
     * @param context
     * @param patchFilePath 补丁包路径
     * @param isV2Patch     是否是V2版本的补丁
     * @param installInfo   补丁包信息, 用于记录补丁包的信息  补丁加载完成后可通过getPatchInfo().installInfo获取
     */
    public boolean installPatch(Context context, String patchFilePath,
                                boolean isV2Patch, String installInfo) throws Exception {
        return installPatch(context, patchFilePath, true, isV2Patch, installInfo, true);
    }


    /**
     * @param context
     * @param patchFilePath   补丁包路径
     * @param isUpdateRes     资源是否热更
     * @param isV2Patch       是否是V2版本的补丁
     * @param installInfo     补丁包信息, 用于记录补丁包的信息 安装补丁完成重启app后可通过getInstallInfo()获取
     * @param clearUnUsePatch 删除未被使用的补丁 (补丁加载完成后,自动删除之前的补丁,会增加本次耗时)
     *                        return true  安装成功
     */
    public synchronized boolean installPatch(Context context, String patchFilePath,
                                             boolean isUpdateRes, boolean isV2Patch,
                                             String installInfo, boolean clearUnUsePatch) throws Exception {
        String metaData = AndroidXmlUtil.getMetaData(context, Meta_KEY_version);
        if (TextUtils.isEmpty(metaData)) {
            throw new Exception("需要设置一个name为PatchTinker_Version的<meta-data>数据  用于基准包版本判断");
        }
        if (installInfo == null) {
            installInfo = "";
        }
        return PatchUtil.getInstance().installPatchApk(context, patchFilePath, isUpdateRes, isV2Patch, installInfo, clearUnUsePatch);

    }

    /**
     * 删除未被使用的补丁
     *
     * @param context
     */
    public synchronized void clearUnUsePatch(Context context) {
        PatchUtil.getInstance().clearUnUsePatch(context);
    }

    /**
     * 卸载补丁
     *
     * @param context
     */
    public synchronized void uninstallPatch(Context context) {
        PatchUtil.getInstance().unInstallPatchApk(context);
    }

    public String getPatchTinkerVersion(Context context) {
        String metaData = AndroidXmlUtil.getMetaData(context, Meta_KEY_version);
        if (TextUtils.isEmpty(metaData)) {
            throw new RuntimeException("需要设置一个name为PatchTinker_Version的<meta-data>数据  用于基准包版本判断");
        }
        return metaData;
    }


}
