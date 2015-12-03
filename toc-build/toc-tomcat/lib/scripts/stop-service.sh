#!/bin/bash

STP_EXIT_CODE_NO_SUCH_SERVICE=123
STP_EXIT_CODE_ABNORMAL_SUCCESS=126
STP_EXIT_CODE_NO_OP=127
STP_EXIT_CODE_SUCCESS=0

if [ "$1" = "" -o "$2" = "" ]; then
    echo $0 not invoked with service name and timeout in seconds.
    exit $STP_EXIT_CODE_NO_OP
fi
export JVMINST=`sc queryex $1 | head -1 | awk '{ sub(/:/,"",$4); print $4 }'`
export JVMPID=`sc queryex $1 | grep PID | awk '{ print $3 }'`
if [ "$JVMINST" = "1060" ]; then
    echo Service $1 not installed on server
    sc queryex $1
    exit $STP_EXIT_CODE_NO_SUCH_SERVICE
elif [ "$JVMPID" -ne "0" ]; then
    sc stop $1 > /dev/null
    export SCRETURN=$?
    if [ "$SCRETURN" -ne "0" ]; then
        echo Could not stop service $1 with process id $JVMPID.
        sc stop $1
        exit $SCRETURN
    fi
    for (( c=1; c<=$2; c++ ))
    do
        /usr/bin/sleep 1
        export JVMNEWPID=`sc queryex  $1 | grep PID | awk '{ print $3 }'`
        if [ $JVMNEWPID -ne $JVMPID ]; then
            exit $SCRETURN
        fi
    done
    echo Service $1 with process id $JVMPID terminated.
    ( sc query $1 | tail -8 )
    /usr/bin/kill -9 -f $JVMPID
    exit $STP_EXIT_CODE_SUCCESS
else
    echo The service has not been started.
    exit $STP_EXIT_CODE_ABNORMAL_SUCCESS
fi
