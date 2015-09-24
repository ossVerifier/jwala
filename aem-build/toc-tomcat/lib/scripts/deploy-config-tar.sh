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
export BACKUP_DATE=`/usr/bin/date +%Y%m%d_%H%M%S`
export JVM_INSTANCE=`/usr/bin/basename $2`
cd $2/.. 

# back up current jvm directory
/usr/bin/mv $2 $2.$BACKUP_DATE
/usr/bin/mkdir $2

# extract the new configuration files
/usr/bin/tar -xf $TAR_FILE 
/usr/bin/rm $TAR_FILE 
exit 0
