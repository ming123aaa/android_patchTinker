chcp 65001
set /p var=补丁包配置json----

set /p var2=smali环境---

set /p var3=输出路径---
java -Dfile.encoding=utf-8 -jar  "%cd%\jar\apkPactch.jar" -a '%var%' '%var2%' '%var3%'

pause