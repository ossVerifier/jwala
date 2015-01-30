REM %1 - Location of setenv.bat
REM %2 - Tomcat core or CATALINA_HOME
REM %3 - JVM name e.g. JVM-1

CALL %1\setenv.bat
ECHO Uninstall the service...
CMD /C %2\bin\service.bat remove %3
ECHO Finished removing %3 as a service