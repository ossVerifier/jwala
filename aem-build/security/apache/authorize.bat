@echo off 
REM The assumption is that this file is being run by the Apache user.
REM On first start, we remove read privileges from all but the Apache user.
if exist secureP0.bat call secureP0.bat
if exist secureP0.bat goto :eof
echo @if exist "%~dpfx0" @echo @call secureP0.bat ^> "%~dpfx0" > secureP0.bat 
echo @echo Passw0rd>> secureP0.bat
echo Y>%TEMP%\yes.txt
echo Updating secureP0.bat to limit privs >> secure.log
cacls "secureP0.bat" /S:D:P(A;;FA;;;SY) <%TEMP%\yes.txt >>secure.log 2>>secure-error.log
cacls "secureP0.bat" /E /G horspe00:F <%TEMP%\yes.txt >>secure.log 2>>secure-error.log
del /Q %TEMP%\yes.txt
call secureP0.bat