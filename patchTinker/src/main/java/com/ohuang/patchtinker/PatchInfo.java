package com.ohuang.patchtinker;

public class PatchInfo {

    /**
     * <meta-data
     * android:name="PatchTinker_Version"
     * android:value=""/>
     * 设置基准包的版本,若基准包版本变化，不进行热更。
     * 当前基准包的版本
     */
    public String patchTinkerVersion = "";

    /**
     * 安装补丁时的基准包版本
     */
    public String patchTinkerVersionForInstall="";

    /**
     * 是否加载了热更  true加载了热更
     */
    public boolean isUpdate = false;

    public State state = State.NoLoadPatch;

    public String msg = "";




    @Override
    public String toString() {
        return "PatchInfo{" +
                "PatchTinker_Version='" + patchTinkerVersion + '\'' +
                ", isUpdate=" + isUpdate +
                ", state=" + state +
                ", msg='" + msg + '\'' +
                '}';
    }

    public PatchInfo copy() {
        PatchInfo patchInfo = new PatchInfo();
        patchInfo.isUpdate = isUpdate;
        patchInfo.msg = msg;
        patchInfo.state = state;
        patchInfo.patchTinkerVersion = patchTinkerVersion;
        return patchInfo;
    }

    public enum State {
        /**
         * 没有可加载的补丁包,或不加载补丁包
         */
        NoLoadPatch,
        /**
         * 基准包更新导致补丁包版本与基准包版本不匹配,不加载补丁包
         */
        PatchVersionError,

        /**
         * 加载补丁dex,.so,res,assets
         */
        LoadCodeAndRes,
        /**
         * 加载补丁dex,.so 不加载res,assets
         */
        LoadCodeNoRes


    }
}
