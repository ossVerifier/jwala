package com.siemens.cto.aem.service.configuration.lifecycle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
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
        ApplicationContext appCtx;
        String msg;
        
        if (event instanceof ContextStartedEvent) {
            msg = "Heartbeat: Context Started - Starting Background Process";
            ContextStartedEvent ctxSE = (ContextStartedEvent)event;
            appCtx = ctxSE.getApplicationContext();
        }  else if (event instanceof ContextRefreshedEvent) {
            msg = "Heartbeat: Context Refreshed - Starting Background Process";
            ContextRefreshedEvent ctxRE = (ContextRefreshedEvent)event;
            appCtx = ctxRE.getApplicationContext();
        } else if (event instanceof ContextStoppedEvent) {
            msg = "Heartbeat: Context Stopped - Stopping Background Process";
            ContextStoppedEvent ctxSE = (ContextStoppedEvent)event;
            appCtx = ctxSE.getApplicationContext();
        } else if (event instanceof ContextClosedEvent) {
            msg = "Heartbeat: Context Closed - Stopping Background Process";
            ContextClosedEvent ctxCE = (ContextClosedEvent)event;
            appCtx = ctxCE.getApplicationContext();
        } else { 
            // not handled.
            msg = "";
            appCtx = null;
        }
        
        if(appCtx != null) {
            msg = msg  + ((event.getSource() != null) ? ("; source=" + event.getSource().toString()) : "");
            LOGGER.info(msg);
            SourcePollingChannelAdapter jvmInitiator = appCtx.getBean("jvmStateInitiator", SourcePollingChannelAdapter.class);
            jvmInitiator.start();
            SourcePollingChannelAdapter webServerStateInitiator = appCtx.getBean("webServerStateInitiator", SourcePollingChannelAdapter.class);
            webServerStateInitiator.start();
        }
    }
}
