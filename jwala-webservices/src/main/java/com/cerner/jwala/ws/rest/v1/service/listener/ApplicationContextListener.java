package com.cerner.jwala.ws.rest.v1.service.listener;

import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

/**
 * Created on 1/3/2017.
 */
@Component
public class ApplicationContextListener {

    private final static Logger LOGGER = LoggerFactory.getLogger(ApplicationContextListener.class);

    @EventListener
    public void handleEvent(ApplicationEvent event) {
        if (event instanceof ContextRefreshedEvent) {
            LOGGER.info("Received application event {}", event);

            GroovyShell groovyShell = new GroovyShell();

            File groovyFile = new File("../data/upgrade/upgrade-test.groovy");
            LOGGER.info("Looking for upgrade file {}", groovyFile.getAbsolutePath());
            if (!groovyFile.exists()){
                LOGGER.info("No upgrade file found. Skipping upgrade process.");
                return;
            }

            try {
                Script groovyScript = groovyShell.parse(groovyFile);
                Object result = groovyScript.run();
                LOGGER.info("Upgrade result: {}", result);
            } catch (IOException e) {
                LOGGER.error("Unable to parse upgrade groovy script", e);
            }
        }
    }
}
