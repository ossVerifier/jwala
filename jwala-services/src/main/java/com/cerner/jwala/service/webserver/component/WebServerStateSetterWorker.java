package com.cerner.jwala.service.webserver.component;

import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.state.CurrentState;
import com.cerner.jwala.common.domain.model.state.StateType;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.domain.model.webserver.WebServerReachableState;
import com.cerner.jwala.common.domain.model.webserver.WebServerState;
import com.cerner.jwala.common.request.state.SetStateRequest;
import com.cerner.jwala.common.request.state.WebServerSetStateRequest;
import com.cerner.jwala.service.MessagingService;
import com.cerner.jwala.service.group.GroupStateNotificationService;
import com.cerner.jwala.service.state.InMemoryStateManagerService;
import com.cerner.jwala.service.webserver.WebServerService;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.ConnectTimeoutException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

/**
 * Sets a web server's state. This class is meant to be a spring bean wherein its "work" method pingWebServer
 * is ran asynchronously as a spun up thread.
 * <p>
 * Note!!! This class has be given its own package named "component" to denote it as a Spring component
 * that is subject to component scanning. In addition, this was also done to avoid the problem of it's unit test
 * which uses Spring config and component scanning from picking up other Spring components that it does not need
 * which also results to spring bean definition problems.
 * <p>
 * Created by Z003BPEJ on 6/25/2015.
 */
@Service
public class WebServerStateSetterWorker {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebServerStateSetterWorker.class);

    private final InMemoryStateManagerService<Identifier<WebServer>, WebServerReachableState> inMemoryStateManagerService;
    private final WebServerService webServerService;
    private final MessagingService messagingService;
    private final GroupStateNotificationService groupStateNotificationService;

    private HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();


    private static final Map<Identifier<WebServer>, WebServerReachableState> WEB_SERVER_LAST_PERSISTED_STATE_MAP = new ConcurrentHashMap<>();
    private static final Map<Identifier<WebServer>, String> WEB_SERVER_LAST_PERSISTED_ERROR_STATUS_MAP = new ConcurrentHashMap<>();

    @Autowired
    public WebServerStateSetterWorker(@Qualifier("webServerInMemoryStateManagerService")
                                      final InMemoryStateManagerService<Identifier<WebServer>, WebServerReachableState> inMemoryStateManagerService,
                                      final WebServerService webServerService, final MessagingService messagingService,
                                      final GroupStateNotificationService groupStateNotificationService,
                                      @Qualifier("httpRequestFactory")
                                      final HttpComponentsClientHttpRequestFactory httpRequestFactory) {
        this.inMemoryStateManagerService = inMemoryStateManagerService;
        this.webServerService = webServerService;
        this.messagingService = messagingService;
        this.groupStateNotificationService = groupStateNotificationService;
        this.httpRequestFactory = httpRequestFactory;
    }

    /**
     * Ping the web server via http get.
     *
     * @param webServer the web server to ping.
     */
    @Async("webServerTaskExecutor")
    public Future<?> pingWebServer(final WebServer webServer) {
        ClientHttpResponse response = null;
        synchronized (webServer) {
            final WebServerReachableState webServerState = webServer.getState();
            if (!isWebServerBusy(webServer) && inMemoryStateManagerService.get(webServer.getId()) != WebServerReachableState.WS_NEW) {
                final String webServerName = webServer.getName();
                LOGGER.debug(">>>> Send ping for web server {}", webServerName);
                try {
                    final ClientHttpRequest request = httpRequestFactory.createRequest(webServer.getStatusUri(), HttpMethod.GET);
                    response = request.execute();

                    LOGGER.debug(">>> Response = {} from web server {}", response.getStatusCode(), webServer.getId().getId());
                    if (response.getStatusCode() == HttpStatus.OK) {
                        setState(webServer, WebServerReachableState.WS_REACHABLE, StringUtils.EMPTY);
                    } else {
                        setState(webServer, WebServerReachableState.WS_UNREACHABLE,
                                "Request for '" + webServer.getStatusUri() + "' failed with a response code of '" +
                                        response.getStatusCode() + "'");
                    }
                } catch (final IOException ioe) {
                    if (ioe instanceof ConnectTimeoutException) {
                        LOGGER.debug("{} {}", webServerName, ioe.getMessage(), ioe);
                    } else {
                        if (!webServerState.equals(WebServerReachableState.WS_UNREACHABLE)) {
                            LOGGER.info("Web Server {} changing state from {} to {}", webServerName, webServer.getState().toStateLabel(), WebServerReachableState.WS_UNREACHABLE.toStateLabel());
                            LOGGER.debug("{} {}", webServerName, ioe.getMessage(), ioe);
                        }
                    }
                    setState(webServer, WebServerReachableState.WS_UNREACHABLE, StringUtils.EMPTY);
                } catch (final RuntimeException rte) {
                    LOGGER.error(rte.getMessage(), rte);
                } finally {
                    if (response != null) {
                        response.close();
                    }
                }

            }

            return new AsyncResult<>(null);
        }
    }

    /**
     * Checks if a web server is either starting or stopping or is down (in a failed state).
     *
     * @param webServer the webServer
     * @return true if web server is starting or stopping
     */
    private boolean isWebServerBusy(final WebServer webServer) {
        return inMemoryStateManagerService.get(webServer.getId()) == WebServerReachableState.WS_START_SENT ||
                inMemoryStateManagerService.get(webServer.getId()) == WebServerReachableState.WS_STOP_SENT;
    }

    /**
     * Sets the web server state if the web server is not starting or stopping.
     *
     * @param webServer               the web server
     * @param webServerReachableState {@link com.cerner.jwala.common.domain.model.webserver.WebServerReachableState}
     * @param msg                     a message
     */
    private void setState(final WebServer webServer, final WebServerReachableState webServerReachableState, final String msg) {
        if (!isWebServerBusy(webServer) && checkStateChangedAndOrMsgNotEmpty(webServer, webServerReachableState, msg)) {
            webServerService.updateState(webServer.getId(), webServerReachableState, msg);
            messagingService.send(new WebServerState(webServer.getId(), webServerReachableState, DateTime.now()));
            groupStateNotificationService.retrieveStateAndSendToATopic(webServer.getId(), WebServer.class);
        }
    }

    /**
     * Check if state has changed or if message is not empty. Sets WEB_SERVER_LAST_PERSISTED_STATE_MAP and WEB_SERVER_LAST_PERSISTED_ERROR_STATUS_MAP.
     *
     * @param webServer               {@link WebServer}
     * @param webServerReachableState {@link WebServerReachableState}
     * @param msg                     a message (usually an error message)
     * @return true of the state is not the same compared to the previous state or if there's a message (error message)
     */
    private boolean checkStateChangedAndOrMsgNotEmpty(final WebServer webServer, final WebServerReachableState webServerReachableState, final String msg) {
        boolean stateChangedAndOrMsgNotEmpty = false;
        if (!WEB_SERVER_LAST_PERSISTED_STATE_MAP.containsKey(webServer.getId()) ||
                !WEB_SERVER_LAST_PERSISTED_STATE_MAP.get(webServer.getId()).equals(webServerReachableState)) {
            WEB_SERVER_LAST_PERSISTED_STATE_MAP.put(webServer.getId(), webServerReachableState);
            stateChangedAndOrMsgNotEmpty = true;
        }

        if (StringUtils.isNotEmpty(msg) && (!WEB_SERVER_LAST_PERSISTED_ERROR_STATUS_MAP.containsKey(webServer.getId()) ||
                !WEB_SERVER_LAST_PERSISTED_ERROR_STATUS_MAP.get(webServer.getId()).equals(msg))) {
            WEB_SERVER_LAST_PERSISTED_ERROR_STATUS_MAP.put(webServer.getId(), msg);
            stateChangedAndOrMsgNotEmpty = true;
        }
        return stateChangedAndOrMsgNotEmpty;
    }

    /**
     * Sets the web server state.
     *
     * @param id    the web server id {@link com.cerner.jwala.common.domain.model.id.Identifier}
     * @param state the state {@link com.cerner.jwala.common.domain.model.webserver.WebServerReachableState}
     * @param msg   a message
     * @return {@link SetStateRequest}
     */
    private SetStateRequest<WebServer, WebServerReachableState> createStateCommand(final Identifier<WebServer> id,
                                                                                   final WebServerReachableState state,
                                                                                   final String msg) {
        if (StringUtils.isEmpty(msg)) {
            return new WebServerSetStateRequest(new CurrentState<>(id,
                    state,
                    DateTime.now(),
                    StateType.WEB_SERVER));
        }
        return new WebServerSetStateRequest(new CurrentState<>(id,
                state,
                DateTime.now(),
                StateType.WEB_SERVER,
                msg));
    }

}
