SET JAVA_HOME=D:\java\jdk-primary
SET STP_HOME=..\..\..\..
SET STP_HOME_UNIX=../../../..

SET CATALINA_HOME=%STP_HOME%\tomcat\core\apache-tomcat-7.0.47
SET CATALINA_BASE=%STP_HOME%\tomcat\instances\tc1

SET CATALINA_OPTS=-javaagent:%CATALINA_HOME%\lib\siemens\spring-instrument-3.2.6.RELEASE.jar -XX:PermSize=512m -XX:MaxPermSize=512m
SET JAVA_OPTS=-DPROPERTIES_ROOT_PATH=%STP_HOME%\tomcat\properties -Dlog4j.debug=true -Dlog4j.configuration=file:/%STP_HOME_UNIX%/tomcat/properties/log4j.xml -Djava.security.auth.login.config=%STP_HOME%\tomcat\properties\jaas.config -Dlog.root.dir=%STP_HOME_UNIX%/tomcat/logs %JREBEL_OPTS%
SET JAVA_SERVICE_OPTS=-DPROPERTIES_ROOT_PATH=%STP_HOME%\tomcat\properties#-Dlog4j.debug=true#-Dlog4j.configuration=file:/%STP_HOME_UNIX%/tomcat/properties/log4j.xml#-Djava.security.auth.login.config=%STP_HOME%\tomcat\properties\jaas.config#-Dlog.root.dir=%STP_HOME_UNIX%/tomcat/logs#-javaagent:%CATALINA_HOME%\lib\siemens\spring-instrument-3.2.6.RELEASE.jar#-XX:PermSize=512m#-XX:MaxPermSize=512m
