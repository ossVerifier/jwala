#!/bin/bash

STP_EXIT_CODE_ABNORMAL_SUCCESS=126
STP_EXIT_CODE_NO_OP=127
STP_EXIT_CODE_SUCCESS=0

if [ "$1" = "" ]; then
    echo $0 not invoked with service name
    exit 125;
fi

export JVMINST=`sc queryex $1 | head -1 | awk '{ sub(/:/,"",$4); print $4 }'`
if [ "$JVMINST" = "1060" ]; then
    echo Service $1 not installed on server
    sc queryex $1
    exit 1060
else 
    sc start $1 > /dev/null
    if [ $? -ne 0 ]; then
        export SCRESULT=`sc start $1 | head -1 | awk '{ sub(/:/,"",$4); print $4 }'`
        if [ "$SCRESULT" = "1056" ]; then
            echo Service $1 already started.
            exit $STP_EXIT_CODE_NO_OP
        fi
        exit $SCRESULT
    fi
    exit $?
fi