#!/bin/bash

STP_EXIT_CODE_NO_SUCH_SERVICE=123
STP_EXIT_CODE_TIMED_OUT=124
STP_EXIT_CODE_ABNORMAL_SUCCESS=126
STP_EXIT_CODE_NO_OP=127
STP_EXIT_CODE_SUCCESS=0
STP_EXIT_CODE_FAILED=1

if [ "$1" = "" -o "$2" = "" ]; then
    echo $0 not invoked with service name or instances folder path 
    exit $STP_EXIT_CODE_NO_OP;
fi
export JVMINST=`sc queryex $1 | head -1 | awk '{ sub(/:/,"",$4); print $4 }'`
if [ "$JVMINST" = "1060" ]; then
    echo Service $1 not installed on server, continuing with invoke 
else
    echo Service $1 already exists 
    exit $STP_EXIT_CODE_FAILED
fi
cygstart $2/invokeWS.bat 
for (( c=1; c<=5; c++ ))
do
    /usr/bin/sleep 1
done

export JVMINST=`sc queryex $1 | head -1 | awk '{ sub(/:/,"",$4); print $4 }'`
if [ "$JVMINST" = "1060" ]; then
    echo Failed to install service $1 
    exit $STP_EXIT_CODE_FAILED
fi
echo Invoke of service $1 was successful 
exit $STP_EXIT_CODE_SUCCESS
