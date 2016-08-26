#!/bin/bash

# return codes
STP_EXIT_CODE_NO_SUCH_SERVICE=123
STP_EXIT_CODE_TIMED_OUT=124
STP_EXIT_CODE_ABNORMAL_SUCCESS=126
STP_EXIT_CODE_NO_OP=127
STP_EXIT_CODE_SUCCESS=0
STP_EXIT_CODE_FAILED=1

if [ "$1" = "" ]; then
    /usr/bin/echo $0 not invoked with service name
    exit $STP_EXIT_CODE_NO_OP;
fi

export JVMINST=`sc queryex $1 | /usr/bin/head -1 | /usr/bin/awk '{ sub(/:/,"",$4); print $4 }'`
if [ "$JVMINST" = "1060" ]; then
    /usr/bin/echo Service $1 not installed on server
    sc queryex $1
    exit $STP_EXIT_CODE_NO_SUCH_SERVICE
else
    sc start $1 > /dev/null
    if [ $? -ne 0 ]; then
        export SCRESULT=`sc start $1 | /usr/bin/head -1 | /usr/bin/awk '{ sub(/:/,"",$4); print $4 }'`
        if [ "$SCRESULT" = "1056" ]; then
            /usr/bin/echo Service $1 already started.
            exit $STP_EXIT_CODE_ABNORMAL_SUCCESS
        fi
        /usr/bin/echo $1 failed to start and was not already started
        exit $STP_EXIT_CODE_FAILED
    else
        exit $STP_EXIT_CODE_SUCCESS
    fi
fi
