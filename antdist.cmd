@echo off
set ANT_HOME=C:\Program Files\apache-ant-1.9.16
set JAVA_HOME=C:\Program Files\Java\jdk-22

set Path=%Path%;%ANT_HOME%\bin
cmd /c ant dist
pause