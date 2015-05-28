:: -------------------------
:: Set the STP_HOME variable 
:: -------------------------

CALL:stpSetHome
SET STP_HOME_UNIX=%STP_HOME:\=/%

:: ------------------------------------------------------------------------------
:: Use fn stpSet to set any path variables. This allows the 'stp' folder to be 
:: moved anywhere in the file system. EPM properties (marked by @) determine 
:: the location at build time and stpSet will adapt the path based on the current 
:: location of the 'stp' folder.  Relocation of the 'stp' dir is needed primarily 
:: for developer environments. 
:: ------------------------------------------------------------------------------

CALL:stpSet JAVA_HOME d:\stp\jdk1.7.0_45
SET JRE_HOME=%JAVA_HOME%\jre

CALL:stpSet CATALINA_HOME d:\stp\siemens\apache-tomcat-7.0.55\core
CALL:stpSet CATALINA_BASE d:\stp\siemens\instances\jvm-1
SET STP_OPTS=%STP_OPTS% -Dsystem_key_4=system_value_4
SET STP_OPTS=%STP_OPTS% -Dsystem_key_1=system_value_1
SET STP_OPTS=%STP_OPTS% -Dsystem_key_3=system_value_3
SET STP_OPTS=%STP_OPTS% -Dsystem_key_2=system_value_2




REM JMX_OPTS port settings deprecated in favor of a lifecycle listener in server.xml
SET JMX_OPTS=-Dcom.sun.management.jmxremote.ssl=false
SET JMX_OPTS=%JMX_OPTS% -Dcom.sun.management.jmxremote.authenticate=false
REM SET JMX_OPTS=%JMX_OPTS% -Dcom.sun.management.jmxremote.password.file=%CATALINA_BASE%/conf/jmxremote.password
REM SET JMX_OPTS=%JMX_OPTS% -Dcom.sun.management.jmxremote.access.file=%CATALINA_BASE%/conf/jmxremote.access

REM SET SSL_OPTS=-Ddeployment.security.SSLv2Hello=false -Ddeployment.security.SSLv3=false 
REM SET SSL_OPTS=%SSL_OPTS% -Ddeployment.security.TLSv1=false -Ddeployment.security.TLSv1.1=false 
REM SET SSL_OPTS=%SSL_OPTS% -Ddeployment.security.TLSv1.2=true
REM SET SSL_OPTS=%SSL_OPTS% -Dhttps.protocols=TLSv1.2
SET SSL_OPTS=-Dhttps.protocols=TLSv1.2

REM Warning - too many system properties will cause the deployment to fail due to environment space.

SET SSL_DEBUG_OPTS=-Djavax.net.debug=ssl

SET ATOMIKOS_OPTS=-Dcom.atomikos.icatch.tm_unique_name=jvm-1

CALL:stpSet SPRING_AGENT d:\stp\siemens\lib\tomcat\ext\spring-instrument-3.2.6.RELEASE.jar
SET SPRING_OPTS=-javaagent:%SPRING_AGENT%

:: -------------------------------------------------------------------------------------------------------------------------------
:: Set the location of the property source files.  If not specified as an environment variable, then set to the default location.
:: -------------------------------------------------------------------------------------------------------------------------------
IF "%STP_PS_LOC%" == "" (
   CALL:stpSet STP_PS_LOC d:\stp\siemens\properties\propertySource.properties
)

:: --------------------------------------------------------------------------------------------------------------------------------------------------
:: STP_HOME, gsm classLoaderUrl, and property source location are needed as system properties for replacement in files like server.xml or context.xml
:: --------------------------------------------------------------------------------------------------------------------------------------------------
SET CATALINA_OPTS=-XX:PermSize=512m -XX:MaxPermSize=512m -DSTP_HOME=%STP_HOME% -Dgsm.classloader.url=%STP_HOME_UNIX%/siemens/lib/tomcat/ext/gsm -Dcom.siemens.cto.infrastructure.properties.propertySourceLocations=%STP_PS_LOC%

CALL:stpSet PROPERTIES_PATH d:\stp\siemens\properties
SET STP_OPTS=-DPROPERTIES_ROOT_PATH=%PROPERTIES_PATH%
SET environment_key_3=environment_value_3
SET environment_key_2=environment_value_2
SET environment_key_1=environment_value_1




CALL:stpSet LOG_ROOT_DIR d:/stp/apache-tomcat-7.0.55/logs
SET LOG_OPTS=-Dlog.root.dir=%LOG_ROOT_DIR%
SET LOG_OPTS=%LOG_OPTS% -Dlog4j.configuration=log4j.xml -Dlog4j.debug=true

SET LOGIN_CONFIG=-Djava.security.auth.login.config=%PROPERTIES_PATH%\jaas.config

SET APR_OPTS=-Djava.library.path=%CATALINA_HOME%\bin

SET PROD_OPTS=%APR_OPTS% %STP_OPTS% %SSL_OPTS% %JMX_OPTS% %ATOMIKOS_OPTS% %SPRING_OPTS% %CATALINA_OPTS% %LOG_OPTS% %LOGIN_CONFIG%
SET DEBUG_OPTS=%PROD_OPTS%

SET JAVA_SERVICE_OPTS=%PROD_OPTS: =#%
SET JAVA_OPTS=%DEBUG_OPTS%

goto:eof



:: ----------------------------------------------------------------------------
:: Sets the STP_DRIVE and STP_HOME variables based on the location of this file
:: ----------------------------------------------------------------------------

:stpSetHome

set STP_DRIVE=%~d0

Setlocal EnableDelayedExpansion

set path=%~p0
set pathList=%path:\=,%
set trimmedPathList=%pathList:~1,-1%
set stpDir=%STP_DRIVE%
set foundStp=0
set savedStpDir=!stpDidr!

For %%A in (%trimmedPathList%) do (
   set stpDir=!stpDir!\%%A
   IF %%A==stp (
      set savedStpDir=!stpDir!
      set foundStp=1
   )
)
:END_FIND_STP_DIR

if %foundStp% EQU 0 (echo stp dir not found so setting to default & set savedStpDir=d:\stp)
echo stpDir is %savedStpDir%

ENDLOCAL & SET STP_HOME=%savedStpDir%

goto:eof


:: ----------------------------------------------------------------------
:: Function to set paths variables based on the current value of STP_HOME
:: ----------------------------------------------------------------------

:stpSet
:: arg1 = variable to set (eg CATALINA_HOME)
:: arg2 = original value of variable (eg d:\stp\siemens\apache-tomcat-7.0.55\core)
::
:: if STP_HOME=e:\view_stores\stp, then this fn sets CATALINA_HOME to e:\view_stores\stp\siemens\apache-tomcat-7.0.55\core 
 
SET %~1=%~2
CALL SET %~1=%%%~1:d:\stp=%STP_HOME%%%

goto:eof

