###问题


###lib热更新  
需要复制正确的cpu架构的so库到目录

### dex热更新

App和Patch这两个类无法热更新
为了可以热更新需要配置Application
<meta-data android:name="Application_Name"
android:value="com.ohuang.hotupdate.TestApp"/>

### 补丁包生成
res、assets、lib等差分包生成:新老apk解压后 运行[生成差分包文件.bat](tool/生成差分文件.bat)
dex差分包:将新老apk的dex转smali后、运行[生成差分包文件.bat](tool/生成差分文件.bat) 在重新打成dex
最后将dex和资源压缩成zip格式
