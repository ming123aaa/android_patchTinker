chcp 65001
set /p var=请输入基准包apk路径----

set /p var1=请输入新包apk路径----

set /p var2=请输入输出apk或文件夹的路径----

java -Dfile.encoding=utf-8 -jar  "%cd%\jar\gameSdkTool.jar" -libs "%cd%\libs" -baseApk %var% -newApk %var1% -out %var2% -buildPatch  -patchType v2

pause
