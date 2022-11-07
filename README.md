###问题
####资源热更新问题及注意点
android 9.0 启动Activity资源热更新会失败
android 9.0及以上版本只支持activity资源热更新
android 9.0以下版本补丁包修改的资源需要修改id与原包不能冲突
生成补丁包 AppCompatActivity theme相关的id与原包保持一致。
###lib热更新  
需要复制正确的cpu架构的so库到目录

### dex热更新
App和Patch这两个类无法热更新
为了可以热更新需要配置Application
<meta-data android:name="Application_Name"
android:value="com.ohuang.hotupdate.TestApp"/>
