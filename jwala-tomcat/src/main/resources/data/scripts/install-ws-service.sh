#!/bin/bash

JWALA_EXIT_CODE_NO_SUCH_SERVICE=123
JWALA_EXIT_CODE_TIMED_OUT=124
JWALA_EXIT_CODE_ABNORMAL_SUCCESS=126
JWALA_EXIT_CODE_NO_OP=127
JWALA_EXIT_CODE_SUCCESS=0
JWALA_EXIT_CODE_FAILED=1

if [ "$1" = "" -o "$2" = "" ]; then
    /usr/bin/echo $0 not invoked with service name or instances folder path
    exit $JWALA_EXIT_CODE_NO_OP;
fi
export JVMINST=`sc queryex $1 | head -1 | awk '{ sub(/:/,"",$4); print $4 }'`
if [ "$JVMINST" = "1060" ]; then
    echo Service $1 not installed on server, continuing with invoke
else
    /usr/bin/echo Service $1 already exists
    exit $JWALA_EXIT_CODE_FAILED
fi
$2/install_serviceWS.bat
if [ "$EXIT_CODE" -ne "0" ]; then
  /usr/bin/echo Failed to install service $1
  exit $JWALA_EXIT_CODE_FAILED
fi

for (( c=1; c<=5; c++ ))
do
    /usr/bin/sleep 1
done

export JVMINST=`sc queryex $1 | head -1 | awk '{ sub(/:/,"",$4); print $4 }'`
if [ "$JVMINST" = "1060" ]; then
    /usr/bin/echo Failed to install service $1
    exit $JWALA_EXIT_CODE_FAILED
fi
/usr/bin/echo Invoke of service $1 was successful
exit $JWALA_EXIT_CODE_SUCCESS
