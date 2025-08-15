推荐使用最新的V2版本的,优化了补丁的大小和补丁加载的时间
本项目在2.0.0以上的版本也支持V2版本补丁方式。
### 注意
暂不支持代码和资源的混淆
1.需要在 gradle.properties添加 android.enableResourceOptimizations=false 避免资源优化导致异常
2.AndroidManifest.xml 无法热更。
3.热更新框架本身无法被热更。
4.热更后需要重启应用才能生效。
5.v2版本的补丁无法修改已存在的assets的资源(若要修改，只需修改一下文件名)

### 引用

```groovy
allprojects {
    repositories {
        maven { url 'https://www.jitpack.io' }
    }
}
```

```groovy
    dependencies {
    implementation 'com.github.ming123aaa:android_patchTinker:2.0.5' //请使用最新
}
```

必须设置一个基准包的版本号,由于AndroidManifest.xml不会热更所以可用于检测基准包版本是否发生变化。
```xml
<application>
  <meta-data
            android:name="PatchTinker_Version"
            android:value="1" />
</application>
```

通过以下代码获取当前基准包版本。
```
  PatchTinker.getInstance().getPatchTinkerVersion(this);
```



### 初始化



提供了3种初始化的方式,选择一种即可。

方式1(接入最简单):

```xml

<application android:name="com.ohuang.patchtinker.PatchApplication">
    <meta-data android:name="Application_Name" android:value="MyApplication" />
</application>
```

将application的name设置为com.ohuang.patchtinker.PatchApplication
<meta-data android:name="Application_Name">设置为自己的application
PatchApplication初始化热更后会自动替换成自己application




方式2(推荐):
类似于Tinker的接入方式
让原来application的代码继承ApplicationLike,这里实现application

```java
public class AppImpl extends ApplicationLike {
    public App(Application application) {
        super(application);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}

```

实现TinkerApplication的getApplicationLikeClassName方法,返回AppImpl的类名。不要使用AppImpl.class.getName()这种方式获取类名

```java
public class TkApp extends TinkerApplication {
    //记得防止类名被混淆
    @Override
    public String getApplicationLikeClassName() {
        return "com.ohuang.hotupdate.AppImpl";
    }
}

```

将TkApp添加到name,TkApp类就无法通过热更修改
```xml
<application android:name=".TkApp" />
```



方式3:
手动调用补丁初始化方式


调用PatchUtil.getInstance().init(base);方法之前加载的类无法热更新

```java
public class App extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        if (ProcessCheck.check(base)) {//进程白名单检查
            PatchUtil.getInstance().init(this);
        }
    }
}
```

### 安装补丁包:(完成后需要重启才能生效)
PatchUtil.getInstance().installPatch()
```java

/***
 *
 * @param context
 * @param patchFilePath 补丁包路径
 * @param isV2Patch  是否是V2版本的补丁
 */
public boolean installPatch(Context context, String patchFilePath,
                            boolean isV2Patch);

/**
 * @param context
 * @param patchFilePath 补丁包路径
 * @param isV2Patch     是否是V2版本的补丁
 * @param installInfo   补丁包信息, 用于记录补丁包的信息  补丁加载完成后可通过getPatchInfo().installInfo获取
 */
public boolean installPatch(Context context, String patchFilePath,
                            boolean isV2Patch, String installInfo);
    
    /**
 * @param context
 * @param patchFilePath 补丁包路径
 * @param isUpdateRes   资源是否热更  
 * @param isV2Patch     是否是V2版本的补丁
 * @param installInfo   补丁包信息, 用于记录补丁包的信息 安装补丁完成重启app后可通过getInstallInfo()获取
 * @param  clearUnUsePatch  删除未被使用的补丁 (补丁加载完成后,自动删除之前的补丁,会增加本次耗时)
 * return true  安装成功
 */

public  boolean  installPatch (Context context, String patchFilePath, 
                               boolean isUpdateRes, boolean isV2Patch, 
                               String installInfo,boolean clearUnUsePatch);

```
### 补丁包信息
通过以下代码获取补丁包信息
```java
/**
 *  补丁包是否load成功
 * @return
 */
public boolean isLoadPatchSuccess();

/**
 * 通过 installPatch()的时候写的
 * 补丁安装时写入的信息
 */
public String getInstallInfo();

/**
 * 获取补丁加载结果
 * @return
 */
public PatchInfo getPatchInfo();

```


### 进程白名单

进程白名单：
<meta-data
android:name="PatchTinker_WhiteProcess"/> 1.0.5以后的版本才生效 进程白名单,白名单的进程不会自动执行热更 (多个进程用","隔开 以":"代表子进程 )

```xml

<application >
    <meta-data android:name="PatchTinker_WhiteProcess" android:value=":phoenix" />
</application>
```

### 类白名单
android sdk 24及以上版本支持
可根据startWith、equals来匹配类名,匹配到的类不进行热更,一般建议使用startWith(可以匹配到内部类)。(配置多个类用,隔开 )
记得防止类名被混淆
```xml

<application >
    <meta-data android:name="PatchTinker_WhiteClassStartWith" android:value="com.aaa.bbb,com.tt.aaa" />
    <meta-data android:name="PatchTinker_WhiteClassEquals" android:value="com.aaa.bbb,com.tt.aaa" />
</application>
```

### 保护模式
保护模式不会替换classloader,主要用于加固环境,默认false. (设置后会导致类白名单功能失效)  
主要用于加固环境下
```xml
<application >
<meta-data android:name="PatchTinker_isProtect" android:value="true" />
</application>
```


### 补丁包生成  V1方式

最新方式:
运行[生成补丁包.bat](tool/生成补丁包.bat)

如果不需要资源热更:
运行[生成补丁包(无资源).bat](tool/生成补丁包(无资源).bat)


~~之前的打包方式:
res、assets、lib等差分包生成:新老apk解压后 运行[生成差分包文件.bat](tool/生成差分文件.bat)
dex差分包:将新老apk的dex转smali后、运行[生成差分包文件.bat](tool/生成差分文件.bat) 在重新打成dex
最后将dex和资源压缩成zip格式~~


### 补丁包生成  V2方式(需要2.0.0的版本以上才支持)
使用阿里的SophixPatchTool打包,需要取消检查初始化选项，需要取消检查初始化选项，需要取消检查初始化选项。
补丁包生成需要使用打补丁工具SophixPatchTool，如还未下载打补丁工具，请前往下载Android打包工具。
打包工具下载地址如下：
[Mac版本打包工具下载](https://ams-hotfix-repo.oss-cn-shanghai.aliyuncs.com/SophixPatchTool_macos.zip?spm=a2c4g.11186623.0.0.58d32cd3lkmCPs&file=SophixPatchTool_macos.zip)
[Windows版本打包工具下载](https://ams-hotfix-repo.oss-cn-shanghai.aliyuncs.com/SophixPatchTool_windows.zip?spm=a2c4g.11186623.0.0.58d32cd3lkmCPs&file=SophixPatchTool_windows.zip)
[Linux版本打包工具地址](https://ams-hotfix-repo.oss-cn-shanghai.aliyuncs.com/SophixPatchTool_linux.zip?spm=a2c4g.11186623.0.0.58d32cd3lkmCPs&file=SophixPatchTool_linux.zip)
如果工具打包很久没有完成的话，可能是apk的问题，可以尝试把apk解压然后再压缩成zip包。

### 关于混淆



混淆配置
```
#防止被混淆
-keep class com.ohuang.patchtinker.**{*;}
#防止inline
-dontoptimize
```

每次打完包记得保存 mapping.txt 文件用于下次打补丁包配置

配置mapping.txt 仅打补丁包的时候配置
在proguard-rules.pro文件上添加以下配置
```
#改成你的mapping.txt路径
-applymapping "D:\Users\ali213\AndroidStudioProjects\MyApplication2\app\mapping.txt" 

```

使用android studio打包生成apk时，要关闭instant run。

使用gradle plugin版本高于4.2时，可能会因为自动开启资源优化导致资源名称被混淆，无法正常解析apk包。解决方案：在gradle.properties 中新增android.enableResourceOptimizations=false，重新生成基线包和修复包，然后再生成补丁。

如果开启了代码混淆，需要关闭R8，不然会导致生成的补丁较大。解决方案：在gradle.properties 中新增android.enableR8=false，重新生成基线包和修复包，然后再生成补丁。
```
android {
   buildTypes {
       release {
           // 关闭 R8.
           minifyEnabled false
       }
   }
}
```
