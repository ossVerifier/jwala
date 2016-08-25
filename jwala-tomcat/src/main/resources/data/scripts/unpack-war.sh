#!/bin/bash

STP_EXIT_CODE_NO_SUCH_SERVICE=123
STP_EXIT_CODE_TIMED_OUT=124
STP_EXIT_CODE_ABNORMAL_SUCCESS=126
STP_EXIT_CODE_NO_OP=127
STP_EXIT_CODE_SUCCESS=0
STP_EXIT_CODE_FAILED=1

if [ "$1" = "" -o "$2" = ""  -o "$3" = "" ]; then
    echo $0 not invoked with web apps folder or or java home or name of the app
    exit $STP_EXIT_CODE_NO_OP;
fi

export BASENAME_WAR=`basename $3 .war`

cd $1
if [ -d "$BASENAME_WAR" ]; then
    mv $BASENAME_WAR $BASENAME_WAR.`/usr/bin/date +%Y%m%d_%H%M%S`
fi
mkdir $BASENAME_WAR    
cd $BASENAME_WAR
$2/bin/jar xf $1/$3

exit $STP_EXIT_CODE_SUCCESS
