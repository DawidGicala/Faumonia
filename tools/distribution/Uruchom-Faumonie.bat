@echo off
setlocal
cd /d "%~dp0"
set "FAUMONIA_JAVA="
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "FAUMONIA_JAVA=%JAVA_HOME%\bin\java.exe"
if not defined FAUMONIA_JAVA for /f "delims=" %%J in ('where java.exe 2^>nul') do if not defined FAUMONIA_JAVA set "FAUMONIA_JAVA=%%J"
if not defined FAUMONIA_JAVA for /f "tokens=2,*" %%A in ('reg query "HKLM\SOFTWARE\JavaSoft" /s /v JavaHome 2^>nul ^| findstr REG_SZ') do if exist "%%B\bin\java.exe" set "FAUMONIA_JAVA=%%B\bin\java.exe"
if not defined FAUMONIA_JAVA for /f "tokens=2,*" %%A in ('reg query "HKLM\SOFTWARE\WOW6432Node\JavaSoft" /s /v JavaHome 2^>nul ^| findstr REG_SZ') do if exist "%%B\bin\java.exe" set "FAUMONIA_JAVA=%%B\bin\java.exe"
if not defined FAUMONIA_JAVA goto missing_java
if not exist "FaumoniaOnline.jar" goto missing_files
"%FAUMONIA_JAVA%" -Dfile.encoding=UTF-8 -jar "FaumoniaOnline.jar" %*
if errorlevel 1 (
    echo.
    echo Nie udalo sie uruchomic gry. Wymagana jest Java 8 lub nowsza.
    echo Sprawdz, czy caly ZIP zostal rozpakowany, razem z folderem lib.
    pause
    exit /b 1
)
exit /b 0
:missing_java
echo Do uruchomienia Faumonii potrzebna jest Java 8 lub nowsza.
echo Pobierz Jawe: https://www.java.com/pl/download/
echo Po instalacji Javy uruchom gre ponownie.
pause
exit /b 1
:missing_files
echo Brakuje pliku FaumoniaOnline.jar. Rozpakuj caly ZIP do jednego folderu.
pause
exit /b 1
