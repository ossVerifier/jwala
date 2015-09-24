#!/bin/bash

STP_EXIT_CODE_NO_SUCH_SERVICE=123
STP_EXIT_CODE_ABNORMAL_SUCCESS=126
STP_EXIT_CODE_NO_OP=127
STP_EXIT_CODE_SUCCESS=0

if [ "$1" = "" ]; then
    echo $0 not invoked with service name
    exit $STP_EXIT_CODE_NO_OP
fi
export JVMINST=`sc queryex $1 | head -1 | awk '{ sub(/:/,"",$4); print $4 }'`
export JVMPID=`sc queryex $1 | grep PID | awk '{ print $3 }'`
if [ "$JVMINST" = "1060" ]; then
    echo Service $1 not installed on server
    sc queryex $1
    exit $STP_EXIT_CODE_SUCCESS
elif [ "$JVMPID" = "0" ]; then
    sc delete $1
    export SCRETURN=$?
    if [ "$SCRETURN" -ne "0" ]; then
        echo Could not delete service $1 with process id $JVMPID.
    else
        echo Successfully delete service $1
    fi
    exit $SCRETURN
else
    echo The service was not stopped and could not be deleted
    exit 1
fi
