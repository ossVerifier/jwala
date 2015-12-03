SET JAVA_HOME=d:\stp\jdk1.7.0_72
SET STP_HOME=d:\stp
SET STP_HOME_UNIX=d:/stp

SET CATALINA_HOME=%STP_HOME%\apache-tomcat-7.0.55\core
SET CATALINA_BASE=%STP_HOME%\app\instances\CTO-N9SF-LTST-TOC

REM JMX_OPTS port settings deprecated in favor of a lifecycle listener in server.xml
SET JMX_OPTS=-Dcom.sun.management.jmxremote.ssl=false
SET JMX_OPTS=%JMX_OPTS% -Dcom.sun.management.jmxremote.authenticate=false
SET JMX_OPTS=%JMX_OPTS% -Dcom.sun.management.jmxremote.password.file=%CATALINA_BASE%/conf/jmxremote.password
SET JMX_OPTS=%JMX_OPTS% -Dcom.sun.management.jmxremote.access.file=%CATALINA_BASE%/conf/jmxremote.access

SET SSL_OPTS=-Ddeployment.security.SSLv2Hello=false -Ddeployment.security.SSLv3=false
SET SSL_OPTS=%SSL_OPTS% -Ddeployment.security.TLSv1=false -Ddeployment.security.TLSv1.1=false
SET SSL_OPTS=%SSL_OPTS% -Ddeployment.security.TLSv1.2=true
SET SSL_OPTS=%SSL_OPTS% -Dhttps.protocols=TLSv1.2
SET SSL_OPTS=%SSL_OPTS% -Djavax.net.ssl.trustStore=%STP_HOME%\app\properties/ctoTomcatTrustStore.jks
SET SSL_OPTS=%SSL_OPTS% -Djavax.net.ssl.trustStorePassword=Passw0rd

REM High impact, unused: -Djavax.net.debug=ssl:handshake
SET SSL_DEBUG_OPTS=-Djavax.net.debug=ssl:handshake

SET ATOMIKOS_OPTS=-Dcom.atomikos.icatch.tm_unique_name=toc
SET SPRING_OPTS=-javaagent:%CATALINA_BASE%\lib\spring-instrument-3.2.6.RELEASE.jar

SET CATALINA_OPTS=-XX:PermSize=128m -XX:MaxPermSize=256m
SET CATALINA_OPTS=%CATALINA_OPTS% -Xmx2048m -Xms256m
SET CATALINA_OPTS=%CATALINA_OPTS% -DSTP_HOME=%STP_HOME% -Dgsm.classloader.url=%STP_HOME_UNIX%/app/lib/tomcat/gsm

SET STP_OPTS=-DPROPERTIES_ROOT_PATH=%STP_HOME%\app\properties
SET STP_OPTS=%STP_OPTS% -DSTP_PROPERTIES_DIR=%STP_HOME%\app\properties

SET LOG_OPTS=-Dlog4j.configuration=log4j.xml
SET LOG_OPTS=%LOG_OPTS% -Dlog4j.debug=true
REM Logging - Useful, low impact, on: log4j.debug=true; Unused, off: -Dlog.root.dir=d:/stp/apache-tomcat-7.0.55/instances/logs

SET LOGIN_CONFIG=-Djava.security.auth.login.config=%STP_HOME%\app\properties\jaas.config

SET APR_OPTS=-Djava.library.path=%CATALINA_HOME%\bin

SET APP_DYNAMICS_OPTS=-javaagent:d:/stp/app-dynamics-3.9.4.0/agent//javaagent.jar -Dappdynamics.controller.hostName=DEVSRF2813 -Dappdynamics.controller.port=8090
SET APP_DYNAMICS_OPTS=%APP_DYNAMICS_OPTS% -Dappdynamics.agent.applicationName=1D0A-Development-Environment-N9SF-LTST -Dappdynamics.agent.tierName=TOMCAT-OPERATIONS-CENTER -Dappdynamics.agent.nodeName=CTO-N9SF-LTST-TOC
SET APP_DYNAMICS_OPTS=%APP_DYNAMICS_OPTS% -Dappdynamics.agent.logs.dir=d:/stp/app/data/app-dynamics/logs/

SET GC_DEBUG_OPTS=-XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -XX:+PrintGCApplicationConcurrentTime -verbose:gc
SET GC_OPTS=-XX:MaxHeapFreeRatio=40 -XX:GCTimeRatio=9

SET PROD_OPTS=%APR_OPTS% %STP_OPTS% %SSL_OPTS% %JMX_OPTS% %ATOMIKOS_OPTS% %SPRING_OPTS% %CATALINA_OPTS% %LOG_OPTS% %LOGIN_CONFIG% %GC_OPTS%
SET DEBUG_OPTS=%PROD_OPTS% %GC_DEBUG_OPTS%

SET JAVA_SERVICE_OPTS=%PROD_OPTS: =#%
SET JAVA_OPTS=%DEBUG_OPTS%