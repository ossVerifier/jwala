package com.siemens.cto.aem.service.webserver;

import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.service.webserver.heartbeat.WebServerServiceFacade;
import com.siemens.cto.aem.service.webserver.heartbeat.WebServerStateServiceFacade;
import com.siemens.cto.aem.si.ssl.hc.HttpClientRequestFactory;
import org.joda.time.DateTime;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
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
    private final WebServerServiceFacade webServerServiceFacade;
    private final HttpClientRequestFactory httpClientRequestFactory;
    private final WebServerStateServiceFacade webServerStateServiceFacade;

    public WebServerStateRetrievalScheduledTaskHandler(WebServerServiceFacade webServerServiceFacade,
                                                       HttpClientRequestFactory httpClientRequestFactory,
                                                       WebServerStateServiceFacade webServerStateServiceFacade) {
        this.webServerServiceFacade = webServerServiceFacade;
        this.httpClientRequestFactory = httpClientRequestFactory;
        this.webServerStateServiceFacade = webServerStateServiceFacade;
    }

    @Scheduled(fixedDelayString = "${ping.webServer.period.millis}")
    public void execute() throws IOException {
        final List<WebServer> webServers = webServerServiceFacade.getAllWebServers();
        ClientHttpResponse response = null;
        for (WebServer webServer: webServers) {
            try {
                ClientHttpRequest request = httpClientRequestFactory.createRequest(webServer.getStatusUri(), HttpMethod.GET);
                response = request.execute();

                if (response.getStatusCode() == HttpStatus.OK) {
                    webServerStateServiceFacade.setState(webServer.getId(),
                                                         WebServerReachableState.WS_REACHABLE,
                                                         DateTime.now());
                } else {
                    webServerStateServiceFacade.setState(webServer.getId(),
                                                         WebServerReachableState.WS_UNREACHABLE,
                                                         DateTime.now());
                }
            } catch (IOException ioe) {
                webServerStateServiceFacade.setState(webServer.getId(),
                                                     WebServerReachableState.WS_UNREACHABLE,
                                                     DateTime.now());
            } finally {
                if (response != null) {
                    response.close();
                }
            }
        }
    }

}