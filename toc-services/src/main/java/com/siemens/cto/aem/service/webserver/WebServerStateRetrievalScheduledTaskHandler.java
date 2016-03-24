package com.siemens.cto.aem.service.webserver;

import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.service.webserver.component.WebServerStateSetterWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Handles periodic retrieval of all web server states. This handler is defined as a Spring bean in conjunction
 * with Spring's @Scheduled annotation defined for its execution method.
 * <p/>
 * Created by Z003BPEJ on 6/16/2015.
 */
public class WebServerStateRetrievalScheduledTaskHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebServerStateRetrievalScheduledTaskHandler.class);
    private final WebServerService webServerService;
    private final WebServerStateSetterWorker webServerStateSetterWorker;
    private final Map<Identifier<WebServer>, Future<?>> webServerFutureMap;
    private boolean enabled;

    public WebServerStateRetrievalScheduledTaskHandler(final WebServerService webServerService,
                                                       final WebServerStateSetterWorker webServerStateSetterWorker,
                                                       final Map<Identifier<WebServer>, Future<?>> webServerFutureMap) {
        this.webServerService = webServerService;
        this.webServerStateSetterWorker = webServerStateSetterWorker;
        this.webServerFutureMap = webServerFutureMap;
    }

    public WebServerStateRetrievalScheduledTaskHandler(final WebServerService webServerService,
                                                       final WebServerStateSetterWorker webServerStateSetterWorker,
                                                       final Map<Identifier<WebServer>, Future<?>> webServerFutureMap,
                                                       final boolean enabled) {
        this.webServerService = webServerService;
        this.webServerStateSetterWorker = webServerStateSetterWorker;
        this.webServerFutureMap = webServerFutureMap;
        this.enabled = enabled;
    }

    @Scheduled(fixedDelayString = "${ping.webServer.period.millis}")
    public void execute() {
        if (isEnabled()) {
            final List<WebServer> webServers = webServerService.getWebServersPropagationNew();
            LOGGER.debug("# of web servers to ping = {}", webServers.size());
            try {
                for (WebServer webServer : webServers) {
                    LOGGER.debug(">>> Web server {} future {}", webServer.getId().getId(), webServerFutureMap.get(webServer.getId()));

                    if (webServerFutureMap.get(webServer.getId()) != null) {
                        LOGGER.debug(">>> Web server {} is done {}", webServer.getId().getId(), webServerFutureMap.get(webServer.getId()).isDone());
                        LOGGER.debug(">>> Web server {} is cancelled {}", webServer.getId().getId(), webServerFutureMap.get(webServer.getId()).isCancelled());
                    }

                    if (webServerFutureMap.get(webServer.getId()) == null || webServerFutureMap.get(webServer.getId()).isDone()) {
                        LOGGER.debug(">>> Pinging web server {}...", webServer.getId().getId());
                        webServerFutureMap.put(webServer.getId(), webServerStateSetterWorker.pingWebServer(webServer));
                        LOGGER.debug(">>> Web server {} pinged", webServer.getId().getId());
                    }
                }
            } finally {
                cleanFuturesMap(webServers);
            }
        }
    }

    /**
     * Remove Futures whose key does not match any of the web server ids in the web server list.
     *
     * @param webServers the web server list
     */
    private void cleanFuturesMap(final List<WebServer> webServers) {
        final List<Identifier<WebServer>> futureKeysForRemoval = new ArrayList<>();
        if (webServers.size() < webServerFutureMap.size()) {
            for (Identifier<WebServer> key : webServerFutureMap.keySet()) {
                boolean hasMatch = false;
                for (WebServer webServer : webServers) {
                    hasMatch = key.equals(webServer.getId());
                    if (hasMatch) {
                        break;
                    }
                }

                if (!hasMatch) {
                    futureKeysForRemoval.add(key);
                }
            }

            for (Identifier<WebServer> key : futureKeysForRemoval) {
                webServerFutureMap.remove(key);
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