<%
    def warFileName 
    if(webApp.warPath != null) {
        warFileName = new File(webApp.warPath).getName();
    } else { 
        warFileName = 'cluster.war'
    }
%><Context antiResourceLocking="false"
  privileged="true"
  docBase="\${STP_HOME}/siemens/webapps/${warFileName}"
  path="${webApp.webAppContext}"  unpackWAR="false"  antiJARLocking="true">
</Context>
