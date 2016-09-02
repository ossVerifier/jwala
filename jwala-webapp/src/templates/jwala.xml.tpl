<Context docBase="${deployPath}/${warName}">

    <Loader className="org.apache.catalina.loader.VirtualWebappLoader"
            virtualClasspath="\\{STP_HOME}/app/data/jwala/conf"/>

    <Listener className="com.cerner.jwala.listener.H2LifecycleListener"
              tcpServerParam="-tcpPort,9094,-tcpAllowOthers,-baseDir,${catalina.base}\\data\\db"
              webServerParam="-webSSL,-webPort,8084,-webAllowOthers"/>

    <Resource name="jdbc/jwala-xa"
          auth="Container"
          type="javax.sql.DataSource"
          factory="org.apache.tomcat.jdbc.pool.DataSourceFactory"
		  driverClassName="org.h2.Driver"
		  uniqueResourceName="jwala_datasource"
		  minIdle="10"
          maxActive="150"
          username="sa"
          password=""
          url="jdbc:h2:tcp://localhost:9094/jwala;LOCK_MODE=0"/>

    <ResourceLink name="jms/jwala-cf" global="jms/jwala-cf" type="com.tibco.tibjms.TibjmsConnectionFactory" />

	<Resource name="jms/jwala-status"
          auth="Container"
          factory="org.apache.naming.factory.BeanFactory"
          type="com.tibco.tibjms.TibjmsTopic"
          address="${jmsStatusAddr}"/>

    <Resource name="jms/jwala-state-notification"
          auth="Container"
          factory="org.apache.naming.factory.BeanFactory"
          type="com.tibco.tibjms.TibjmsTopic"
          address="${jmsStateNotificationAddr}"/>

</Context>
