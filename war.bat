@echo off
call d:\java\setenv.bat
pushd E:\TOC\v1\git\cto-aem\aem-webapp
call gradle war
if errorlevel 1 goto fail
rmdir /s /q E:\TOC\v1\deploy\instances\tc1\webapps\aem
copy /Y build\libs\aem-webapp-1.0-SNAPSHOT.war E:\TOC\v1\deploy\webapps
goto :eof
:fail
echo BUILD FAILURE - NO UPDATE
goto :eof
popd
