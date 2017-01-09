#!/bin/bash

JWALA_EXIT_CODE_NO_OP=127
JWALA_EXIT_CODE_SUCCESS=0
JWALA_EXIT_CODE_FAILED=1

cygwin=false
linux=false
case "`uname`" in
CYGWIN*) cygwin=true;;
Linux*) linux=true;;
esac

if [ "$1" = "" -o "$2" = "" ]; then
    echo $0 not invoked with service name or instances folder path
    exit $JWALA_EXIT_CODE_NO_OP;
fi

if $cygwin; then
  export JVMINST=`sc queryex $1 | head -1 | awk '{ sub(/:/,"",$4); print $4 }'`
  if [ "$JVMINST" = "1060" ]; then
      echo Service $1 not installed on server, continuing with invoke
  else
      /usr/bin/echo Service $1 already exists
      exit $JWALA_EXIT_CODE_FAILED
  fi
  $2/install_serviceWS.bat

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
fi

if $linux; then
  # Need to pass $3 for apache home ex: /opt/ctp/apache-httpd-2.4.20, remote.paths.apache.httpd from vars.properties
  sed -e "s/@APACHE_HOME@/${3//\//\\/}/g" -e "s/@HTTPD_CONF@/${2//\//\\/}\\/httpd.conf/g" /linux/httpd-ws-service> $1
  chmod 755 $1
  sudo cp $1 /etc/init.d
  sudo chkconfig --add $1
fi
