#!/usr/bin/env bash
cygwin=false
linux=false
case "`uname`" in
CYGWIN*) cygwin=true;;
Linux*) linux=true;;
esac
# 1 - Java_HOME
# 2 - JVM_INSTANCE_DIR
if $linux; then
	export JAVA_HOME=$1
	export JVM_INSTANCE_DIR=$2
	echo $(<${JVM_INSTANCE_DIR}/logs/catalina.pid)
	/usr/bin/sudo -u tomcat ${JAVA_HOME}/bin/jstack $(<${JVM_INSTANCE_DIR}/logs/catalina.pid)
fi
#TODO fix windows path
if $cygwin; then
	export JAVA_HOME=$1
	export JVM_INSTANCE_DIR=$2
	/usr/bin/sudo ${JAVA_HOME}/bin/jstack $(<${JVM_INSTANCE_DIR}/logs/catalina.pid)
fi