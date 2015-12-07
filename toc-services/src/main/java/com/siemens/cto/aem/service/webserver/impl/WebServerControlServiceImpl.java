package com.siemens.cto.aem.service.webserver.impl;

import com.siemens.cto.aem.persistence.jpa.domain.JpaWebServer;
import com.siemens.cto.aem.request.state.SetStateRequest;
import com.siemens.cto.aem.request.state.WebServerSetStateRequest;
import com.siemens.cto.aem.common.exception.InternalErrorException;
import com.siemens.cto.aem.control.webserver.WebServerCommandExecutor;
import com.siemens.cto.aem.exec.CommandOutput;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.state.StateType;
import com.siemens.cto.aem.domain.model.user.User;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.model.webserver.WebServerControlOperation;
import com.siemens.cto.aem.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.request.webserver.ControlWebServerRequest;
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

    /**
     * Since the querying of the web server status only depends on pinging to tell if it's alive or not, we can't tell for sure
     * if the web server is really starting or stopping. Therefore it is assumed that if the user sends a signal for the
     * web server to start or to stop, then the said web server is starting or stopping. So unlike the JVMs where we
     * initially display START SENT or STOP SENT upon starting or stopping and getting a starting/stopping status afterwards,
     * with the web servers we just say that the operational state is already STARTING/STOPPING since the web server
     * will not return a status that is STARTING/STOPPING ( IMHO, we should just say START SENT or STOP SENT
     * and let the UI display that as well and then explain to the user that the Web Server status is determined via pinging
     * that is why we can't really say if the web server is starting/stopping.)
     *
     * This method translates STARTING/STOPPING to START SENT/STOP SENT.
     *
     * @return
     */
    private String translateEvent(final String event) {
        if (event.equalsIgnoreCase("STARTING")) {
            return "START SENT";
        } else if (event.equalsIgnoreCase("STOPPING")) {
            return "STOP SENT";
        }
        return event;
    }

    @Override
    public CommandOutput controlWebServer(final ControlWebServerRequest aCommand,
                                          final User aUser) {

        final JpaWebServer webServer = webServerService.getJpaWebServer(aCommand.getWebServerId().getId(), true);
        try {
            final String event = aCommand.getControlOperation().getOperationState() == null ?
                aCommand.getControlOperation().name() :
                aCommand.getControlOperation().getOperationState().toStateString();
            historyService.createHistory(webServer.getName(), webServer.getGroups(), translateEvent(event), aUser.getId());

            aCommand.validate();

            final SetStateRequest<WebServer, WebServerReachableState> setStateCommand = createStateCommand(aCommand);
            webServerReachableStateMap.put(aCommand.getWebServerId(), setStateCommand.getNewState().getState());

            webServerStateService.setCurrentState(setStateCommand, aUser);

            final CommandOutput commandOutput = webServerCommandExecutor.controlWebServer(aCommand,
                    webServerService.getWebServer(aCommand.getWebServerId()));

            if (commandOutput != null &&
                (aCommand.getControlOperation().equals(WebServerControlOperation.START) ||
                 aCommand.getControlOperation().equals(WebServerControlOperation.STOP))) {
                    commandOutput.cleanStandardOutput();
                    LOGGER.info("shell command output{}", commandOutput.getStandardOutput());

                    // Set the states after sending out the control command.
                    if (commandOutput.getReturnCode().wasSuccessful() || commandOutput.getReturnCode().wasAbnormallySuccessful()) {
                        final WebServerReachableState finalWebServerState =
                                aCommand.getControlOperation().equals(WebServerControlOperation.START) ?
                                        WebServerReachableState.WS_REACHABLE : WebServerReachableState.WS_UNREACHABLE;
                        webServerStateService.setCurrentState(createStateCommand(aCommand.getWebServerId(),
                                                                                 finalWebServerState), aUser);
                    } else {
                        setFailedState(aCommand, aUser, commandOutput.extractMessageFromStandardOutput());
                    }

            }

            return commandOutput;
        } catch (final CommandFailureException cfe) {
            historyService.createHistory(webServer.getName(), webServer.getGroups(), cfe.getMessage(), aUser.toString());

            setFailedState(aCommand, aUser, ExceptionUtils.getStackTrace(cfe));
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE,
                                             "CommandFailureException when attempting to control a Web Server: " + aCommand,
                                             cfe);
        } finally {
            webServerReachableStateMap.remove(aCommand.getWebServerId());
        }
    }

    /**
     * Set web server state to failed.
     * @param aCommand {@link ControlWebServerRequest} which contains the id of the web server whose status is to be set to failed.
     * @param aUser the user who issued the control command.
     * @param msg the message that details the cause of the failed state.
     */
    private void setFailedState(final ControlWebServerRequest aCommand, final User aUser, String msg) {
        final WebServer webServer = webServerService.getWebServer(aCommand.getWebServerId());
        msg = webServer.getName() + " at " + webServer.getHost() + ": " + msg;
        webServerReachableStateMap.put(aCommand.getWebServerId(), WebServerReachableState.WS_FAILED);
        webServerStateService.setCurrentState(createStateCommand(aCommand.getWebServerId(),
                WebServerReachableState.WS_FAILED,
                msg), aUser);
    }

    /**
     * Sets the web server state.
     * @param aCommand {@link ControlWebServerRequest}
     * @return {@link SetStateRequest}
     */
    SetStateRequest<WebServer, WebServerReachableState> createStateCommand(final ControlWebServerRequest aCommand) {
        return new WebServerSetStateRequest(new CurrentState<>(aCommand.getWebServerId(),
                                                               aCommand.getControlOperation().getOperationState(),
                                                               DateTime.now(),
                                                               StateType.WEB_SERVER));
    }

    /**
     * Sets the web server state.
     * @param anId the web server id {@link com.siemens.cto.aem.domain.model.id.Identifier}
     * @param aState the state {@link com.siemens.cto.aem.domain.model.webserver.WebServerReachableState}
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
     * @param anId the web server id {@link com.siemens.cto.aem.domain.model.id.Identifier}
     * @param aState the state {@link com.siemens.cto.aem.domain.model.webserver.WebServerReachableState}
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