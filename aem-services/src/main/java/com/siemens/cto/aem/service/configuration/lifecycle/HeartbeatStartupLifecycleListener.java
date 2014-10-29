package com.siemens.cto.aem.service.configuration.lifecycle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.integration.endpoint.SourcePollingChannelAdapter;

import com.siemens.cto.aem.domain.model.uri.UriBuilder;

/**
 * Listen to events and eagerly initialize the heart-beat code
 */
public class HeartbeatStartupLifecycleListener implements ApplicationListener<ApplicationEvent>
{
    private final static Logger LOGGER = LoggerFactory.getLogger(HeartbeatStartupLifecycleListener.class); 

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ContextRefreshedEvent) {
            LOGGER.info("Heartbeat: Context Refreshed - Starting Background Process");
            ContextRefreshedEvent ctxRE = (ContextRefreshedEvent)event;
            SourcePollingChannelAdapter jvmInitiator = ctxRE.getApplicationContext().getBean("jvmStateInitiator", SourcePollingChannelAdapter.class);
            jvmInitiator.start();
            SourcePollingChannelAdapter webServerStateInitiator = ctxRE.getApplicationContext().getBean("webServerStateInitiator", SourcePollingChannelAdapter.class);
            webServerStateInitiator.start();
        } else if (event instanceof ContextStoppedEvent) {
            LOGGER.info("Heartbeat: Context Stopped - Starting Background Process");
            ContextStoppedEvent ctxSE = (ContextStoppedEvent)event;
            SourcePollingChannelAdapter jvmInitiator = ctxSE.getApplicationContext().getBean("jvmStateInitiator", SourcePollingChannelAdapter.class);
            jvmInitiator.stop();
            SourcePollingChannelAdapter webServerStateInitiator = ctxSE.getApplicationContext().getBean("webServerStateInitiator", SourcePollingChannelAdapter.class);
            webServerStateInitiator.stop();
        }  else if (event instanceof ContextStartedEvent) {
            LOGGER.info("Heartbeat: Context Started - Starting Background Process");
            ContextStartedEvent ctxSE = (ContextStartedEvent)event;
            SourcePollingChannelAdapter jvmInitiator = ctxSE.getApplicationContext().getBean("jvmStateInitiator", SourcePollingChannelAdapter.class);
            jvmInitiator.start();
            SourcePollingChannelAdapter webServerStateInitiator = ctxSE.getApplicationContext().getBean("webServerStateInitiator", SourcePollingChannelAdapter.class);
            webServerStateInitiator.start();
        }       
    }
}
