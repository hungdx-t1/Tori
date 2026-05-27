@echo off
:loop
java -jar server.jar
if %ERRORLEVEL% equ 42 goto loop
echo Server da tat hoan toan.
pause