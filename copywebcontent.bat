@echo off
pushd %~dp0
robocopy aem-webapp\src\main\webapp ..\..\deploy\instances\tc1\webapps\aem /XF web.xml /XF context.xml /XD lib /MIR
robocopy aem-webapp\build\react\js ..\..\deploy\instances\tc1\webapps\aem\gen\resources\js\react /E
popd