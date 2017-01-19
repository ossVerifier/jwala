#!/usr/bin/env bash
cygwin=false
linux=false
case "`uname`" in
CYGWIN*) cygwin=true;;
Linux*) linux=true;;
esac
echo '***heapdump-start***'
if $linux; then
	export JAVA_HOME=$1
	export DATA_DIR=$2
	export DUMP_FILE=$3
	export DUMP_LIVE=$4
	export JVM_INSTANCE_PATH=$5

	mkdir -p /opt/ctp/app/data
	/usr/bin/sudo -u tomcat ${JAVA_HOME}/bin/jmap -dump:${DUMP_LIVE}format=b,file=${DATA_DIR}/${DUMP_FILE} $(<${JVM_INSTANCE_PATH}/logs/catalina.pid)
fi
echo '***heapdump-end***'
	
