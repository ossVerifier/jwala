#!/bin/bash

STP_EXIT_CODE_NO_OP=127
STP_EXIT_CODE_SUCCESS=0

if [ "$1" = "" -o "$2" = "" -o "$3" = "" -o "$4" = "" ]; then
    /usr/bin/echo $0 not invoked with source file path, ssh username, ssh target host, or target file location.
    exit $STP_EXIT_CODE_NO_OP
fi
export SRC_FILE=`/usr/bin/cygpath $1`
export DEST_FILE=`/usr/bin/cygpath $4`
/usr/bin/scp $2@$3:$DEST_FILE $2@$3:$DEST_FILE.`/usr/bin/date +%Y%m%d_%H%M%S`
/usr/bin/scp $SRC_FILE $2@$3:$DEST_FILE
exit $?