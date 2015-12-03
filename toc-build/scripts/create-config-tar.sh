#!/bin/bash

STP_EXIT_CODE_NO_SUCH_SERVICE=123
STP_EXIT_CODE_TIMED_OUT=124
STP_EXIT_CODE_ABNORMAL_SUCCESS=126
STP_EXIT_CODE_NO_OP=127
STP_EXIT_CODE_SUCCESS=0
STP_EXIT_CODE_FAILED=1

if [ "$1" = "" -o "$2" = "" ]; then
    echo $0 not invoked with tar name or folder to tar
    exit $STP_EXIT_CODE_NO_OP;
fi

cd $2/..
/usr/bin/tar -cf $1 `/usr/bin/basename $2`
exit $STP_EXIT_CODE_SUCCESS