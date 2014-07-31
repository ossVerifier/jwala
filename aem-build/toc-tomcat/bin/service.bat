CALL D:\apache\tomcat\instances\tc1\bin\setenv.bat

ECHO Uninstall the service
CMD /C %CATALINA_HOME%\bin\service.bat remove TC-1

ECHO Install the service
CMD /C %CATALINA_HOME%\bin\service.bat install TC-1

ECHO Set java opts
CMD /C %CATALINA_HOME%\bin\tomcat7 //US//TC-1 ++JvmOptions %JAVA_SERVICE_OPTS%

