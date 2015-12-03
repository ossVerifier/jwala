@echo off
SETLOCAL
call d:\java\setenv.bat
SET STP_HOME=\stp
SET STP_TC_HOME=%STP_HOME%\siemens
pushd %~dp0\toc-webapp
call gradle %1 war
if errorlevel 1 goto fail
echo Deleting existing WAR
del /Q %STP_TC_HOME%\webapps\toc-webapp-1.0-SNAPSHOT.war
rmdir /s /q %STP_TC_HOME%\instances\jvm-1\stpapps\aem
echo Copying packed WAR
copy /Y build\libs\toc-webapp-1.0-SNAPSHOT.war %STP_TC_HOME%\webapps
popd
echo Exploding WAR
mkdir %STP_TC_HOME%\instances\jvm-1\stpapps\aem
pushd %STP_TC_HOME%\instances\jvm-1\stpapps\aem
%JAVA_HOME%\bin\jar xf %STP_TC_HOME%\webapps\toc-webapp-1.0-SNAPSHOT.war
popd
goto :eof
:fail
echo BUILD FAILURE - NO UPDATE
popd
goto :eof
