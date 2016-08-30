SET JAVA_HOME=C:\jdk1.8.0_66
REM SET STP_HOME=D:\stp
REM SET STP_HOME_UNIX=D:/stp

SET CATALINA_HOME=%~dp0..
SET CATALINA_BASE=%~dp0..
ECHO echo Using jwala CATALINA_BASE:   "%CATALINA_BASE%"

REM JMX_OPTS port settings deprecated in favor of a lifecycle listener in server.xml
SET JMX_OPTS=-Dcom.sun.management.jmxremote.ssl=false
SET JMX_OPTS=%JMX_OPTS% -Dcom.sun.management.jmxremote.authenticate=false
SET JMX_OPTS=%JMX_OPTS% -Dcom.sun.management.jmxremote.password.file=%CATALINA_BASE%/conf/jmxremote.password
SET JMX_OPTS=%JMX_OPTS% -Dcom.sun.management.jmxremote.access.file=%CATALINA_BASE%/conf/jmxremote.access

SET SSL_OPTS=-Ddeployment.security.SSLv2Hello=false -Ddeployment.security.SSLv3=false
SET SSL_OPTS=%SSL_OPTS% -Ddeployment.security.TLSv1=false -Ddeployment.security.TLSv1.1=false
SET SSL_OPTS=%SSL_OPTS% -Ddeployment.security.TLSv1.2=true
SET SSL_OPTS=%SSL_OPTS% -Dhttps.protocols=TLSv1.2
SET SSL_OPTS=%SSL_OPTS% -Djavax.net.ssl.trustStore=D:\stp\app\instances\CTO-N9SF-LTST-TOC\data\properties/tomcatTrustStore.jks
SET SSL_OPTS=%SSL_OPTS% -Djavax.net.ssl.trustStorePassword=changeit

REM High impact, unused: -Djavax.net.debug=ssl:handshake
SET SSL_DEBUG_OPTS=-Djavax.net.debug=ssl:handshake

SET CATALINA_OPTS=-XX:PermSize=128m -XX:MaxPermSize=256m
SET CATALINA_OPTS=%CATALINA_OPTS% -Xmx2048m -Xms256m
SET CATALINA_OPTS=%CATALINA_OPTS% -DSTP_HOME=%STP_HOME%

SET STP_OPTS=-DPROPERTIES_ROOT_PATH=%CATALINA_HOME%\data\properties
REM SET STP_OPTS=%STP_OPTS% -DSTP_PROPERTIES_DIR=D:\stp\app\properties

SET LOG_OPTS=-Dlog4j.configuration=log4j.xml
SET LOG_OPTS=%LOG_OPTS% -Dlog4j.debug=true
REM Logging - Useful, low impact, on: log4j.debug=true; Unused, off: -Dlog.root.dir=/logs

SET APR_OPTS=-Djava.library.path=%CATALINA_HOME%\bin

SET GC_DEBUG_OPTS=-XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -XX:+PrintGCApplicationConcurrentTime -verbose:gc
SET GC_OPTS=-XX:MaxHeapFreeRatio=40 -XX:GCTimeRatio=9
SET GC_DEBUG_OPTS=%GC_DEBUG_OPTS% -Xloggc:D:/stp/app/instances/CTO-N9SF-LTST-TOC/logs/gc.log

SET JACOCO_OPTS=-javaagent:D:/stp/app/instances/CTO-N9SF-LTST-TOC/lib/jacocoagent.jar=output=file,destfile=D:/stp/app/instances/CTO-N9SF-LTST-TOC/data/integration_test_toc.exec,jmx=true,includes=com.cerner.*

SET PROD_OPTS=%APR_OPTS% %STP_OPTS% %SSL_OPTS% %JMX_OPTS% %CATALINA_OPTS% %LOG_OPTS% %GC_OPTS%

SET DEBUG_OPTS=%PROD_OPTS% %GC_DEBUG_OPTS%

SET JAVA_SERVICE_OPTS=%PROD_OPTS: =#%
SET JAVA_OPTS=%DEBUG_OPTS%
