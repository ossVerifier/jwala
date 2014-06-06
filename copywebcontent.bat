@echo off
pushd %~dp0
robocopy aem-webapp\src\main\webapp\resources ..\..\deploy\instances\tc1\webapps\aem\resources /XF web.xml /XF context.xml /XD lib /MIR
robocopy aem-webapp\src\main\webapp\WEB-INF\views ..\..\deploy\instances\tc1\webapps\aem\WEB-INF\views /XF web.xml /XF context.xml /XD lib /MIR
robocopy aem-webapp\build\react\js ..\..\deploy\instances\tc1\webapps\aem\gen\resources\js\react /E
popd