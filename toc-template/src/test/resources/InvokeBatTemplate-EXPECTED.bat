CALL d:\stp\app\instances\tc1\bin\setenv.bat
CMD /C d:\stp\apache-tomcat-7.0.55\core\bin\service.bat install tc1
CMD /C d:\stp\apache-tomcat-7.0.55\core\bin\tomcat7 //US//tc1 ++JvmOptions %JAVA_SERVICE_OPTS%
SC CONFIG tc1 start= auto