CALL d:\\stp\\app\\instances\\${jvm.jvmName}\\bin\\setenv.bat

ECHO Install the service
CMD /C d:\\stp\\apache-tomcat-7.0.55\\core\\bin\\service.bat install ${jvm.jvmName}

ECHO Update Java Options
CMD /C d:\\stp\\apache-tomcat-7.0.55\\core\\bin\\tomcat7 //US//${jvm.jvmName} ++JvmOptions %JAVA_SERVICE_OPTS%

ECHO Change the service to automatically start
SC CONFIG ${jvm.jvmName} start= auto

EXIT %ERRORLEVEL%
