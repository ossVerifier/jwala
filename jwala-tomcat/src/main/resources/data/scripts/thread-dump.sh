#!/usr/bin/env bash
export JAVA_HOME=$1
export JVM_INSTANCE_DIR=$2
sudo ${JAVA_HOME}/bin/jstack $(<${JVM_INSTANCE_DIR}/logs/catalina.pid)