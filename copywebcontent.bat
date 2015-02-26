@echo off
SETLOCAL
SET STP_HOME=\stp
SET STP_TC_HOME=%STP_HOME%\siemens
pushd %~dp0
robocopy aem-webapp\src\main\webapp\resources %STP_TC_HOME%\instances\jvm-1\stpapps\aem\resources /XF web.xml /XF context.xml /XD lib /MIR
robocopy aem-webapp\src\main\webapp\WEB-INF\views %STP_TC_HOME%\instances\jvm-1\stpapps\aem\WEB-INF\views /XF web.xml /XF context.xml /XD lib /MIR
robocopy aem-webapp\build\react\js %STP_TC_HOME%\instances\jvm-1\stpapps\aem\gen\resources\js\react /E
popd
ENDLOCAL