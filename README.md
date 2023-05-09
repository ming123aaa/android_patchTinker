###问题


### lib热更新  
需要复制正确的cpu架构的so库到目录

### dex热更新

初始化
```java
public class App extends Application {
 @Override
 protected void attachBaseContext(Context base) {

  PatchUtil.getInstance().init(base);
  super.attachBaseContext(base);
 }
}
```

调用PatchUtil.getInstance().init(base);方法之前加载的类无法热更新


为了可以热更新需要配置Application
AppImpl.java实现了代理application，通过配置androidManifest.xml设置代理application
<meta-data android:name="Application_Name"
android:value="com.ohuang.hotupdate.TestApp"/>

加载补丁包:(完成后需要重启才能生效)
PatchUtil.getInstance().loadPatchApk(StartActivity.this, str_patch_apk);


### 补丁包生成
最新方式:
运行[生成补丁包.bat](tool/生成补丁包.bat)


~~之前的打包方式:
res、assets、lib等差分包生成:新老apk解压后 运行[生成差分包文件.bat](tool/生成差分文件.bat)
 dex差分包:将新老apk的dex转smali后、运行[生成差分包文件.bat](tool/生成差分文件.bat) 在重新打成dex 
 最后将dex和资源压缩成zip格式~~

