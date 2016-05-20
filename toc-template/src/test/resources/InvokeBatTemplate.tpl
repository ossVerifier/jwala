REM ${varsProperties.'test.toc.property'}

CALL d:\\stp\\app\\instances\\${jvm.jvmName}\\bin\\setenv.bat
CMD /C d:\\stp\\apache-tomcat-7.0.55\\core\\bin\\service.bat install ${jvm.jvmName}
CMD /C d:\\stp\\apache-tomcat-7.0.55\\core\\bin\\tomcat7 //US//${jvm.jvmName} ++JvmOptions %JAVA_SERVICE_OPTS%
SC CONFIG ${jvm.jvmName} start= auto