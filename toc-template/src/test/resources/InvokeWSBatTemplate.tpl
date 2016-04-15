PUSHD  d:\\stp\\apache-httpd-2.4.10
d:\\stp\\apache-httpd-2.4.10\\bin\\httpd -k install -n ${webServer.name} -f d:\\stp\\app\\data\\httpd\\httpd.conf
CMD /C SC config ${webServer.name} DisplayName= "Apache ${webServer.name}"
POPD