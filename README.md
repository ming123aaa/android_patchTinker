### 注意
暂不支持代码和资源的混淆
1.需要在 gradle.properties添加 android.enableResourceOptimizations=false 避免资源优化导致异常
2.AndroidManifest.xml 无法热更。
3.热更新框架本身无法被热更。
4.热更后需要重启应用才能生效。

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
    implementation 'com.github.ming123aaa:android_patchTinker:v1.0.8' //请使用最新
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

通过以下代码获取补丁包信息
```
PatchTinker.getInstance().getPatchInfo()
```

### 初始化



提供了3种初始化的方式

方式1:

```xml

<application android:name="com.ohuang.patchtinker.PatchApplication">
    <meta-data android:name="Application_Name" android:value="com.ohuang.hotupdate.TestApp" />
    <meta-data android:name="PatchTinker_WhiteProcess" android:value=":phoenix" />
</application>
```

将application的name设置为com.ohuang.patchtinker.PatchApplication
<meta-data android:name="Application_Name"》设置为自己的application
PatchApplication初始化热更后会自动替换成自己application




方式2:
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

```java
public class App extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        PatchUtil.getInstance().init(this);
    }
}
```

调用PatchUtil.getInstance().init(base);方法之前加载的类无法热更新



```java
public class App extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        if (ProcessCheck.check(base)) {
            PatchUtil.getInstance().init(this);
        }
    }
}
```

### 加载补丁包:(完成后需要重启才能生效)

PatchTinker.getInstance().loadPatchApk(StartActivity.this, patch_path);

### 进程白名单

进程白名单：
<meta-data
android:name="PatchTinker_WhiteProcess"/> 1.0.5以后的版本才生效 进程白名单,白名单的进程不会自动执行热更 (多个进程用","隔开 以":"代表子进程 )

```xml

<application >
    <meta-data android:name="PatchTinker_WhiteProcess" android:value=":phoenix" />
</application>
```

### 补丁包生成

最新方式:
运行[生成补丁包.bat](tool/生成补丁包.bat)

~~之前的打包方式:
res、assets、lib等差分包生成:新老apk解压后 运行[生成差分包文件.bat](tool/生成差分文件.bat)
dex差分包:将新老apk的dex转smali后、运行[生成差分包文件.bat](tool/生成差分文件.bat) 在重新打成dex
最后将dex和资源压缩成zip格式~~

### 关于混淆

混淆配置
```
-keep class com.ohuang.patchtinker.**{*;}
```

每次打完包记得保存 mapping.txt 文件用于下次打补丁包配置

配置mapping.txt 仅打补丁包的时候配置
在proguard-rules.pro文件上添加以下配置
```
#改成你的mapping.txt路径
-applymapping "D:\Users\ali213\AndroidStudioProjects\MyApplication2\app\mapping.txt" 

```
