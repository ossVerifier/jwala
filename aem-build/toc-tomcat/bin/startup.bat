SETLOCAL 
SET JAVA_HOME=D:\java\jdk-primary
SET STP_HOME=V:\Apache
SET STP_HOME_UNIX=V:/Apache

SET CATALINA_HOME=%STP_HOME%\tomcat\core\apache-tomcat-7.0.47
SET CATALINA_BASE=%STP_HOME%\tomcat\instances\tc1

CD %~dp0
CALL .\setenv.bat

%CATALINA_HOME%\bin\catalina.bat start
ENDLOCAL