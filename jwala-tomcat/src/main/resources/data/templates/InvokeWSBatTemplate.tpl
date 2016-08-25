PUSHD  D:\stp\apache-httpd-2.4.20
D:\stp\apache-httpd-2.4.20\bin\httpd -k install -n ${webServer.name} -f ${webServer.httpConfigFile.path}
CMD /C SC config ${webServer.name} DisplayName= "Apache ${webServer.name}"
POPD
