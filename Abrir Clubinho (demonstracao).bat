@echo off
setlocal
cd /d "%~dp0"

where javaw >nul 2>nul
if %errorlevel%==0 goto usarpath

if defined JAVA_HOME if exist "%JAVA_HOME%\bin\javaw.exe" goto usarjavahome

set "JAVAW="
for /d %%D in ("%ProgramFiles%\Java\*" "%ProgramFiles%\Eclipse Adoptium\*" "%ProgramFiles%\Microsoft\*" "%ProgramFiles%\Zulu\*" "%ProgramFiles%\Amazon Corretto\*") do if exist "%%~D\bin\javaw.exe" set "JAVAW=%%~D\bin\javaw.exe"
if defined JAVAW goto usarjavaw

echo.
echo O Java nao foi encontrado neste computador.
echo Instale o Java 17 (ou mais novo) e depois abra este arquivo de novo.
echo Download: https://adoptium.net/temurin/releases/?version=17
echo.
pause
exit /b 1

:usarpath
start "" javaw -jar ClubinhoReservas.jar --demo
exit /b 0

:usarjavahome
start "" "%JAVA_HOME%\bin\javaw.exe" -jar ClubinhoReservas.jar --demo
exit /b 0

:usarjavaw
start "" "%JAVAW%" -jar ClubinhoReservas.jar --demo
exit /b 0
