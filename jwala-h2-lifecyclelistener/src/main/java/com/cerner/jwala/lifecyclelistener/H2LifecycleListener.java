package com.cerner.jwala.lifecyclelistener;

import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.LifecycleState;
import org.apache.log4j.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class H2LifecycleListener implements ServletContextListener, LifecycleListener {
    Logger LOG = Logger.getLogger(H2LifecycleListener.class);
    @Override
    public void lifecycleEvent(LifecycleEvent event) {
        LifecycleState lifecycleState = event.getLifecycle().getState();
        if (LifecycleState.STARTING_PREP.equals(lifecycleState) || LifecycleState.STARTING.equals(lifecycleState)) {
            print("Initializing H2 on Tomcat lifecyle: " + lifecycleState.toString());
            H2LifecycleService.INSTANCE.startH2DatabaseServer();
        }
        if (lifecycleState.equals(LifecycleState.DESTROYING)) {
            print("Destroying H2 on Tomcat lifecyle: "  + lifecycleState.toString());
            H2LifecycleService.INSTANCE.stopH2DatabaseServer();
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        print("Initializing H2 on Servlet Context Initialization");
        H2LifecycleService.INSTANCE.startH2DatabaseServer();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        print("Destroying H2 on Servlet Context Destruction");
        H2LifecycleService.INSTANCE.stopH2DatabaseServer();
    }

    public void print(final String content){
        System.out.println(content);
        LOG.debug(content);
    }
}