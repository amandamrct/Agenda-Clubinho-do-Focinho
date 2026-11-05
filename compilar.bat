@echo off
cd /d "%~dp0"
if exist out rmdir /s /q out
mkdir out
dir /s /b src\*.java > fontes.txt
javac -encoding UTF-8 -d out @fontes.txt
if errorlevel 1 exit /b 1
del fontes.txt
echo Main-Class: clubinho.App> manifesto.txt
jar cfm ClubinhoReservas.jar manifesto.txt -C out . -C resources .
del manifesto.txt
echo Pronto: ClubinhoReservas.jar
