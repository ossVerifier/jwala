@echo off
echo Preparing work/temp/logs folders
mkdir %1\work
mkdir %1\temp
mkdir %1\logs
echo Configuring files
SET CATALINA_HOME=D:\apache\tomcat\core\apache-tomcat-7.0.47
SET JAVA_HOME=D:\apache\java\jdk1.7.0_45
pushd %1
SET CATALINA_BASE=%CD%
echo Using JAVA_HOME: %JAVA_HOME%
echo Using Catalina home: %CD%
echo Using CATALINA_BASE: %CATALINA_BASE%
popd
:retry
echo Configure setenv.bat - replace tc1 with %1
start /wait "CONFIGURE WITH BASE NAME %1" "notepad" %1\bin\setenv.bat
echo Configure Service.bat - replace tc1 with %1
start /wait "CONFIGURE WITH BASE NAME %1" "notepad" %1\bin\service.bat
echo Configure conf/server.xml with port numbers
echo HTTP = BASE PORT, like 8080
echo HTTPS = BASE+1, like 8081
echo Redirect= BASE+1, like 8081
echo Shutdown= BASE+3, like 8083
echo AJP= BASE+4, like 8084
start /wait "CONFIGURE WITH BASE NAME %1" "notepad" %1\conf\server.xml
echo Configure hct.xml - replace jvm name with right name
start /wait "CONFIGURE WITH JVM NAME %1" "notepad" %1\conf\Catalina\localhost\hct.xml
echo Ready to run install
pause
start /wait /D "%CATALINA_BASE%" "INSTALLING" bin\service.bat
if errorlevel 1 (
	echo Problem encountered installing service. Retry configuration or ctrl-c
	pause
	goto retry
)
