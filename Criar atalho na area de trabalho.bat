@echo off
cd /d "%~dp0"
powershell -NoProfile -ExecutionPolicy Bypass -Command "$d=[Environment]::GetFolderPath('Desktop'); $s=(New-Object -ComObject WScript.Shell).CreateShortcut($d+'\Clubinho do Focinho.lnk'); $s.TargetPath='%~dp0Abrir Clubinho do Focinho.bat'; $s.WorkingDirectory='%~dp0'; $s.IconLocation='%~dp0icone.ico'; $s.WindowStyle=7; $s.Save()"
echo.
echo Pronto! O atalho "Clubinho do Focinho" foi criado na area de trabalho.
echo.
pause
