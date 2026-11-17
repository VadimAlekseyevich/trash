@echo off
cd /d "%~dp0"
call "%~dp0mvnw.cmd" "-DsiteUrl=http://localhost:8080/litecart/" "-DpauseSeconds=10" test
exit /b %ERRORLEVEL%
