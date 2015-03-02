#!/bin/sh
echo Next step - generate a certificate signing request \(CSR\)

echo US>csrdata.txt
echo Pennsylvania>>csrdata.txt
echo Malvern>>csrdata.txt
echo Siemens AG>>csrdata.txt
echo Health Services>>csrdata.txt
echo $1.usmlvv1d0a.smshsc.net>>csrdata.txt
echo peter.horsfield@siemens.com>>csrdata.txt
echo >>csrdata.txt
echo >>csrdata.txt

openssl req -new -key $1.key -out $1.csr < csrdata.txt

openssl req -subject < $1.csr

rm csrdata.txt 

echo NOW GO HERE TO SUBMIT YOUR CSR: 
echo http://mlvv103a.usmlvv1d0a.smshsc.net/certsrv/certrqxt.asp
echo You need a Web Server certificate
echo You need Base64
echo You need a P7B Certificate Chain
echo Save it here as certnew.p7b
echo Then run ./processp7b.sh $1
