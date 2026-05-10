@echo off
set "ANT_HOME=C:\Program Files\apache-ant-1.9.16"
set "JAVA_HOME=C:\Program Files\Java\jdk-1.8"

set "Path=%JAVA_HOME%\bin;%ANT_HOME%\bin;%Path%"

call "%ANT_HOME%\bin\ant.bat" clean jar

pause