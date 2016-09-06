<?xml version='1.0' encoding='utf-8'?>
<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at


      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
<!-- Note:  A "Server" is not itself a "Container", so you may not
     define subcomponents such as "Valves" at this level.
     Documentation at /docs/config/server.html
 -->
<Server port="${jvmShutDownPort}" shutdown="SHUTDOWN">
  <!-- Security listener. Documentation at /docs/config/listeners.html
  <Listener className="org.apache.catalina.security.SecurityListener" />
  -->
  <!--APR library loader. Documentation at /docs/apr.html -->
  <Listener className="org.apache.catalina.core.AprLifecycleListener" SSLEngine="on" />
  <!--Initialize Jasper prior to webapps are loaded. Documentation at /docs/jasper-howto.html -->
  <Listener className="org.apache.catalina.core.JasperListener" />
  <!-- Prevent memory leaks due to use of particular java/javax APIs-->
  <Listener className="org.apache.catalina.core.JreMemoryLeakPreventionListener" />
  <Listener className="org.apache.catalina.mbeans.GlobalResourcesLifecycleListener" />
  <Listener className="org.apache.catalina.core.ThreadLocalLeakPreventionListener" />

  <!-- commented out until we have jmx ports in jvm definitions in TOC -->
  <!--Listener className="org.apache.catalina.mbeans.JmxRemoteLifecycleListener"
            rmiRegistryPortPlatform="@toc.jmx.rmiRegistryPortPlatform@" rmiServerPortPlatform="@toc.jmx.rmiServerPortPlatform@" /-->
  
  <!-- Global JNDI resources
       Documentation at /docs/jndi-resources-howto.html
  -->
  <GlobalNamingResources>
    <!-- Editable user database that can also be used by
         UserDatabaseRealm to authenticate users
    -->
    <Resource name="UserDatabase" auth="Container"
              type="org.apache.catalina.UserDatabase"
              description="User database that can be updated and saved"
              factory="org.apache.catalina.users.MemoryUserDatabaseFactory"
              pathname="conf/tomcat-users.xml" />
              
    <Resource name="jms/toc-status"
              auth="Container"
              factory="org.apache.naming.factory.BeanFactory"
              type="com.tibco.tibjms.TibjmsTopic"
              address="@toc.jms.statusTopic@"/>

    <Resource auth="Container"
              name="jms/toc-cf"
              factory="org.apache.naming.factory.BeanFactory"
              type="com.tibco.tibjms.TibjmsConnectionFactory"
              serverUrl="ssl://@tibco.ems.host@:@tibco.ems.port@"
              userName="@tibco.ems.user.name@"
              userPassword="@tibco.ems.password.stpencrypted@"
              connAttemptCount="100"
              connAttemptDelay="1000"
              reconnAttemptCount="100"
              reconnAttemptDelay="1000"
              SSLEnableVerifyHost="true"
              SSLEnableVerifyHostName="false"
              SSLTrustedCertificate="@jwala.security.dir@\\ems\\ctorootca.pem" />
  </GlobalNamingResources>


  <!-- A "Service" is a collection of one or more "Connectors" that share
       a single "Container" Note:  A "Service" is not itself a "Container",
       so you may not define subcomponents such as "Valves" at this level.
       Documentation at /docs/config/service.html
   -->
  <!-- Soarian Tomcat Platform Service 
       Features: SSL enabled, AJP disabled, HTTP disabled 
       Exploded apps: jwalaapps
       Archived apps: by context.xmls in conf/jwala/localhost
       -->

  <Service name="jwala">
    <!-- Define a SSL HTTP/1.1 Connector.  This connector uses the JSSE configuration, when using APR, the
         connector should be using the OpenSSL style configuration described in the APR documentation 

        Defines Peter's APR + JSSE compatible port 
        JWALA Features:
        Compression: Forced on
        Compressable types: text/html,text/xml,text/plain,application/json
        compressionMinSize: 2048 (default)
        threadPriority: 1 above Java NORM_PRIORITY - 6
    -->

    <Connector 
      port="${jvmHttpsPort}" 
      SSLCertificateFile="@jwala.security.dir@\\id\\${jvmHostName}.cer"
      SSLCertificateKeyFile="@jwala.security.dir@\\id\\${jvmHostName}.key"
      SSLEnabled="true" 
      SSLPassword="" 
      acceptCount="100" 
      clientAuth="false" 
      disableUploadTimeout="true" 
      enableLookups="false" 
      keystoreFile="conf/.keystore" 
      maxHttpHeaderSize="8192" 
      maxSavePostSize="-1" 
      maxThreads="150" 
      protocol="HTTP/1.1" 
      scheme="https" 
      secure="true" 
      sslProtocol="TLS"
      compressableMimeTypes="text/html,text/xml,text/plain,application/json"
      compressionMinSize="2048"
      compression="force"
      threadPriority="6" />
        
     <!-- An Engine represents the entry point (within Catalina) that processes
     every request.  The Engine implementation for Tomcat stand alone
     analyzes the HTTP headers included with the request, and passes them
     on to the appropriate Host (virtual host).
     Documentation at /docs/config/engine.html -->
     
     <!-- The JWALA Engine is the normal standalone Tomcat host
          Warning: potential name conflict on jvmRoute -->
    <Engine name="jwala" defaultHost="localhost" jvmRoute="${jvmName}">

      <!-- Host Features: Standard Host
           AppBase: jwalaapps/
           Unpacking: no
           Auto deploy: yes
           Deploy .war/META-INF/context.xml: no
           Customized Error Reports: no
      -->

      <!-- Use the LockOutRealm to prevent attempts to guess user passwords via a brute-force attack -->
      <Realm className="org.apache.catalina.realm.LockOutRealm">
         <Realm className="org.apache.catalina.realm.UserDatabaseRealm" resourceName="UserDatabase"/>
      </Realm>

      <Host name="localhost"  
            appBase="jwalaapps"
            unpackWARs="false"
            autoDeploy="true"
            deployXML="false" 
            errorReportValveClass="org.apache.catalina.valves.ErrorReportValve">

        <!-- Attempt to ensure we 'could' identify ourselves properly over SSL -->  
        <Alias>${jvmHostName}</Alias>
        
        <!-- Access log processes all example.
             Documentation at: /docs/config/valve.html
             JWALA Features: Standard Access Log
             Daily rotating: yes
             Status request logging can be disabled by adding attribute "status" to the ServletRequest.
             -->
        <Valve className="org.apache.catalina.valves.AccessLogValve" directory="logs"
               prefix="jwala_access_log." suffix=".txt"
               pattern="common"
               conditionUnless="status" />

      </Host>      
    </Engine>
    
  </Service>
  
  <!-- Default well-known service 
       Only supports HTTP connections -->
  <Service name="Catalina">

    <!--The connectors can use a shared executor, you can define one or more named thread pools-->
    <!--
    <Executor name="tomcatThreadPool" namePrefix="catalina-exec-"
        maxThreads="150" minSpareThreads="4"/>
    -->


    <!-- A "Connector" represents an endpoint by which requests are received
         and responses are returned. Documentation at :
         Java HTTP Connector: /docs/config/http.html (blocking & non-blocking)
         Java AJP  Connector: /docs/config/ajp.html
         APR (HTTP/AJP) Connector: /docs/apr.html
         Define a non-SSL HTTP/1.1 Connector on port 8090
    -->
    <Connector port="${jvmHttpPort}" protocol="HTTP/1.1"
               connectionTimeout="20000"
               redirectPort="${jvmHttpsPort}" />
    <!-- A "Connector" using the shared thread pool-->
    <!--
    <Connector executor="tomcatThreadPool"
               port="8090" protocol="HTTP/1.1"
               connectionTimeout="20000"
               redirectPort="8091" />
    -->
    <!-- Define an AJP 1.3 Connector on port 8094 -->
    <Connector port="${jvmAjpPort}" protocol="AJP/1.3" redirectPort="${jvmHttpsPort}" />


    <!-- An Engine represents the entry point (within Catalina) that processes
         every request.  The Engine implementation for Tomcat stand alone
         analyzes the HTTP headers included with the request, and passes them
         on to the appropriate Host (virtual host).
         Documentation at /docs/config/engine.html -->

    <!-- You should set jvmRoute to support load-balancing via AJP ie :
    <Engine name="Catalina" defaultHost="localhost" jvmRoute="jvm1">
    -->
    <Engine name="Catalina" defaultHost="localhost" jvmRoute="@toc.jvm.name@">

      <!--For clustering, please take a look at documentation at:
          /docs/cluster-howto.html  (simple how to)
          /docs/config/cluster.html (reference documentation) -->
      <!--
      <Cluster className="org.apache.catalina.ha.tcp.SimpleTcpCluster"/>
      -->

      <!-- Use the LockOutRealm to prevent attempts to guess user passwords
           via a brute-force attack -->
      <Realm className="org.apache.catalina.realm.LockOutRealm">
        <!-- This Realm uses the UserDatabase configured in the global JNDI
             resources under the key "UserDatabase".  Any edits
             that are performed against this UserDatabase are immediately
             available for use by the Realm.  -->
        <Realm className="org.apache.catalina.realm.UserDatabaseRealm"
               resourceName="UserDatabase"/>
      </Realm>

      <Host name="localhost"  appBase="webapps"
            unpackWARs="true" autoDeploy="true">

        <!-- SingleSignOn valve, share authentication between web applications
             Documentation at: /docs/config/valve.html -->
        <!--
        <Valve className="org.apache.catalina.authenticator.SingleSignOn" />
        -->

        <!-- Access log processes all example.
             Documentation at: /docs/config/valve.html
             Note: The pattern used is equivalent to using pattern="common" -->
        <Valve className="org.apache.catalina.valves.AccessLogValve" directory="logs"
               prefix="localhost_access_log." suffix=".txt"
               pattern="%h %l %u %t &quot;%r&quot; %s %b" />

      </Host>
    </Engine>
  </Service>
</Server>
