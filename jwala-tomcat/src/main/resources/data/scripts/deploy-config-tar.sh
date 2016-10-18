#!/bin/bash

JWALA_EXIT_CODE_NO_SUCH_SERVICE=123
JWALA_EXIT_CODE_TIMED_OUT=124
JWALA_EXIT_CODE_ABNORMAL_SUCCESS=126
JWALA_EXIT_CODE_NO_OP=127
JWALA_EXIT_CODE_SUCCESS=0
JWALA_EXIT_CODE_FAILED=1

if [ "$1" = "" -o "$2" = "" -o "$3" = "" ]; then
    /usr/bin/echo $0 not invoked with tar name or folder to untar or the jar executable path
    exit $JWALA_EXIT_CODE_NO_OP;
fi
export JAR_FILE=`/usr/bin/basename $1`
export BACKUP_DATE=`/usr/bin/date +%Y%m%d_%H%M%S`
export JVM_INSTANCE=`/usr/bin/basename $2`
cd $2/.. 

# back up current jvm directory
if [ -d "$2" ]; then
    /usr/bin/mv $2 $2.$BACKUP_DATE
fi
/usr/bin/mkdir $2

# extract the new configuration files
if [ ! -e "$3.exe" ]; then
  /usr/bin/echo JVM version not installed: $3 does not exist on this host
  exit $JWALA_EXIT_CODE_FAILED
fi
$3 xf `cygpath -wa $1`
/usr/bin/rm $1
/usr/bin/echo Deploy of $1 was successful
exit 0
