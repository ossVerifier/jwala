package com.siemens.cto.aem.service.configuration.lifecycle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.integration.endpoint.SourcePollingChannelAdapter;

/**
 * Listen to events and eagerly initialize the heart-beat code
 */
public class HeartbeatStartupLifecycleListener implements ApplicationListener<ApplicationEvent>
{
    private final static Logger LOGGER = LoggerFactory.getLogger(HeartbeatStartupLifecycleListener.class); 

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        
        if (event instanceof ContextStartedEvent) {
            ContextStartedEvent ctxSE = (ContextStartedEvent)event;
            startHeartbeats(false, ctxSE.getApplicationContext(), "Heartbeat: Context Started - Starting Background Process");
        }  else if (event instanceof ContextRefreshedEvent) {
            ContextRefreshedEvent ctxRE = (ContextRefreshedEvent)event;
            startHeartbeats(false, ctxRE.getApplicationContext(), "Heartbeat: Context Refreshed - Starting Background Process");
        }
    }
    
    private void startHeartbeats(boolean doJvm, ApplicationContext appCtx, String eventMessage) {
    	boolean failed = false;
    	
    	if(doJvm) {
            try {
                SourcePollingChannelAdapter jvmInitiator = appCtx.getBean("jvmStateInitiator", SourcePollingChannelAdapter.class);
                jvmInitiator.start();            
            } catch(NoSuchBeanDefinitionException e) {
                LOGGER.error("Could not start JVM reverse heartbeat", e);
                failed = true;
            }
    	}

        if(!failed) {
        	LOGGER.info(eventMessage);
        }
    }
}
