chcp 65001
set /p var=旧包路径----

set /p var2=新包路径---
set /p var3=输出路径---


java -Dfile.encoding=utf-8 -jar  "%cd%\jar\apkPactch.jar" -e "" "%~dp0jar\baksmali\baksmali-2.4.0.jar" "%~dp0jar\baksmali\smali-2.4.0.jar" "%var%" "%var2%" "%var3%"

pause