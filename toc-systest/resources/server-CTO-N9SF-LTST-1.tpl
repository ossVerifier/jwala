<%    def rmiRegistryPort = jvm.shutdownPort+ 40000
      def rmiServerPort = jvm.shutdownPort + 40001
      def jvmName = jvm.jvmName.replaceAll(" ", "")
%><!--
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
<Server port="${jvm.shutdownPort}" shutdown="notused">
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
  <Listener className="com.siemens.cto.infrastructure.atomikos.AtomikosLifecycleListener" />
  <Listener className="com.siemens.cto.infrastructure.report.tomcat.ReportingLifeCycleListener"
            id="${jvm.id.id}"
            instanceId="${jvm.id.id}"
            type="JVM"
      jmsConnectionFactory="java:/jms/toc-cf"
      jmsDestination="java:/jms/toc-status"
            jmsTtl="60"
            jmsTtlUnit="SECONDS"
            schedulerDelayInitial="30"
            schedulerDelaySubsequent="30"
            schedulerDelayShutdown="30"
            schedulerDelayUnit="SECONDS"
      schedulerThreadCount="1"
      schedulerThreadNamePrefix="JMS Reporting Thread"/>
  <Listener className="org.apache.catalina.mbeans.JmxRemoteLifecycleListener"
            rmiRegistryPortPlatform="${rmiRegistryPort}" rmiServerPortPlatform="${rmiServerPort}" />
  <Listener className="com.siemens.cto.infrastructure.report.tomcat.FailFastLifeCycleListener"
            jmsConnectionFactory="java:/jms/toc-cf"
            jmsDestination="java:/jms/toc-status"
            systemExitCodeOnError="125"
            testMessageTimeToLiveMs="125"
            timeToWaitForJmsResponseMs="5000"
			exitOnError="false" />

  <Listener className="com.siemens.cto.infrastructure.report.tomcat.FastJmsStartLifeCycleListener" />

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
              address="tocStatus-localhost"/>

    <Resource auth="Container"
              name="jms/toc-cf"
              factory="org.apache.naming.factory.BeanFactory"
              type="com.tibco.tibjms.TibjmsConnectionFactory"
              serverUrl="ssl://localhost:7243"
              userName="admin"
              userPassword="\${encrypted:9hQ4KpxpM8e/VX3KkT2VYg==}"
              connAttemptCount="2"
              connAttemptDelay="100"
              reconnAttemptCount="100"
              reconnAttemptDelay="1000"
              SSLEnableVerifyHost="true"
              SSLEnableVerifyHostName="false"
              SSLTrustedCertificate="\${STP_HOME}\\siemens\\data\\security\\ems\\ctorootca.pem" />
  </GlobalNamingResources>


  <!-- A "Service" is a collection of one or more "Connectors" that share
       a single "Container" Note:  A "Service" is not itself a "Container",
       so you may not define subcomponents such as "Valves" at this level.
       Documentation at /docs/config/service.html
   -->
  <!-- Soarian Tomcat Platform Service
       Features: SSL enabled, AJP disabled, HTTP disabled
       Exploded apps: stpapps
       Archived apps: by context.xmls in conf/stp/localhost
       -->

  <Service name="stp">
    <!-- Define a SSL HTTP/1.1 Connector.  This connector uses the JSSE configuration, when using APR, the
         connector should be using the OpenSSL style configuration described in the APR documentation

        Defines Peter's APR + JSSE compatible port
        STP Features:
        Compression: Forced on
        Compressable types: text/html,text/xml,text/plain,application/json
        compressionMinSize: 2048 (default)
        threadPriority: 1 above Java NORM_PRIORITY - 6
    -->

    <Connector
      port="${jvm.httpsPort}"
      SSLCertificateFile="\${catalina.base}/../../data/security/id/${jvm.hostName}.cer"
      SSLCertificateKeyFile="\${catalina.base}/../../data/security/id/${jvm.hostName}.key"
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
      sslProtocol="TLS1.2"
      compressableMimeTypes="text/html,text/xml,text/plain,application/json"
      compressionMinSize="2048"
      compression="force"
      threadPriority="6" />

     <!-- An Engine represents the entry point (within Catalina) that processes
     every request.  The Engine implementation for Tomcat stand alone
     analyzes the HTTP headers included with the request, and passes them
     on to the appropriate Host (virtual host).
     Documentation at /docs/config/engine.html -->

     <!-- The STP Engine is the normal standalone Tomcat host
          Warning: potential name conflict on jvmRoute -->
    <Engine name="stp" defaultHost="localhost" jvmRoute="${jvm.jvmName}">

      <!-- Host Features: Standard Host
           AppBase: stpapps/
           Unpacking: no
           Auto deploy: yes
           Deploy .war/META-INF/context.xml: no
           Customized Error Reports: no
      -->
      <Host name="localhost"
            appBase="stpapps"
            unpackWARs="false"
            autoDeploy="true"
            deployXML="false"
            errorReportValveClass="org.apache.catalina.valves.ErrorReportValve">

        <!-- Attempt to ensure we 'could' identify ourselves properly over SSL -->
        <Alias>${jvm.hostName}</Alias>
        <!-- Unsupported: <Alias>${webServerName}.webServerDomain</Alias> -->

        <!-- Access log processes all example.
             Documentation at: /docs/config/valve.html
             STP Features: Standard Access Log
             Daily rotating: yes
             Status request logging can be disabled by adding attribute "status" to the ServletRequest.
             -->
        <Valve className="org.apache.catalina.valves.AccessLogValve" directory="logs"
               prefix="stp_access_log." suffix=".txt"
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
         Define a non-SSL HTTP/1.1 Connector on port ${httpPort}
    -->
    <Connector port="${jvm.httpPort}" protocol="HTTP/1.1"
               connectionTimeout="20000"
               redirectPort="${jvm.httpsPort}" />

    <!-- Define an AJP 1.3 Connector on port ${ajpPort} -->
    <Connector port="${jvm.ajpPort}" protocol="AJP/1.3" redirectPort="${jvm.httpsPort}" />


    <!-- An Engine represents the entry point (within Catalina) that processes
         every request.  The Engine implementation for Tomcat stand alone
         analyzes the HTTP headers included with the request, and passes them
         on to the appropriate Host (virtual host).
         Documentation at /docs/config/engine.html -->

    <!-- You should set jvmRoute to support load-balancing via AJP ie :
    <Engine name="Catalina" defaultHost="localhost" jvmRoute="jvm1">
    -->
    <Engine name="Catalina" defaultHost="localhost" jvmRoute="${jvmName}">

      <!-- Host Features: Standard Host
           AppBase: stpapps/
           Unpacking: no
           Auto deploy: yes
           Deploy .war/META-INF/context.xml: no
           Customized Error Reports: no
      -->

      <Cluster className="org.apache.catalina.ha.tcp.SimpleTcpCluster"
                      channelSendOptions="6" channelStartOptions="3">

      <Manager className="org.apache.catalina.ha.session.DeltaManager"
        expireSessionsOnown="false"
        notifyListenersOnReplication="true"
        sessionAttributeFilter="^(userName|)\$"
        recordAllActions="true"/>

      <Channel className="org.apache.catalina.tribes.group.GroupChannel">
        <Receiver className="org.apache.catalina.tribes.transport.nio.NioReceiver"
          address="localhost" autoBind="0"
          port="11000"
          securePort="11001"
          selectorTimeout="100"
          maxThreads="6"/>

        <Sender className="org.apache.catalina.tribes.transport.ReplicationTransmitter">
          <Transport className="org.apache.catalina.tribes.transport.nio.PooledParallelSender"/>
        </Sender>
        <Interceptor className="org.apache.catalina.tribes.group.interceptors.TcpFailureDetector"/>
        <Interceptor className="org.apache.catalina.tribes.group.interceptors.MessageDispatch15Interceptor"/>
        <Interceptor className="org.apache.catalina.tribes.group.interceptors.StaticMembershipInterceptor">
        <!-- 4 bytes for each: org (cto=1), env (d0a-ltst = 1), host name (4439), port (listenport - 11000) -->
        <!-- Point to JVM on same node only for now. -->
          <Member className="org.apache.catalina.tribes.membership.StaticMember"
            port="11010"
            securePort="11011"
            host="localhost" />
        </Interceptor>
        <!-- Statistics -->
        <Interceptor className="org.apache.catalina.tribes.group.interceptors.ThroughputInterceptor"/>
      </Channel>

      <Valve className="org.apache.catalina.ha.tcp.ReplicationValve"
        filter=".*\\.gif|.*\\.js|.*\\.jpeg|.*\\.jpg|.*\\.png|.*\\.htm|.*\\.html|.*\\.css|.*\\.txt"/>

      <ClusterListener className="org.apache.catalina.ha.session.ClusterSessionListener"/>

      </Cluster>

      <!-- Use the LockOutRealm to prevent attempts to guess user passwords via a brute-force attack
      <Realm className="org.apache.catalina.realm.LockOutRealm">
         <Realm className="org.apache.catalina.realm.UserDatabaseRealm" resourceName="UserDatabase"/>
      </Realm>-->

      <Host name="localhost"
            appBase="stpapps"
            unpackWARs="false"
            autoDeploy="true"
            deployXML="false"
            errorReportValveClass="org.apache.catalina.valves.ErrorReportValve">

        <!-- Attempt to ensure we 'could' identify ourselves properly over SSL -->
        <Alias>localhost</Alias>

        <!-- Access log processes all example.
             Documentation at: /docs/config/valve.html
             STP Features: Standard Access Log
             Daily rotating: yes
             Status request logging can be disabled by adding attribute "status" to the ServletRequest.
             -->
        <Valve className="org.apache.catalina.valves.AccessLogValve" directory="logs"
               prefix="stp_access_log." suffix=".txt"
               pattern="common"
               conditionUnless="status" />

      <Valve className="org.apache.catalina.ha.authenticator.ClusterSingleSignOn"
      requireReauthentication="false"
      mapSendOptions="6"
      terminateOnStartFailure="true" />

      </Host>
      </Engine>
    </Service>
</Server>
