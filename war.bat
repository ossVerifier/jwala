@echo off
call d:\java\setenv.bat
pushd %~dp0\aem-webapp
call gradle war
if errorlevel 1 goto fail
del /Q ..\..\..\deploy\webapps\aem-webapp-1.0-SNAPSHOT.war 
rmdir /s /q ..\..\..\deploy\instances\tc1\webapps\aem
copy /Y build\libs\aem-webapp-1.0-SNAPSHOT.war ..\..\..\deploy\webapps
popd
goto :eof
:fail
echo BUILD FAILURE - NO UPDATE
goto :eof
popd
