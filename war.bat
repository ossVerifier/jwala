@echo off
SETLOCAL
call d:\java\setenv.bat
SET STP_HOME=\stp
SET STP_TC_HOME=%STP_HOME%\siemens
pushd %~dp0\aem-webapp
call gradle %1 war
if errorlevel 1 goto fail
del /Q %STP_TC_HOME%\webapps\aem-webapp-1.0-SNAPSHOT.war 
rmdir /s /q %STP_TC_HOME%\instances\jvm-1\stpapps\aem
copy /Y build\libs\aem-webapp-1.0-SNAPSHOT.war %STP_TC_HOME%\webapps
popd
goto :eof
:fail
echo BUILD FAILURE - NO UPDATE
popd
goto :eof
