@echo off
robocopy e:\toc\v1\git\cto-aem\aem-webapp\src\main\webapp e:\toc\v1\deploy\instances\tc1\webapps\aem /XF web.xml /XF context.xml /XD lib /MIR
robocopy e:\toc\v1\git\cto-aem\aem-webapp\build\react\js e:\toc\v1\deploy\instances\tc1\webapps\aem\gen\resources\js\react /E