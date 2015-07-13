package com.siemens.cto.aem.service.webserver.component;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.state.StateType;
import com.siemens.cto.aem.domain.model.state.command.SetStateCommand;
import com.siemens.cto.aem.domain.model.state.command.WebServerSetStateCommand;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.service.state.StateService;
import com.siemens.cto.aem.si.ssl.hc.HttpClientRequestFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.ConnectTimeoutException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Sets a web server's state. This class is meant to be a spring bean wherein its "work" method pingWebServer
 * is ran asynchronously as a spun up thread.
 *
 * Note!!! This class has be given it's own package named "component" to denote it as a Spring component
 * that is subject to component scanning. In addition, this was also done to avoid the problem of it's unit test
 * which uses Spring config and component scanning from picking up other Spring components that it does not need
 * which also results to spring bean definition problems.
 *
 * Created by Z003BPEJ on 6/25/2015.
 */
@Service
public class WebServerStateSetterWorker {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebServerStateSetterWorker.class);
    private HttpClientRequestFactory httpClientRequestFactory;
    private Map<Identifier<WebServer>, WebServerReachableState> webServerReachableStateMap;
    private StateService<WebServer, WebServerReachableState> webServerStateService;

    /**
     * Note: Setting of class variables through the constructor preferred but @Async does not work
     * if WebServerStateSetterWorker is instantiated like this in the config file:
     *
     * @Bean
     * WebServerStateSetterWorker webServerStateSetterWorker() {
     *     return new WebServerStateSetterWorker(...);
     * }
     *
     * It should totally be instantiated by Spring (e.g. using @Autowired or if usign XML using <Bean>...<Bean/>)
     *
     * Bean definition using context xml and constructor injection would have worked but for consistency
     * (as the application is mostly Spring annotation driven), it was avoided.
     */
    public WebServerStateSetterWorker() {}

    /**
     * Ping the web server via http get.
     * @param webServer the web server to ping.
     */
    @Async("webServerTaskExecutor")
    public Future<?> pingWebServer(WebServer webServer) {
        ClientHttpResponse response = null;
        if (!isWebServerBusyOrDown(webServer)) {
            try {
                ClientHttpRequest request = httpClientRequestFactory.createRequest(webServer.getStatusUri(), HttpMethod.GET);
                response = request.execute();

                if (response.getStatusCode() == HttpStatus.OK) {
                    setState(webServer, WebServerReachableState.WS_REACHABLE, null);
                } else {
                    setState(webServer, WebServerReachableState.WS_FAILED,
                             "Request for '" + webServer.getStatusUri() + "' failed with a response code of '" +
                             response.getStatusCode() + "'");
                }
            } catch (ConnectTimeoutException cte) {
                LOGGER.info(cte.getLocalizedMessage(), cte);
                setState(webServer, WebServerReachableState.WS_UNREACHABLE, null);
            } catch (IOException ioe) {
                LOGGER.error(ioe.getLocalizedMessage(), ioe);
                setState(webServer, WebServerReachableState.WS_FAILED, ioe.getMessage());
            } finally {
                if (response != null) {
                    response.close();
                }
            }

        }

        return new AsyncResult<>(null);
    }

    /**
     * Checks if a web server is either starting or stopping or is down (in a failed state).
     * @param webServer the webServer
     * @return true if web server is starting or stopping
     */
    private boolean isWebServerBusyOrDown(final WebServer webServer) {
        return  webServerReachableStateMap.get(webServer.getId()) == WebServerReachableState.WS_STARTING ||
                webServerReachableStateMap.get(webServer.getId()) == WebServerReachableState.WS_STOPPING ||
                webServerReachableStateMap.get(webServer.getId()) == WebServerReachableState.WS_FAILED;
    }

    /**
     * Sets the web server state if the web server is not starting or stopping.
     * @param webServer the web server
     * @param webServerReachableState {@link com.siemens.cto.aem.domain.model.webserver.WebServerReachableState}
     * @param msg a message
     */
    private void setState(final WebServer webServer,
                          final WebServerReachableState webServerReachableState,
                          final String msg) {
        if (!isWebServerBusyOrDown(webServer)) {
            webServerStateService.setCurrentState(createStateCommand(webServer.getId(), webServerReachableState, msg),
                                                  User.getSystemUser());
        }
    }

    /**
     * Sets the web server state.
     * @param id the web server id {@link com.siemens.cto.aem.domain.model.id.Identifier}
     * @param state the state {@link com.siemens.cto.aem.domain.model.webserver.WebServerReachableState}
     * @param msg a message
     * @return {@link com.siemens.cto.aem.domain.model.state.command.SetStateCommand}
     */
    private SetStateCommand<WebServer, WebServerReachableState> createStateCommand(final Identifier<WebServer> id,
                                                                                   final WebServerReachableState state,
                                                                                   final String msg) {
            if (StringUtils.isEmpty(msg)) {
                return new WebServerSetStateCommand(new CurrentState<>(id,
                                                                       state,
                                                                       DateTime.now(),
                                                                       StateType.WEB_SERVER));
            }
            return new WebServerSetStateCommand(new CurrentState<>(id,
                                                    state,
                                                    DateTime.now(),
                                                    StateType.WEB_SERVER,
                                                    msg));
    }

    public void setHttpClientRequestFactory(HttpClientRequestFactory httpClientRequestFactory) {
        this.httpClientRequestFactory = httpClientRequestFactory;
    }

    public void setWebServerReachableStateMap(Map<Identifier<WebServer>, WebServerReachableState> webServerReachableStateMap) {
        this.webServerReachableStateMap = webServerReachableStateMap;
    }

    public void setWebServerStateService(StateService<WebServer, WebServerReachableState> webServerStateService) {
        this.webServerStateService = webServerStateService;
    }

}
