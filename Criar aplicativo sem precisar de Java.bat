@echo off
cd /d "%~dp0"
where jpackage >nul 2>nul
if errorlevel 1 goto semjdk

if exist dist rmdir /s /q dist
if exist entrada rmdir /s /q entrada
mkdir entrada
copy /y ClubinhoReservas.jar entrada\ >nul

jpackage --type app-image --name "Clubinho do Focinho" --input entrada --main-jar ClubinhoReservas.jar --main-class clubinho.App --icon icone.ico --dest dist --add-modules java.base,java.desktop,java.logging --java-options "-Dfile.encoding=UTF-8"
if errorlevel 1 goto erro

rmdir /s /q entrada
echo.
echo Pronto! Copie a pasta "dist\Clubinho do Focinho" inteira para o outro computador.
echo La dentro, o arquivo "Clubinho do Focinho.exe" abre o sistema (nao precisa instalar o Java).
echo.
pause
exit /b 0

:semjdk
echo.
echo O jpackage nao foi encontrado. Ele vem com o JDK 17 (ou mais novo).
echo Instale o JDK, abra um novo terminal e tente de novo.
echo.
pause
exit /b 1

:erro
echo.
echo Algo deu errado ao criar o aplicativo. Mande a mensagem acima para quem esta te ajudando.
echo.
pause
exit /b 1
