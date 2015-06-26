#!/bin/bash

STP_EXIT_CODE_NO_SUCH_SERVICE=123
STP_EXIT_CODE_TIMED_OUT=124
STP_EXIT_CODE_ABNORMAL_SUCCESS=126
STP_EXIT_CODE_NO_OP=127
STP_EXIT_CODE_SUCCESS=0
STP_EXIT_CODE_FAILED=1

if [ "$1" = "" -o "$2" = "" ]; then
    echo $0 not invoked with service name or time out in seconds
    exit $STP_EXIT_CODE_NO_OP;
fi

export JVMINST=`sc queryex $1 | head -1 | awk '{ sub(/:/,"",$4); print $4 }'`
if [ "$JVMINST" = "1060" ]; then
    echo Service $1 not installed on server
    sc queryex $1
    exit $STP_EXIT_CODE_NO_SUCH_SERVICE
else
    sc start $1 > /dev/null
    if [ $? -ne 0 ]; then
        export SCRESULT=`sc start $1 | head -1 | awk '{ sub(/:/,"",$4); print $4 }'`
        if [ "$SCRESULT" = "1056" ]; then
            echo Service $1 already started.
            exit $STP_EXIT_CODE_ABNORMAL_SUCCESS
        fi
        exit $STP_EXIT_CODE_FAILED
    else
        for (( c=1; c<=$2; c++ ))
        do
            /usr/bin/sleep 1
            export SCRESULT=`sc queryex $1 | grep STATE | awk '{ print $4 }'`
            if [ "$SCRESULT" = "RUNNING" ]; then
                exit $STP_EXIT_CODE_SUCCESS
            fi
        done
        exit $STP_EXIT_CODE_TIMED_OUT
    fi
fi
