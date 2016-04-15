PUSHD  d:\stp\apache-httpd-2.4.10
d:\stp\apache-httpd-2.4.10\bin\httpd -k install -n Apache2.4 -f d:\stp\app\data\httpd\httpd.conf
CMD /C SC config Apache2.4 DisplayName= "Apache Apache2.4"
POPD