chcp 65001
set /p var=补丁包配置json----

set /p var2=smali环境旧包---
set /p var3=smali环境新包---

set /p var4=输出路径---
java -Dfile.encoding=utf-8 -jar  "%cd%\jar\apkPactch.jar" -c '%var%' '%var2%' '%var3%' '%var4%'

pause