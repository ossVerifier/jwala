package com.siemens.cto.aem.service.webserver.impl;

import com.siemens.cto.aem.common.exception.InternalErrorException;
import com.siemens.cto.aem.control.webserver.WebServerCommandExecutor;
import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.exec.ExecData;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.state.StateType;
import com.siemens.cto.aem.domain.model.state.command.SetStateCommand;
import com.siemens.cto.aem.domain.model.state.command.WebServerSetStateCommand;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.model.webserver.WebServerControlHistory;
import com.siemens.cto.aem.domain.model.webserver.WebServerControlOperation;
import com.siemens.cto.aem.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.domain.model.webserver.command.CompleteControlWebServerCommand;
import com.siemens.cto.aem.domain.model.webserver.command.ControlWebServerCommand;
import com.siemens.cto.aem.exception.CommandFailureException;
import com.siemens.cto.aem.service.state.StateService;
import com.siemens.cto.aem.service.webserver.WebServerControlHistoryService;
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
    private final WebServerControlHistoryService controlHistoryService;
    private final StateService<WebServer, WebServerReachableState> webServerStateService;
    private static final Logger LOGGER = LoggerFactory.getLogger(WebServerControlServiceImpl.class);
    private final Map<Identifier<WebServer>, WebServerReachableState> webServerReachableStateMap;

    public WebServerControlServiceImpl(final WebServerService theWebServerService,
                                       final WebServerCommandExecutor theExecutor,
                                       final WebServerControlHistoryService theControlHistoryService,
                                       final StateService<WebServer, WebServerReachableState> theWebServerStateService,
                                       final Map<Identifier<WebServer>, WebServerReachableState> theWebServerReachableStateMap) {
        webServerService = theWebServerService;
        webServerCommandExecutor = theExecutor;
        controlHistoryService = theControlHistoryService;
        webServerStateService = theWebServerStateService;
        webServerReachableStateMap = theWebServerReachableStateMap;
    }

    @Override
    public WebServerControlHistory controlWebServer(final ControlWebServerCommand aCommand,
                                                    final User aUser) {

        try {
            aCommand.validateCommand();

            final SetStateCommand<WebServer, WebServerReachableState> setStateCommand = createStateCommand(aCommand);
            webServerReachableStateMap.put(aCommand.getWebServerId(), setStateCommand.getNewState().getState());

            final WebServerControlHistory incompleteHistory = controlHistoryService.beginIncompleteControlHistory(new Event<>(aCommand,
                    AuditEvent.now(aUser)));

            webServerStateService.setCurrentState(setStateCommand, aUser);

            final ExecData execData = webServerCommandExecutor.controlWebServer(aCommand,
                    webServerService.getWebServer(aCommand.getWebServerId()));

            if (execData != null &&
                (aCommand.getControlOperation().equals(WebServerControlOperation.START) ||
                 aCommand.getControlOperation().equals(WebServerControlOperation.STOP))) {
                    execData.cleanStandardOutput();
                    LOGGER.info("shell command output{}", execData.getStandardOutput());

                    // Set the states after sending out the control command.
                    if (execData.getReturnCode().wasSuccessful() || execData.getReturnCode().wasAbnormallySuccessful()) {
                        final WebServerReachableState finalWebServerState =
                                aCommand.getControlOperation().equals(WebServerControlOperation.START) ?
                                        WebServerReachableState.WS_REACHABLE : WebServerReachableState.WS_UNREACHABLE;
                        webServerStateService.setCurrentState(createStateCommand(aCommand.getWebServerId(),
                                                                                 finalWebServerState), aUser);
                    } else {
                        setFailedState(aCommand, aUser, execData.extractMessageFromStandardOutput());
                    }

            }

            return controlHistoryService.completeControlHistory(
                        new Event<>(new CompleteControlWebServerCommand(incompleteHistory.getId(), execData),
                        AuditEvent.now(aUser)));

        } catch (final CommandFailureException cfe) {
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
     * @param aCommand {@link ControlWebServerCommand} which contains the id of the web server whose status is to be set to failed.
     * @param aUser the user who issued the control command.
     * @param msg the message that details the cause of the failed state.
     */
    private void setFailedState(final ControlWebServerCommand aCommand, final User aUser, String msg) {
        final WebServer webServer = webServerService.getWebServer(aCommand.getWebServerId());
        msg = webServer.getName() + " at " + webServer.getHost() + ": " + msg;
        webServerReachableStateMap.put(aCommand.getWebServerId(), WebServerReachableState.WS_FAILED);
        webServerStateService.setCurrentState(createStateCommand(aCommand.getWebServerId(),
                WebServerReachableState.WS_FAILED,
                msg), aUser);
    }

    /**
     * Sets the web server state.
     * @param aCommand {@link ControlWebServerCommand}
     * @return {@link com.siemens.cto.aem.domain.model.state.command.SetStateCommand}
     */
    SetStateCommand<WebServer, WebServerReachableState> createStateCommand(final ControlWebServerCommand aCommand) {
        return new WebServerSetStateCommand(new CurrentState<>(aCommand.getWebServerId(),
                                                               aCommand.getControlOperation().getOperationState(),
                                                               DateTime.now(),
                                                               StateType.WEB_SERVER));
    }

    /**
     * Sets the web server state.
     * @param anId the web server id {@link com.siemens.cto.aem.domain.model.id.Identifier}
     * @param aState the state {@link com.siemens.cto.aem.domain.model.webserver.WebServerReachableState}
     * @param aMessage a message e.g. error message etc.
     * @return {@link com.siemens.cto.aem.domain.model.state.command.SetStateCommand}
     */
    SetStateCommand<WebServer, WebServerReachableState> createStateCommand(final Identifier<WebServer> anId,
                                                                           final WebServerReachableState aState,
                                                                           final String aMessage) {
        return new WebServerSetStateCommand(new CurrentState<>(anId,
                                                               aState,
                                                               DateTime.now(),
                                                               StateType.WEB_SERVER,
                                                               aMessage));
    }

    /**
     * Sets the web server state.
     * @param anId the web server id {@link com.siemens.cto.aem.domain.model.id.Identifier}
     * @param aState the state {@link com.siemens.cto.aem.domain.model.webserver.WebServerReachableState}
     * @return {@link com.siemens.cto.aem.domain.model.state.command.SetStateCommand}
     */
    SetStateCommand<WebServer, WebServerReachableState> createStateCommand(final Identifier<WebServer> anId,
                                                                           final WebServerReachableState aState) {
        return new WebServerSetStateCommand(new CurrentState<>(anId,
                                            aState,
                                            DateTime.now(),
                                            StateType.WEB_SERVER));
    }

}