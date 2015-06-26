package com.siemens.cto.aem.service.webserver;

import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.util.List;

/**
 * Handles periodic retrieval of all web server states. This handler is defined as a Spring bean in conjunction
 * with Spring's @Scheduled annotation defined for its execution method.
 *
 * Created by Z003BPEJ on 6/16/2015.
 */
public class WebServerStateRetrievalScheduledTaskHandler {
    private final WebServerService webServerService;
    private final WebServerStateSetterWorker webServerStateSetterWorker;
    private boolean enabled;

    public WebServerStateRetrievalScheduledTaskHandler(final WebServerService webServerService,
                                                       final WebServerStateSetterWorker webServerStateSetterWorker) {
        this.webServerService = webServerService;
        this.webServerStateSetterWorker = webServerStateSetterWorker;
    }

    @Scheduled(fixedDelayString = "${ping.webServer.period.millis}")
    public void execute() throws IOException {
        if (isEnabled()) {
            final List<WebServer> webServers = webServerService.getWebServers(PaginationParameter.all());

            for (WebServer webServer : webServers) {
                webServerStateSetterWorker.pingWebServer(webServer);
            }
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}