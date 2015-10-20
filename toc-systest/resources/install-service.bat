REM %1 - Location of setenv.bat
REM %2 - Tomcat core or CATALINA_HOME
REM %3 - JVM name e.g. JVM-1
REM %4 - Jacoco agent location
REM %5 - Jacoco exec destination file e.g. D:\scratch\jacoco\hct-rest.exec

CALL %1\setenv.bat

ECHO Uninstall the service...
CMD /C %2\bin\service.bat remove %3

ECHO Install the service...
CMD /C %2\bin\service.bat install %3

ECHO Set java opts...

CMD /C %2\bin\tomcat7 //US//%3 ++JvmOptions %JAVA_SERVICE_OPTS%#-javaagent:%4=destfile=%5,append=true,includes=com.siemens.cto.*

ECHO Adding spring jpa agent to TOC's service
CMD /C %2\bin\tomcat7 //US//%3 ++JvmOptions -javaagent:%CATALINA_BASE%\lib\spring-instrument-3.2.6.RELEASE.jar

ECHO Finished installing %3 as a service
