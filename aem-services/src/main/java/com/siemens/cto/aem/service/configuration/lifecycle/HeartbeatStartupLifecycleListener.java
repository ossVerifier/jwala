package com.siemens.cto.aem.service.configuration.lifecycle;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.integration.endpoint.SourcePollingChannelAdapter;
/**
 * Listen to events and eagerly initialize the heart-beat code
 */
public class HeartbeatStartupLifecycleListener implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event instanceof ContextRefreshedEvent) {
            ContextRefreshedEvent ctxRE = (ContextRefreshedEvent)event;
            SourcePollingChannelAdapter jvmInitiator = ctxRE.getApplicationContext().getBean("jvmStateInitiator", SourcePollingChannelAdapter.class);
            jvmInitiator.start();
            SourcePollingChannelAdapter webServerStateInitiator = ctxRE.getApplicationContext().getBean("webServerStateInitiator", SourcePollingChannelAdapter.class);
            webServerStateInitiator.start();
        }      
    }
}
