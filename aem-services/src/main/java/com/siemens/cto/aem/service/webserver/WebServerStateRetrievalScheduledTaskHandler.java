package com.siemens.cto.aem.service.webserver;

import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

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

            final List<Future<?>> futures = new ArrayList<>();
            final List<WebServer> webServers = webServerService.getWebServers(PaginationParameter.all());

            for (WebServer webServer : webServers) {
                futures.add(webServerStateSetterWorker.pingWebServer(webServer));
            }

            /**
             * Wait until all asynchronous threads are finished before having this method complete its execution.
             * This is done to make sure that the web server state pipeline does not get clogged.
             * Spring's @Schedule will only commence once this method has finished executing.
             * {@link http://docs.spring.io/spring/docs/current/spring-framework-reference/html/scheduling.html} 28.4.2
             * a fixed delay...the period will be measured from the completion time of each preceding invocation
             */
            if (futures.size() > 0) {
                boolean done = false;
                while (!done) {
                    for (Future<?> future : futures) {
                        done = future.isDone();
                        if (!done) {
                            break;
                        }
                    }
                }
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