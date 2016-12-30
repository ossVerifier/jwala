#!/usr/bin/env bash
echo '***heapdump-start***'
#export TIME_STAMP=%1
#export DATA_DIR=/opt/ctp/app/data
#export DUMP_LIVE=
#export DUMP_FILE=heapDump.HCT.TIME_STAMP

export JAVA_HOME=$1
export DATA_DIR=$2
export DUMP_FILE=$3
export DUMP_LIVE=$4
export JVM_INSTANCE_PATH=$5

mkdir -p /opt/ctp/app/data
sudo ${JAVA_HOME}/bin/jmap -dump:${DUMP_LIVE}format=b,file=${DATA_DIR}/${DUMP_FILE} $(<${JVM_INSTANCE_PATH}/logs/catalina.pid)
echo '***heapdump-end***'
