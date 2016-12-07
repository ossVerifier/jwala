@ECHO OFF

set svc_username=%1
set svc_password=%2

CALL ${vars['remote.paths.instances']}\\${jvm.jvmName}\bin\setenv.bat

ECHO Run pre_install.bat
if exist ${vars['remote.paths.instances']}\\${jvm.jvmName}\bin\pre_install.bat call ${vars['remote.paths.instances']}\\${jvm.jvmName}\bin\pre_install.bat
IF "%ERRORLEVEL%" NEQ "0" (
    EXIT %ERRORLEVEL%"
)

ECHO Install the service
CMD /C ${vars['remote.paths.tomcat.core']}\bin\service.bat install ${jvm.jvmName}

ECHO Update Java Options
CMD /C ${vars['remote.paths.tomcat.core']}\bin\tomcat7 //US//${jvm.jvmName} ++JvmOptions %JAVA_SERVICE_OPTS% --StartPath %START_PATH% --StdOutput "" --StdError ""

ECHO Change the service to automatically start
SC CONFIG ${jvm.jvmName} start= auto

ECHO Run post_install.bat
if exist ${vars['remote.paths.instances']}\\${jvm.jvmName}\bin\post_install.bat call ${vars['remote.paths.instances']}\\${jvm.jvmName}\bin\post_install.bat
IF "%ERRORLEVEL%" NEQ "0" (
    EXIT %ERRORLEVEL%"
)

if %svc_username%=="" goto :no_user

SC CONFIG ${jvm.jvmName} obj=%svc_username% password=%svc_password%

:no_user

EXIT %ERRORLEVEL%
