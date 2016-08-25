package com.cerner.jwala.lifecyclelistener;

import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.LifecycleState;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A listener that start H2 when Tomcat is starting and stops h2 when Tomcat is stopping
 */
public class H2LifecycleListener implements ServletContextListener, LifecycleListener {

    private final static Logger LOGGER = LoggerFactory.getLogger(H2LifecycleListener.class);

    @Override
    public void lifecycleEvent(final LifecycleEvent event) {
        final LifecycleState lifecycleState = event.getLifecycle().getState();
        if (LifecycleState.STARTING_PREP.equals(lifecycleState) || LifecycleState.STARTING.equals(lifecycleState)) {
            LOGGER.info("Initializing H2 on Tomcat lifecyle: {}", lifecycleState.toString());
            H2LifecycleService.INSTANCE.startH2DatabaseServer();
        } else if (LifecycleState.DESTROYING.equals(lifecycleState)) {
            LOGGER.info("Destroying H2 on Tomcat lifecyle: {}", lifecycleState.toString());
            H2LifecycleService.INSTANCE.stopH2DatabaseServer();
        }
    }

    @Override
    public void contextInitialized(final ServletContextEvent sce) {
        LOGGER.info("Initializing H2 on Servlet Context Initialization");
        H2LifecycleService.INSTANCE.startH2DatabaseServer();
    }

    @Override
    public void contextDestroyed(final ServletContextEvent sce) {
        LOGGER.info("Destroying H2 on Servlet Context Destruction");
        H2LifecycleService.INSTANCE.stopH2DatabaseServer();
    }
}