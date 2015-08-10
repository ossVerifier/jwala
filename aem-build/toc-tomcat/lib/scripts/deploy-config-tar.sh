#!/bin/bash

STP_EXIT_CODE_NO_SUCH_SERVICE=123
STP_EXIT_CODE_TIMED_OUT=124
STP_EXIT_CODE_ABNORMAL_SUCCESS=126
STP_EXIT_CODE_NO_OP=127
STP_EXIT_CODE_SUCCESS=0
STP_EXIT_CODE_FAILED=1

if [ "$1" = "" -o "$2" = "" -o "$3" = "" ]; then
    echo $0 not invoked with tar name or folder to untar or the data directory location
    exit $STP_EXIT_CODE_NO_OP;
fi

export TAR_FILE=`/usr/bin/basename $1`
export TAR_FILE_BACKUP=$TAR_FILE.`/usr/bin/date +%Y%m%d_%H%M%S`
export JVM_INSTANCE=`/usr/bin/basename $2`
/usr/bin/cp $1 $2/..
cd $2/..

# back up current jvm directory
/usr/bin/tar -cf $TAR_FILE_BACKUP $JVM_INSTANCE --exclude="$JVM_INSTANCE/bin" --exclude="$JVM_INSTANCE/logs" --exclude="$JVM_INSTANCE/stpapps" --exclude="$JVM_INSTANCE/temp" --exclude="$JVM_INSTANCE/webapps" --exclude="$JVM_INSTANCE/work"
/usr/bin/mv $TAR_FILE_BACKUP $3

# extract the new configuration files
/usr/bin/tar -xf $TAR_FILE
/usr/bin/rm $TAR_FILE
exit $STP_EXIT_CODE_SUCCESS