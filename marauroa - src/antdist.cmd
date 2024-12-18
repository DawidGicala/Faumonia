@echo off
set ANT_HOME=X:\Program Files\apache-ant-1.9.4
set JAVA_HOME=X:\Program Files\Java\jdk1.8.0_201

set Path=%Path%;%ANT_HOME%\bin
cmd /c ant dist
pause