@echo off
setlocal
cd /d "%~dp0"
if not defined FAUMONIA_JAVA set "FAUMONIA_JAVA=java"
if not exist "FaumoniaOnline.jar" (
    echo Brak FaumoniaOnline.jar. Uruchom ten plik z folderu rozpakowanego klienta.
    goto failed
)
rem The manifest supplies the bundled data and libraries. The local launcher
rem bypasses the online updater so it cannot replace a development build.
"%FAUMONIA_JAVA%" -cp "FaumoniaOnline.jar" games.stendhal.client.stendhal %*
set "FAUMONIA_EXIT=%ERRORLEVEL%"
if not "%FAUMONIA_EXIT%"=="0" if not defined FAUMONIA_NO_PAUSE pause
exit /b %FAUMONIA_EXIT%

:failed
if not defined FAUMONIA_NO_PAUSE pause
exit /b 1
