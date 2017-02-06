package com.cerner.jwala.service.webserver;

import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.service.webserver.component.WebServerStateSetterWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

/**
 * Handles periodic retrieval of all web server states. This handler is defined as a Spring bean in conjunction
 * with Spring's @Scheduled annotation defined for its execution method.
 * <p/>
 * Created by Jedd Cuison on 6/16/2015.
 */
public class WebServerStateRetrievalScheduledTaskHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebServerStateRetrievalScheduledTaskHandler.class);
    private final WebServerService webServerService;
    private final WebServerStateSetterWorker webServerStateSetterWorker;
    private boolean enabled;

    public WebServerStateRetrievalScheduledTaskHandler(final WebServerService webServerService,
                                                       final WebServerStateSetterWorker webServerStateSetterWorker) {
        this.webServerService = webServerService;
        this.webServerStateSetterWorker = webServerStateSetterWorker;
    }

    public WebServerStateRetrievalScheduledTaskHandler(final WebServerService webServerService,
                                                       final WebServerStateSetterWorker webServerStateSetterWorker,
                                                       final boolean enabled) {
        this.webServerService = webServerService;
        this.webServerStateSetterWorker = webServerStateSetterWorker;
        this.enabled = enabled;
    }

    @Scheduled(fixedDelayString = "${ping.webServer.period.millis}")
    public void execute() {

        if (!isEnabled()) {
            return;
        }

        final List<WebServer> webServers = webServerService.getWebServersPropagationNew();
        LOGGER.debug("# of web servers to ping = {}", webServers.size());
        for (final WebServer webServer : webServers) {
            webServerStateSetterWorker.pingWebServer(webServer);
        }

    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}