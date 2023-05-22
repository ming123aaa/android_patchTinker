### 注意
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
	        implementation 'com.github.ming123aaa:android_patchTinker:v1.0.3'
	}
```
### 初始化
提供了3种初始化的方式

方式1:

```xml
<application
android:name="com.ohuang.patchtinker.PatchApplication">
    <meta-data android:name="Application_Name"
        android:value="com.ohuang.hotupdate.TestApp"/>
</application>
```
将application的name设置为com.ohuang.patchtinker.PatchApplication
通过配置meta-data设置为自己的application   PatchApplication初始化热更后会自动替换成自己application

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
TkApp类就无法通过热更修改

```xml
<application
android:name=".TkApp"/>
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



### 加载补丁包:(完成后需要重启才能生效)

PatchUtil.getInstance().loadPatchApk(StartActivity.this, patch_path);



### 补丁包生成
最新方式:
运行[生成补丁包.bat](tool/生成补丁包.bat)


~~之前的打包方式:
res、assets、lib等差分包生成:新老apk解压后 运行[生成差分包文件.bat](tool/生成差分文件.bat)
dex差分包:将新老apk的dex转smali后、运行[生成差分包文件.bat](tool/生成差分文件.bat) 在重新打成dex
最后将dex和资源压缩成zip格式~~
