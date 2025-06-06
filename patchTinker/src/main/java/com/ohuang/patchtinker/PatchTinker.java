package com.ohuang.patchtinker;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;

import com.ohuang.patchtinker.util.AndroidXmlUtil;

import java.io.IOException;

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


    public PatchInfo getPatchInfo() {
        return PatchUtil.getInstance().getPatchInfo();
    }

    /***
     *
     * @param context
     * @param patchFilePath
     * @param isV2Patch  是否是V2版本的补丁
     */
    public void installPatch(Context context, String patchFilePath,boolean isV2Patch) {
        installPatch(context, patchFilePath, true, isV2Patch);
    }

    public void installPatch(Context context, String patchFilePath, boolean isUpdateRes,boolean isV2Patch) {
        String metaData = AndroidXmlUtil.getMetaData(context, Meta_KEY_version);
        if (TextUtils.isEmpty(metaData)) {
            throw new RuntimeException("需要设置一个name为PatchTinker_Version的<meta-data>数据  用于基准包版本判断");
        }
        try {
            PatchUtil.getInstance().loadPatchApk(context, patchFilePath, isUpdateRes,isV2Patch);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void uninstallPatch(Context context){
        PatchUtil.getInstance().unInstallPatchApk(context);
    }

    public String getPatchTinkerVersion(Context context){
        String metaData = AndroidXmlUtil.getMetaData(context, Meta_KEY_version);
        if (TextUtils.isEmpty(metaData)) {
            throw new RuntimeException("需要设置一个name为PatchTinker_Version的<meta-data>数据  用于基准包版本判断");
        }
        return metaData;
    }


}
