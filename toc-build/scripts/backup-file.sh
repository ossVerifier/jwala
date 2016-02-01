#!/bin/bash

if [ "$1" = "" -o "$2" = "" ]; then
    /usr/bin/echo $0 not invoked with source file path or target file location.
    exit $STP_EXIT_CODE_NO_OP
fi
export SRC_FILE=`/usr/bin/cygpath $1`
export DEST_FILE=`/usr/bin/cygpath $2`

/usr/bin/echo /usr/bin/cp $SRC_FILE $DEST_FILE
/usr/bin/cp $SRC_FILE $DEST_FILE

exit $?
