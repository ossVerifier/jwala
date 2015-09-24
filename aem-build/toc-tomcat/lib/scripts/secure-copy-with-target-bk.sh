#!/bin/bash

###############################################################################
# Description:
#
# Copies a file via ssh to a specified host. If the file exists, 
# it's backed up first before the file is copied to the target location. 
#
# Usage:
#
# secure-copy-with-target-bk [source file] [user name] [target host] [target file location]
#
# Command Line Arguments:
#
# $1 - source file path
# $2 - ssh user name
# $3 - ssh target host
# $4 - target file location
###############################################################################

STP_EXIT_CODE_NO_OP=127
STP_EXIT_CODE_SUCCESS=0

if [ "$1" = "" -o "$2" = "" -o "$3" = "" -o "$4" = "" ]; then
    /usr/bin/echo $0 not invoked with source file path, ssh username, ssh target host, or target file location.
    exit $STP_EXIT_CODE_NO_OP
fi
export SRC_FILE=`/usr/bin/cygpath $1`
export DEST_FILE=`/usr/bin/cygpath $4`
export LOCALHOST=`/usr/bin/hostname | /usr/bin/tr [:upper:] [:lower:]`
export DEST_HOST=`/usr/bin/echo $3 | /usr/bin/tr [:upper:] [:lower:]`
if [ "$LOCALHOST" = "$DEST_HOST" ]; then
    /usr/bin/cp $DEST_FILE $DEST_FILE.`/usr/bin/date +%Y%m%d_%H%M%S`
    /usr/bin/cp $SRC_FILE $DEST_FILE
else
	export DEST_FILE_BACKUP_DATE=`/usr/bin/date +%Y%m%d_%H%M%S`
    /usr/bin/scp $2@$3:$DEST_FILE $DEST_FILE.$DEST_FILE_BACKUP_DATE
    /usr/bin/scp $DEST_FILE.$DEST_FILE_BACKUP_DATE $2@$3:`/usr/bin/dirname $DEST_FILE`
    /usr/bin/rm $DEST_FILE.$DEST_FILE_BACKUP_DATE
    /usr/bin/scp $SRC_FILE $2@$3:$DEST_FILE
fi

exit $?