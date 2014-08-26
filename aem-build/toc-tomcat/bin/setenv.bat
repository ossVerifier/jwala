SET STP_HOME=..\..\..\..
SET STP_HOME_UNIX=../../../..

SET CATALINA_HOME=%STP_HOME%\tomcat\core\apache-tomcat-7.0.47
SET CATALINA_BASE=%STP_HOME%\tomcat\instances\tc1

SET JMX_OPTS=-Dcom.sun.management.jmxremote
SET JMX_OPTS=%JMX_OPTS% -Dcom.sun.management.jmxremote.port=6969
SET JMX_OPTS=%JMX_OPTS% -Dcom.sun.management.jmxremote.ssl=false
SET JMX_OPTS=%JMX_OPTS% -Dcom.sun.management.jmxremote.authenticate=false

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

SET PROD_OPTS=%APR_OPTS% %STP_OPTS% %ATOMIKOS_OPTS% %SPRING_OPTS% %CATALINA_OPTS% %LOG_OPTS% %LOGIN_CONFIG%
SET DEBUG_OPTS=%PROD_OPTS% %JMX_OPTS% %JREBEL_OPTS%

SET JAVA_SERVICE_OPTS=%PROD_OPTS: =#%
SET JAVA_OPTS=%DEBUG_OPTS%
