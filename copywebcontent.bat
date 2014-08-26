@echo off
SETLOCAL
SET STP_HOME=\apache
SET STP_TC_HOME=%STP_HOME%\tomcat
pushd %~dp0
robocopy aem-webapp\src\main\webapp\resources %STP_TC_HOME%\instances\tc1\webapps\aem\resources /XF web.xml /XF context.xml /XD lib /MIR
robocopy aem-webapp\src\main\webapp\WEB-INF\views %STP_TC_HOME%\instances\tc1\webapps\aem\WEB-INF\views /XF web.xml /XF context.xml /XD lib /MIR
robocopy aem-webapp\build\react\js %STP_TC_HOME%\instances\tc1\webapps\aem\gen\resources\js\react /E
popd
ENDLOCAL