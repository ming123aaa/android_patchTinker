package com.ohuang.patchtinker.util;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;

import com.ohuang.patchtinker.tinker.ShareReflectUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import me.weishu.reflection.Reflection;

/**
 * 资源补丁 id 替换管理器（V2 方案）。
 *
 * <p>已知局限（受 Resources 影响但未覆盖）：
 * <ul>
 *   <li>1. {@code obtainStyledAttributes(int resid, int[])}：compileSdk 34 已不可重写该签名，补丁改 R.style.* 时该 style 不替换（场景少）。</li>
 *   <li>2. {@code obtainAttributes} / {@code obtainStyledAttributes(AttributeSet,int[],int,int)}：未重写，defStyleAttr/defStyleRes id 不替换，TypedArray 内部经 mResources 兜底。</li>
 *   <li>3. {@code Theme.obtainStyledAttributes(...)}：属 Resources.Theme 方法，无法在 ResourcesRouter 重写，Activity 主题(windowBackground 等)解析不经 replaceId。经评估 hook 需替换 ResourcesImpl/ThemeImpl（内部类、方法不可重写、newTheme 返回 ThemeImpl 无法替换），成本极高/不可行。</li>
 *   <li>4. Manifest 的 android:theme：attach 时从 PackageManager 读 theme id 走 Theme.obtainStyledAttributes，不经 replaceId。同 3，不可行。</li>
 *   <li>5. RemoteViews / Notification / AppWidget：跨进程，系统进程无 sRouter 且无补丁 AssetManager，补丁资源完全无效（V1 同样）。</li>
 *   <li>6. WebView：自带 chromium Resources，补丁资源无效。</li>
 *   <li>7. AssetManager.open/openFd/openNonAsset：按 name 不按 id，同名 assets 覆盖 OK，改名取不到（非 id 替换范畴）。</li>
 *   <li>8. Resources.getSystem()：系统 Resources，不含应用资源，本就不应替换。</li>
 *   <li>9. PackageManager.getResourcesForApplication：其他包 Resources，非本应用补丁范畴。</li>
 * </ul>
 */
public class PatchIdManager {

    public static String TAG = "PatchIdManager";
    private static Map<Integer, Integer> sIdMap = new LinkedHashMap<>();

    private static boolean sPatchLoaded = false;
    public static String mapFileName = "patch_id_map.txt";

    // 已安装的 ResourcesRouter 实例，insurance hack 重注入时复用，避免重复 new
    private static ResourcesRouter sRouter = null;

    // Activity 生命周期回调，保证每个新 Activity 创建后其 ContextImpl.mResources 也被替换为 sRouter
    private static Application.ActivityLifecycleCallbacks sActivityLifecycleCallback;

    public static boolean isPatchLoaded() {
        return sPatchLoaded;
    }

    public static Map<Integer, Integer> getPatchIdMap() {
        return sIdMap;
    }

    /**
     * 重置补丁状态（卸载补丁时调用）。
     * 清空 id 映射表，已安装的 ResourcesRouter 将不再做 id 转换；
     * insurance hack / Activity 回调仍保留，但 sRouter 置空后它们不会再注入。
     */
    public static void reset() {
        sIdMap.clear();
        sPatchLoaded = false;
        sRouter = null;
        Log.i(TAG, "补丁状态已重置");
    }

    public static boolean loadResPatch(Context context, String resPatch) throws Throwable {
        // 1. 把补丁资源路径加到应用 AssetManager，原 Resources 与 ResourcesRouter 共享该 AssetManager，
        //    这样 overlay id 对应的补丁资源才能被取到。
        addAssetPath(getSystemAssetManager(), resPatch);
        addAssetPath(context.getAssets(), resPatch);

        // 2. 加载 origin -> overlay 的 id 映射表
        loadPatchMapping(resPatch, mapFileName);
        if (!sPatchLoaded) {
            Log.e(TAG, "资源无需映射");
            return false;
        }
        try {
            Reflection.unseal(context);//反射隐藏api
        } catch (Throwable e) {
            e.printStackTrace();
        }

        // 3. 安装 ResourcesRouter，接管所有经过 Context.getResources() 的资源访问（核心：id 替换生效）
        installResourcesRouter(context);

        // 4. 替换 LayoutInflater，接管布局 inflate 时的资源 id
        replaceLayoutInflater(context);
        return sPatchLoaded;
    }


    static AssetManager getSystemAssetManager() throws Throwable {
        Method addAssetPath = AssetManager.class.getDeclaredMethod("getSystem");
        addAssetPath.setAccessible(true);
        return (AssetManager) addAssetPath.invoke(null);
    }


    static void addAssetPath(AssetManager assetManager, String path) throws Throwable {
        Method addAssetPath = assetManager.getClass().getDeclaredMethod("addAssetPath", String.class);
        addAssetPath.setAccessible(true);
        addAssetPath.invoke(assetManager, path);
    }


    /**
     * 核心方法：ID转换
     */
    public static int getPatchedId(int oldId) {

        if (!sPatchLoaded) {
            return oldId;
        }
        Integer newId = sIdMap.get(oldId);
        return newId != null ? newId : oldId;
    }


    /**
     * 加载补丁资源映射表
     *
     * @param mappingFilePath 映射表文件路径（JSON格式）
     */
    public static void loadPatchMapping(String resApk, String mappingFilePath) {
        try {

            String json = readFile(resApk, mappingFilePath);
            JSONObject obj = new JSONObject(json);
            JSONArray ids = obj.getJSONArray("map");

            for (int i = 0; i < ids.length(); i++) {
                try {
                    JSONObject item = ids.getJSONObject(i);
                    int oldId = parseResId(item.getString("origin"));
                    int newId = parseResId(item.getString("overlay"));
                    sIdMap.put(oldId, newId);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            sPatchLoaded = true;
            Log.i(TAG, "加载映射表成功，共 " + sIdMap.size() + " 条映射");

        } catch (Exception e) {
            Log.e(TAG, "加载映射表失败", e);
        }
    }


    private static String readFile(String apkPath, String fileName) throws IOException {
        File file = new File(apkPath);
        if (file.exists() && file.isFile()) {
            return ZipUtil.readZipEntry(apkPath, fileName);
        } else {
            return FileUtils.readText(new File(apkPath, fileName).getAbsolutePath());
        }

    }

    /**
     * 解析资源 id 字符串，兼容 0x 开头的十六进制(如 "0x7f020001")与十进制(如 "2131033089")。
     */
    private static int parseResId(String s) throws NumberFormatException {
        if (s == null) {
            throw new NumberFormatException("resource id is null");
        }
        String t = s.trim();
        if (t.startsWith("0x") || t.startsWith("0X")) {
            // 用 long 解析再转 int，避免 0x80......(>=0x80000000) 超出 int 正数上限抛 NumberFormatException
            return (int) Long.parseLong(t.substring(2), 16);
        }
        return Integer.parseInt(t);
    }

    /**
     * 创建 ResourcesRouter 并安装到 Context / LoadedApk / 已存在的 Activity、Service 上，
     * 使所有经过 Context.getResources() 的访问都走 id 替换。router 实例缓存复用。
     */
    private static void installResourcesRouter(Context context) {
        try {
            if (sRouter == null) {
                Resources orig = context.getResources();
                sRouter = new ResourcesRouter(
                        context.getAssets(),
                        orig.getDisplayMetrics(),
                        orig.getConfiguration(),
                        sIdMap);
            }

            // 替换 Application / Activity 的 ContextImpl.mResources
            replaceContextImplResources(context, sRouter);

            // 替换 ActivityThread 中所有 LoadedApk.mResources（影响后续新建的 Context）
            Object activityThread = ShareReflectUtil.getActivityThread(context, null);
            if (activityThread != null) {
                replaceLoadedApkResources(activityThread, sRouter);
                replaceExistingActivitiesResources(activityThread, sRouter);
                replaceExistingServicesResources(activityThread, sRouter);
                replaceExistingProvidersResources(activityThread, sRouter);
            }

            // 安装 insurance hack，保证后续 Activity 启动/重启、Service 创建（系统可能重新生成
            // Resources 覆盖 ContextImpl.mResources）时，重新注入 sRouter，避免 id 替换丢失
            installResourceInsuranceHack(context);

            // 注册 Activity 生命周期回调，保证每个新 Activity 创建后其 ContextImpl.mResources、
            // mInflater 也被替换（LoadedApk.mResources 的替换在 API24+ 对新 Activity 可能无效）
            installActivityLifecycleHack(context);

            Log.i(TAG, "ResourcesRouter 安装成功");

        } catch (Throwable e) {
            Log.e(TAG, "ResourcesRouter 安装失败", e);
        }
    }

    /**
     * 用已缓存的 sRouter 重新替换 Context / LoadedApk / 已存在 Activity、Service 的 Resources。
     * 供 insurance hack 在 LAUNCH_ACTIVITY / RELAUNCH_ACTIVITY / CREATE_SERVICE 时调用。
     */
    private static void reInjectRouter(Context context) {
        if (sRouter == null) {
            // 补丁未加载或已 reset，无需重注入
            return;
        }
        replaceContextImplResources(context, sRouter);
        Object activityThread = ShareReflectUtil.getActivityThread(context, null);
        if (activityThread != null) {
            replaceLoadedApkResources(activityThread, sRouter);
            replaceExistingActivitiesResources(activityThread, sRouter);
            replaceExistingServicesResources(activityThread, sRouter);
            replaceExistingProvidersResources(activityThread, sRouter);
        }
    }

    /**
     * 替换 ContextWrapper( Application / Activity / Service ) 内部 ContextImpl 的 mResources 字段，
     * 并清掉 context(Activity 等 ContextThemeWrapper 子类)自身缓存的 mResources，让它重新从 ContextImpl 取 sRouter。
     */
    private static void replaceContextImplResources(Context context, Resources router) {
        if (router == null || !(context instanceof ContextWrapper)) {
            return;
        }
        try {
            Context contextImpl = ((ContextWrapper) context).getBaseContext();
            if (contextImpl != null) {
                Field mResourcesField = ShareReflectUtil.findField(contextImpl, "mResources");
                mResourcesField.set(contextImpl, router);
            }
        } catch (Throwable ignore) {
            // 某些 ROM/版本字段名不同，忽略
        }
        // 清掉 context(Activity 等 ContextThemeWrapper 子类)自身缓存的 mResources，
        // 让它重新从 ContextImpl 取 sRouter；非 ContextThemeWrapper 的 Context(如 Application/Service)无此字段，忽略
        try {
            Field cacheField = ShareReflectUtil.findField(context, "mResources");
            cacheField.set(context, router);
        } catch (Throwable ignore) {
            // 无缓存字段，忽略
        }
    }

    /**
     * 遍历 ActivityThread.mPackages( / mResourcePackages )，替换每个 LoadedApk.mResources。
     */
    private static void replaceLoadedApkResources(Object activityThread, Resources router) {
        String[] packageFieldNames = (Build.VERSION.SDK_INT < 27)
                ? new String[]{"mPackages", "mResourcePackages"}
                : new String[]{"mPackages"};

        for (String fieldName : packageFieldNames) {
            try {
                Field field = ShareReflectUtil.findField(activityThread.getClass(), fieldName);
                Object value = field.get(activityThread);
                if (!(value instanceof Map)) {
                    continue;
                }
                for (Object entryObj : ((Map<?, ?>) value).entrySet()) {
                    Map.Entry<?, ?> entry = (Map.Entry<?, ?>) entryObj;
                    Object ref = entry.getValue();
                    if (!(ref instanceof WeakReference)) {
                        continue;
                    }
                    Object loadedApk = ((WeakReference<?>) ref).get();
                    if (loadedApk == null) {
                        continue;
                    }
                    try {
                        Field mResourcesField = ShareReflectUtil.findField(loadedApk, "mResources");
                        mResourcesField.set(loadedApk, router);
                    } catch (Throwable ignore) {
                        // 忽略个别 LoadedApk 的差异
                    }
                }
            } catch (Throwable ignore) {
                // 忽略
            }
        }
    }

    /**
     * 遍历 ActivityThread.mActivities，替换已存在 Activity 的 ContextImpl.mResources。
     */
    private static void replaceExistingActivitiesResources(Object activityThread, Resources router) {
        try {
            Field mActivitiesField = ShareReflectUtil.findField(activityThread.getClass(), "mActivities");
            Object activities = mActivitiesField.get(activityThread);
            if (!(activities instanceof Map)) {
                return;
            }
            for (Object rec : ((Map<?, ?>) activities).values()) {
                if (rec == null) {
                    continue;
                }
                try {
                    Field activityField = ShareReflectUtil.findField(rec, "activity");
                    Object activity = activityField.get(rec);
                    if (activity instanceof ContextWrapper) {
                        replaceContextImplResources((ContextWrapper) activity, router);
                    }
                } catch (Throwable ignore) {
                    // 忽略单条异常
                }
            }
        } catch (Throwable ignore) {
            // 忽略
        }
    }

    /**
     * 遍历 ActivityThread.mServices，替换已存在 Service 的 ContextImpl.mResources。
     */
    private static void replaceExistingServicesResources(Object activityThread, Resources router) {
        try {
            Field mServicesField = ShareReflectUtil.findField(activityThread.getClass(), "mServices");
            Object services = mServicesField.get(activityThread);
            if (!(services instanceof Map)) {
                return;
            }
            for (Object service : ((Map<?, ?>) services).values()) {
                if (service instanceof ContextWrapper) {
                    replaceContextImplResources((ContextWrapper) service, router);
                }
            }
        } catch (Throwable ignore) {
            // 忽略
        }
    }

    /**
     * 遍历 ActivityThread.mLocalProviders( / mProviderMap )，替换已存在 ContentProvider 的
     * ContextImpl.mResources。ContentProvider 在 Application.onCreate 前已创建且不走 mH 消息，
     * 故在此一次性替换；动态 installProvider 的场景无兜底，作为已知局限。
     */
    private static void replaceExistingProvidersResources(Object activityThread, Resources router) {
        for (String fieldName : new String[]{"mLocalProviders", "mProviderMap"}) {
            try {
                Field field = ShareReflectUtil.findField(activityThread.getClass(), fieldName);
                Object value = field.get(activityThread);
                if (!(value instanceof Map)) {
                    continue;
                }
                for (Object rec : ((Map<?, ?>) value).values()) {
                    if (rec == null) {
                        continue;
                    }
                    try {
                        Field mLocalProviderField = ShareReflectUtil.findField(rec, "mLocalProvider");
                        Object provider = mLocalProviderField.get(rec);
                        if (provider == null) {
                            continue;
                        }
                        // ContentProvider 不是 ContextWrapper，其 mContext 是 ContextImpl
                        try {
                            Field mContextField = ShareReflectUtil.findField(provider, "mContext");
                            Object contextImpl = mContextField.get(provider);
                            if (contextImpl != null) {
                                Field mResourcesField = ShareReflectUtil.findField(contextImpl, "mResources");
                                mResourcesField.set(contextImpl, router);
                            }
                        } catch (Throwable ignore) {
                            // 忽略个别 provider 差异
                        }
                    } catch (Throwable ignore) {
                        // 忽略单条异常
                    }
                }
            } catch (Throwable ignore) {
                // 忽略
            }
        }
    }

    /**
     * hook ActivityThread.mH 的 mCallback，在每次启动/重启 Activity、创建 Service 的消息到来时
     * 重新注入 sRouter，避免系统重新生成 Resources 后 id 替换失效。
     */
    private static void installResourceInsuranceHack(Context context) {
        try {
            final Object activityThread = ShareReflectUtil.getActivityThread(context, null);
            if (activityThread == null) {
                return;
            }
            Field mHField = ShareReflectUtil.findField(activityThread, "mH");
            Handler mH = (Handler) mHField.get(activityThread);
            Field mCallbackField = ShareReflectUtil.findField(Handler.class, "mCallback");
            Handler.Callback originCallback = (Handler.Callback) mCallbackField.get(mH);
            if (originCallback instanceof ResourceInsuranceHandlerCallback) {
                // 已安装，避免重复包装
                return;
            }
            ResourceInsuranceHandlerCallback hackCallback =
                    new ResourceInsuranceHandlerCallback(context, originCallback, mH);
            mCallbackField.set(mH, hackCallback);
            Log.i(TAG, "ResourceInsuranceHack 安装成功");
        } catch (Throwable e) {
            Log.e(TAG, "installResourceInsuranceHack 失败", e);
        }
    }

    /**
     * 注册 Activity 生命周期回调，在每个 Activity 创建/启动/恢复时把其 ContextImpl.mResources
     * 替换为 sRouter，并把 mInflater 包装为 PatchLayoutInflater。这是保证新 Activity 资源访问
     * 走 id 替换的关键（LoadedApk.mResources 的替换在 API24+ 对新 Activity 可能无效，
     * insurance hack 的消息时机又早于 Activity attach）。
     */
    private static void installActivityLifecycleHack(Context context) {
        try {
            Context appContext =context;
            if (!(appContext instanceof Application) || sActivityLifecycleCallback != null) {
                return;
            }
            sActivityLifecycleCallback = new Application.ActivityLifecycleCallbacks() {
                @Override
                public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                    replaceContextImplResources(activity, sRouter);
                    replaceActivityInflater(activity);
                }

                @Override
                public void onActivityStarted(Activity activity) {
                    replaceContextImplResources(activity, sRouter);
                }

                @Override
                public void onActivityResumed(Activity activity) {
                    replaceContextImplResources(activity, sRouter);
                }

                @Override
                public void onActivityPaused(Activity activity) {
                }

                @Override
                public void onActivityStopped(Activity activity) {
                }

                @Override
                public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
                }

                @Override
                public void onActivityDestroyed(Activity activity) {
                }
            };
            ((Application) appContext).registerActivityLifecycleCallbacks(sActivityLifecycleCallback);
            Log.i(TAG, "ActivityLifecycleHack 安装成功");
        } catch (Throwable e) {
            Log.e(TAG, "installActivityLifecycleHack 失败", e);
        }
    }

    /**
     * 把 Activity 的 ContextImpl.mInflater 包装为 PatchLayoutInflater，
     * 使 LayoutInflater.from(activity) 路径的 inflate(int) 走 id 替换。
     * 注意：PatchLayoutInflater 内部把 inflate 委托给原 LayoutInflater(mBase)，故 AppCompat 的
     * Factory2 仍由 mBase 持有、view 自动替换不受影响；布局文件本身的 id 替换也已由
     * ResourcesRouter.getLayout 兜底，此处为 LayoutInflater 路径再加一层覆盖。
     */
    private static void replaceActivityInflater(Activity activity) {
        if (sRouter == null) {
            return;
        }
        try {
            Field mInflaterField = ShareReflectUtil.findField(activity, "mInflater");

            Object original = mInflaterField.get(activity);
            if (original instanceof PatchLayoutInflater) {
                Log.d(TAG, "PatchLayoutInflater 已加载-" + activity);
            } else if (original instanceof LayoutInflater) {
                mInflaterField.set(activity,
                        PatchLayoutInflater.createLayoutInflater((LayoutInflater) original, sIdMap));
            }


        } catch (Throwable ignore) {
            // 某些版本字段名不同，忽略
        }

        try {
            Context contextImpl = ((ContextWrapper) activity).getBaseContext();
            if (contextImpl == null) {
                return;
            }

            Field mInflaterField = ShareReflectUtil.findField(contextImpl, "mInflater");

            Object original = mInflaterField.get(contextImpl);
            if (original instanceof PatchLayoutInflater) {
                Log.d(TAG, "PatchLayoutInflater 已加载-" + contextImpl);
            } else if (original instanceof LayoutInflater) {
                mInflaterField.set(contextImpl,
                        PatchLayoutInflater.createLayoutInflater((LayoutInflater) original, sIdMap));
            }

        } catch (Throwable ignore) {

        }


    }

    private static void replaceLayoutInflater(Context context) {
        try {
            // 获取系统 LayoutInflater
            LayoutInflater systemInflater = LayoutInflater.from(context);

            // 创建 PatchLayoutInflater
            LayoutInflater patchInflater = new PatchLayoutInflater(
                    systemInflater,
                    getPatchIdMap()  // 需要加一个 getter 方法
            );

            try {
                // 反射替换 ContextImpl 中的 mInflater
                if (context instanceof ContextWrapper) {
                    Context contextImpl =
                            ((ContextWrapper) context).getBaseContext();
                    Field mInflaterField = contextImpl.getClass().getDeclaredField("mInflater");
                    mInflaterField.setAccessible(true);
                    mInflaterField.set(contextImpl, patchInflater);
                }
            } catch (Throwable e) {

            }

            // 同时替换 Application 的 mLayoutInflater（如果有）
            try {
                Field mLayoutInflaterField = Application.class.getDeclaredField("mLayoutInflater");
                mLayoutInflaterField.setAccessible(true);
                mLayoutInflaterField.set(context, patchInflater);
            } catch (Throwable e) {
                // 某些版本可能没有这个字段，忽略
            }

            LayoutInflaterService.replaceServices(getPatchIdMap());

            Log.i("Patch", "LayoutInflater 替换完成");

        } catch (Exception e) {
            Log.e("Patch", "替换 LayoutInflater 失败", e);
        }
    }

    /**
     * ActivityThread.mH 的消息回调，拦截启动/重启 Activity、创建 Service 的消息以重新注入补丁 Resources。
     * 对应 V1 TinkerResourcePatcher.ResourceInsuranceHandlerCallback。
     */
    private static final class ResourceInsuranceHandlerCallback implements Handler.Callback {
        private static final String LAUNCH_ACTIVITY_ITEM_CLASSNAME =
                "android.app.servertransaction.LaunchActivityItem";

        private final Context mContext;
        private final Handler.Callback mOriginalCallback;
        private final Handler mH;

        private final int LAUNCH_ACTIVITY;
        private final int RELAUNCH_ACTIVITY;
        private final int EXECUTE_TRANSACTION;
        private final int CREATE_SERVICE;

        private Method mGetCallbacksMethod = null;
        private boolean mSkipInterceptExecuteTransaction = false;

        ResourceInsuranceHandlerCallback(Context context, Handler.Callback original, Handler mH) {
            Context appContext = context.getApplicationContext();
            mContext = (appContext != null ? appContext : context);
            mOriginalCallback = original;
            this.mH = mH;
            LAUNCH_ACTIVITY = fetchMessageId(mH.getClass(), "LAUNCH_ACTIVITY", 100);
            RELAUNCH_ACTIVITY = fetchMessageId(mH.getClass(), "RELAUNCH_ACTIVITY", 126);
            EXECUTE_TRANSACTION = (Build.VERSION.SDK_INT >= 28)
                    ? fetchMessageId(mH.getClass(), "EXECUTE_TRANSACTION", 159)
                    : -1;
            CREATE_SERVICE = fetchMessageId(mH.getClass(), "CREATE_SERVICE", 114);
        }

        private int fetchMessageId(Class<?> hClazz, String name, int defVal) {
            try {
                return ShareReflectUtil.findField(hClazz, name).getInt(null);
            } catch (Throwable e) {
                return defVal;
            }
        }

        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == CREATE_SERVICE && mH != null) {
                // Service 在 handleCreateService 之后才 attach 进 mServices，故 post 到当前消息之后重注入
                mH.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            reInjectRouter(mContext);
                        } catch (Throwable e) {
                            Log.e(TAG, "reInjectRouter( service ) 失败", e);
                        }
                    }
                });
            } else if (shouldReInject(msg)) {
                try {
                    reInjectRouter(mContext);
                } catch (Throwable e) {
                    Log.e(TAG, "reInjectRouter 失败", e);
                }
            }
            if (mOriginalCallback != null) {
                return mOriginalCallback.handleMessage(msg);
            }
            return false;
        }

        @SuppressWarnings("unchecked")
        private boolean shouldReInject(Message msg) {
            if (msg.what == LAUNCH_ACTIVITY || msg.what == RELAUNCH_ACTIVITY) {
                return true;
            }
            if (msg.what == EXECUTE_TRANSACTION && msg.obj != null && !mSkipInterceptExecuteTransaction) {
                try {
                    if (mGetCallbacksMethod == null) {
                        mGetCallbacksMethod = ShareReflectUtil.findMethod(msg.obj, "getCallbacks");
                    }
                    List<Object> callbacks = (List<Object>) mGetCallbacksMethod.invoke(msg.obj);
                    if (callbacks != null && !callbacks.isEmpty()) {
                        Object cb = callbacks.get(0);
                        return cb != null
                                && LAUNCH_ACTIVITY_ITEM_CLASSNAME.equals(cb.getClass().getName());
                    }
                } catch (Throwable ignore) {
                    // 找不到 getCallbacks，后续不再尝试，避免反复反射失败
                    mSkipInterceptExecuteTransaction = true;
                }
            }
            return false;
        }
    }
}
