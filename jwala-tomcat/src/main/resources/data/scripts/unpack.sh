#!/bin/bash

JWALA_EXIT_CODE_NO_SUCH_SERVICE=123
JWALA_EXIT_CODE_TIMED_OUT=124
JWALA_EXIT_CODE_ABNORMAL_SUCCESS=126
JWALA_EXIT_CODE_NO_OP=127
JWALA_EXIT_CODE_SUCCESS=0
JWALA_EXIT_CODE_FAILED=1

echo $1
echo $2
echo $3
echo $4

if [ "$1" = "" -o "$2" = ""  -o "$3" = "" -o "$4" = "" ]; then
    echo $0 not invoked with web apps folder or or java home or name of the app
    exit $JWALA_EXIT_CODE_NO_OP;
fi

export FILE_EXT=$(echo $3 |awk -F . '{if (NF>1) {print $NF}}')

echo $FILE_EXT

export BASENAME_BINARY=`basename $3 .$FILE_EXT`

echo BASENAME_BINARY $BASENAME_BINARY

if [ "$4" = "true" ]; then
   rm -r $1/$BASENAME_BINARY
fi

cd $1
if [ -d "$BASENAME_BINARY" ]; then
    mv $BASENAME_BINARY $BASENAME_BINARY.`/usr/bin/date +%Y%m%d_%H%M%S`
fi

mkdir $BASENAME_BINARY    
cd $BASENAME_BINARY
$2/bin/jar xf $1/$3

exit $JWALA_EXIT_CODE_SUCCESS
