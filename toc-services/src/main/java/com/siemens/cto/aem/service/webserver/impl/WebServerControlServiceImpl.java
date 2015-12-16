package com.siemens.cto.aem.service.webserver.impl;

import com.siemens.cto.aem.persistence.jpa.domain.JpaWebServer;
import com.siemens.cto.aem.persistence.jpa.type.EventType;
import com.siemens.cto.aem.common.request.state.SetStateRequest;
import com.siemens.cto.aem.common.request.state.WebServerSetStateRequest;
import com.siemens.cto.aem.common.exception.InternalErrorException;
import com.siemens.cto.aem.control.webserver.WebServerCommandExecutor;
import com.siemens.cto.aem.common.exec.CommandOutput;
import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.state.StateType;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerControlOperation;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.common.request.webserver.ControlWebServerRequest;
import com.siemens.cto.aem.exception.CommandFailureException;
import com.siemens.cto.aem.service.HistoryService;
import com.siemens.cto.aem.service.state.StateService;
import com.siemens.cto.aem.service.webserver.WebServerControlService;
import com.siemens.cto.aem.service.webserver.WebServerService;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class WebServerControlServiceImpl implements WebServerControlService {

    private final WebServerService webServerService;
    private final WebServerCommandExecutor webServerCommandExecutor;
    private final StateService<WebServer, WebServerReachableState> webServerStateService;
    private static final Logger LOGGER = LoggerFactory.getLogger(WebServerControlServiceImpl.class);
    private final Map<Identifier<WebServer>, WebServerReachableState> webServerReachableStateMap;
    private final HistoryService historyService;

    public WebServerControlServiceImpl(final WebServerService theWebServerService,
                                       final WebServerCommandExecutor theExecutor,
                                       final StateService<WebServer, WebServerReachableState> theWebServerStateService,
                                       final Map<Identifier<WebServer>, WebServerReachableState> theWebServerReachableStateMap,
                                       final HistoryService historyService) {
        webServerService = theWebServerService;
        webServerCommandExecutor = theExecutor;
        webServerStateService = theWebServerStateService;
        webServerReachableStateMap = theWebServerReachableStateMap;
        this.historyService = historyService;
    }

    @Override
    public CommandOutput controlWebServer(final ControlWebServerRequest controlWebServerRequest,
                                          final User aUser) {

        final JpaWebServer webServer = webServerService.getJpaWebServer(controlWebServerRequest.getWebServerId().getId(), true);
        try {
            final String event = controlWebServerRequest.getControlOperation().getOperationState() == null ?
                controlWebServerRequest.getControlOperation().name() :
                controlWebServerRequest.getControlOperation().getOperationState().toStateString();
            historyService.createHistory(webServer.getName(), webServer.getGroups(), event, EventType.USER_ACTION,
                    aUser.getId());

            controlWebServerRequest.validate();

            final SetStateRequest<WebServer, WebServerReachableState> setStateCommand = createStateCommand(controlWebServerRequest);
            webServerReachableStateMap.put(controlWebServerRequest.getWebServerId(), setStateCommand.getNewState().getState());

            webServerStateService.setCurrentState(setStateCommand, aUser);

            final CommandOutput commandOutput = webServerCommandExecutor.controlWebServer(controlWebServerRequest,
                    webServerService.getWebServer(controlWebServerRequest.getWebServerId()));

            if (commandOutput != null &&
                (controlWebServerRequest.getControlOperation().equals(WebServerControlOperation.START) ||
                 controlWebServerRequest.getControlOperation().equals(WebServerControlOperation.STOP))) {
                    commandOutput.cleanStandardOutput();
                    LOGGER.info("shell command output{}", commandOutput.getStandardOutput());

                    // Set the states after sending out the control command.
                    if (commandOutput.getReturnCode().wasSuccessful() || commandOutput.getReturnCode().wasAbnormallySuccessful()) {
                        final WebServerReachableState finalWebServerState =
                                controlWebServerRequest.getControlOperation().equals(WebServerControlOperation.START) ?
                                        WebServerReachableState.WS_REACHABLE : WebServerReachableState.WS_UNREACHABLE;
                        webServerStateService.setCurrentState(createStateCommand(controlWebServerRequest.getWebServerId(),
                                                                                 finalWebServerState), aUser);
                    } else {
                        setFailedState(controlWebServerRequest, aUser, commandOutput.extractMessageFromStandardOutput());
                    }

            }

            return commandOutput;
        } catch (final CommandFailureException cfe) {
            final String stackTrace = ExceptionUtils.getStackTrace(cfe);
            historyService.createHistory(webServer.getName(), webServer.getGroups(), stackTrace,
                    EventType.APPLICATION_ERROR, aUser.getId());

            setFailedState(controlWebServerRequest, aUser, stackTrace);
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE,
                                             "CommandFailureException when attempting to control a Web Server: " + controlWebServerRequest,
                                             cfe);
        } finally {
            webServerReachableStateMap.remove(controlWebServerRequest.getWebServerId());
        }
    }

    /**
     * Set web server state to failed.
     * @param controlWebServerRequest {@link ControlWebServerRequest} which contains the id of the web server whose status is to be set to failed.
     * @param aUser the user who issued the control command.
     * @param msg the message that details the cause of the failed state.
     */
    private void setFailedState(final ControlWebServerRequest controlWebServerRequest, final User aUser, String msg) {
        final WebServer webServer = webServerService.getWebServer(controlWebServerRequest.getWebServerId());
        msg = webServer.getName() + " at " + webServer.getHost() + ": " + msg;
        webServerReachableStateMap.put(controlWebServerRequest.getWebServerId(), WebServerReachableState.WS_FAILED);
        webServerStateService.setCurrentState(createStateCommand(controlWebServerRequest.getWebServerId(),
                WebServerReachableState.WS_FAILED,
                msg), aUser);
    }

    /**
     * Sets the web server state.
     * @param controlWebServerRequest {@link ControlWebServerRequest}
     * @return {@link SetStateRequest}
     */
    SetStateRequest<WebServer, WebServerReachableState> createStateCommand(final ControlWebServerRequest controlWebServerRequest) {
        return new WebServerSetStateRequest(new CurrentState<>(controlWebServerRequest.getWebServerId(),
                                                               controlWebServerRequest.getControlOperation().getOperationState(),
                                                               DateTime.now(),
                                                               StateType.WEB_SERVER));
    }

    /**
     * Sets the web server state.
     * @param anId the web server id {@link com.siemens.cto.aem.common.domain.model.id.Identifier}
     * @param aState the state {@link com.siemens.cto.aem.common.domain.model.webserver.WebServerReachableState}
     * @param aMessage a message e.g. error message etc.
     * @return {@link SetStateRequest}
     */
    SetStateRequest<WebServer, WebServerReachableState> createStateCommand(final Identifier<WebServer> anId,
                                                                           final WebServerReachableState aState,
                                                                           final String aMessage) {
        return new WebServerSetStateRequest(new CurrentState<>(anId,
                                                               aState,
                                                               DateTime.now(),
                                                               StateType.WEB_SERVER,
                                                               aMessage));
    }

    /**
     * Sets the web server state.
     * @param anId the web server id {@link com.siemens.cto.aem.common.domain.model.id.Identifier}
     * @param aState the state {@link com.siemens.cto.aem.common.domain.model.webserver.WebServerReachableState}
     * @return {@link SetStateRequest}
     */
    SetStateRequest<WebServer, WebServerReachableState> createStateCommand(final Identifier<WebServer> anId,
                                                                           final WebServerReachableState aState) {
        return new WebServerSetStateRequest(new CurrentState<>(anId,
                                            aState,
                                            DateTime.now(),
                                            StateType.WEB_SERVER));
    }

}