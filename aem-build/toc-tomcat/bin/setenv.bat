SET STP_HOME=..\..\..\..
SET STP_HOME_UNIX=../../../..

SET CATALINA_HOME=%STP_HOME%\tomcat\core\apache-tomcat-7.0.47
SET CATALINA_BASE=%STP_HOME%\tomcat\instances\tc1

REM JMX_OPTS port settings deprecated in favor of a lifecycle listener in server.xml
SET JMX_OPTS=-Dcom.sun.management.jmxremote.ssl=false
SET JMX_OPTS=%JMX_OPTS% -Dcom.sun.management.jmxremote.authenticate=false
SET JMX_OPTS=%JMX_OPTS% -Dcom.sun.management.jmxremote.password.file=%CATALINA_BASE%/conf/jmxremote.password
SET JMX_OPTS=%JMX_OPTS% -Dcom.sun.management.jmxremote.access.file=%CATALINA_BASE%/conf/jmxremote.access

SET SSL_OPTS=-Ddeployment.security.SSLv2Hello=false -Ddeployment.security.SSLv3=false 
SET SSL_OPTS=%SSL_OPTS% -Ddeployment.security.TLSv1=false -Ddeployment.security.TLSv1.1=false 
SET SSL_OPTS=%SSL_OPTS% -Ddeployment.security.TLSv1.2=true
SET SSL_OPTS=%SSL_OPTS% -Dhttps.protocols=TLSv1.2

SET SSL_DEBUG_OPTS=-Djavax.net.debug=ssl

SET ATOMIKOS_OPTS=-Dcom.atomikos.icatch.tm_unique_name=tc1
SET SPRING_OPTS=-javaagent:%CATALINA_HOME%\lib\siemens\spring-instrument-3.2.6.RELEASE.jar

SET CATALINA_OPTS=-XX:PermSize=512m -XX:MaxPermSize=512m

SET STP_OPTS=-DPROPERTIES_ROOT_PATH=%STP_HOME%\tomcat\properties
SET LOG_OPTS=-Dlog4j.debug=true
SET LOG_OPTS=-Dlog.root.dir=%STP_HOME_UNIX%/tomcat/logs
SET LOG_OPTS=%LOG_OPTS% -Dlog4j.configuration=file:/%STP_HOME_UNIX%/tomcat/properties/log4j.xml

SET LOGIN_CONFIG=-Djava.security.auth.login.config=%STP_HOME%\tomcat\properties\jaas.config

SET JREBEL_OPTS=-Drebel.env.ide.version=4.3.1
SET JREBEL_OPTS=%JREBEL_OPTS% -Drebel.env.ide.product=Eclipse
SET JREBEL_OPTS=%JREBEL_OPTS% -Drebel.env.ide=Eclipse
SET JREBEL_OPTS=%JREBEL_OPTS% -Drebel.notification.url=http://127.0.0.1:64632/jrebel/notifications
SET JREBEL_OPTS=%JREBEL_OPTS% -Drebel.workspace.path="V:\toc\v1\workspace"
SET JREBEL_OPTS=%JREBEL_OPTS% -Drebel.properties="C:\Users\horspe00\.jrebel\jrebel.properties"

SET APR_OPTS=-Djava.library.path=%CATALINA_HOME%\bin

SET PROD_OPTS=%APR_OPTS% %STP_OPTS% %SSL_OPTS% %ATOMIKOS_OPTS% %SPRING_OPTS% %CATALINA_OPTS% %LOG_OPTS% %LOGIN_CONFIG%
SET DEBUG_OPTS=%PROD_OPTS% %JMX_OPTS% %SSL_DEBUG_OPTS% %JREBEL_OPTS%

SET JAVA_SERVICE_OPTS=%PROD_OPTS: =#%
SET JAVA_OPTS=%DEBUG_OPTS%
