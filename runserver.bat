@echo off
setlocal
cd /d "%~dp0"
set "FAUMONIAONLINE_VERSION=8.0"
if not defined FAUMONIA_JAVA set "FAUMONIA_JAVA=java"
if "%~1"=="--check" set "FAUMONIA_NO_PAUSE=1"

rem JARs must precede loose files left by older, manually unpacked packages.
set "LOCALCLASSPATH=FaumoniaOnline-server-%FAUMONIAONLINE_VERSION%.jar;FaumoniaOnline-maps-%FAUMONIAONLINE_VERSION%.jar;FaumoniaOnline-xmlconf-%FAUMONIAONLINE_VERSION%.jar;marauroa.jar;commons-lang.jar;mysql-connector.jar;h2.jar;log4j.jar;simple.jar;groovy.jar;data\script;data\conf;."
for %%J in ("FaumoniaOnline-server-%FAUMONIAONLINE_VERSION%.jar" "FaumoniaOnline-maps-%FAUMONIAONLINE_VERSION%.jar" "FaumoniaOnline-xmlconf-%FAUMONIAONLINE_VERSION%.jar" marauroa.jar commons-lang.jar mysql-connector.jar h2.jar log4j.jar simple.jar groovy.jar) do (
    if not exist "%%~J" (
        echo Brak pliku: %%~J
        echo Rozpakuj cala paczke ZIP serwera. Nie rozpakowuj plikow JAR.
        goto failed
    )
)
"%FAUMONIA_JAVA%" -version >nul 2>&1
if errorlevel 1 (
    echo Nie znaleziono dzialajacej Javy. Zainstaluj Java 8 lub ustaw FAUMONIA_JAVA na java.exe.
    goto failed
)
if "%~1"=="--check" goto check
"%FAUMONIA_JAVA%" -Xmx400m -cp "%LOCALCLASSPATH%" games.stendhal.server.StendhalServer -c server.ini -l %*
goto finished

:check
"%FAUMONIA_JAVA%" -cp "%LOCALCLASSPATH%" games.stendhal.server.ServerPackageCheck
:finished
set "FAUMONIA_EXIT=%ERRORLEVEL%"
if not "%FAUMONIA_EXIT%"=="0" if not defined FAUMONIA_NO_PAUSE pause
exit /b %FAUMONIA_EXIT%

:failed
if not defined FAUMONIA_NO_PAUSE pause
exit /b 1
