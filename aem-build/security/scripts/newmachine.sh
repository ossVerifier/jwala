#!/bin/sh
echo Generating a new SSL certificate signed by EHS Corporate. First step, we make a key
openssl genrsa 2048 > $1.key
echo Finished generating $1.key 
./gencsr.sh $1 
