package com.cerner.jwala.service.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

/**
 * The application startup listener that checks for upgrades.
 *
 * The initial use case for this class was to check for backwards compatibility for the JDK media. The addition
 * of the JDK media to the JVM configuration introduced a dependency that needs to be fulfilled in order for the
 * JVM generation to work. For deployed instances of the application that are not configured with the JDK media, this
 * startup listener will configure the application with a default JDK for the JVMs.
 *
 */
public class ApplicationContextListener {

    private final static Logger LOGGER = LoggerFactory.getLogger(ApplicationContextListener.class);

    @EventListener
    public void handleEvent(ApplicationEvent event) {
        LOGGER.info("Received application event {}", event);

        if (!(event instanceof ContextRefreshedEvent)) {
            LOGGER.debug("Expecting ContextRefreshedEvent. Skipping.");
            return;
        }

        ContextRefreshedEvent crEvent = (ContextRefreshedEvent) event;
        final ApplicationContext applicationContext = crEvent.getApplicationContext();
        if (null == applicationContext){
            LOGGER.debug("Expecting non-null ApplicationContext. Skipping.");
            return;
        }
        if (null == applicationContext.getParent()){
            LOGGER.debug("Expecting non-null ApplicationContext parent. Skipping.");
            return;
        }

        processUpgradeEvent();
    }

    private void processUpgradeEvent() {
        LOGGER.info("Begin upgrade process.");
    }
}
