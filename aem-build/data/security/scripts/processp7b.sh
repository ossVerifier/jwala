#!/bin/sh

echo This script converts certnew.p7b obtained from EHS Corporate
echo Into something for use with Tomcat and Apache
openssl pkcs7 -print_certs -in certnew.p7b -out $1.cer
echo Wrote $1.cer for use in Apache, Tomcat

