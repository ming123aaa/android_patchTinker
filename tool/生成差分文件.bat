chcp 65001
set /p var=旧包路径----

set /p var2=新包路径---
set /p var3=输出路径---


java -Dfile.encoding=utf-8 -jar  "%cd%\jar\apkPactch.jar" -d '%var%' '%var2%' '%var3%'

pause