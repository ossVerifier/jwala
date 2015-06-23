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
import com.siemens.cto.aem.service.webserver.WebServerStateGateway;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebServerControlServiceImpl implements WebServerControlService {

    private final WebServerService webServerService;
    private final WebServerCommandExecutor webServerCommandExecutor;
    private final WebServerStateGateway webServerStateGateway;
    private final WebServerControlHistoryService controlHistoryService;
    private final StateService<WebServer, WebServerReachableState> webServerStateService;
    private static final Logger LOGGER = LoggerFactory.getLogger(WebServerControlServiceImpl.class);

    public WebServerControlServiceImpl(final WebServerService theWebServerService,
                                       final WebServerCommandExecutor theExecutor,
                                       final WebServerStateGateway theWebServerStateGateway,
                                       final WebServerControlHistoryService theControlHistoryService,
                                       final StateService<WebServer, WebServerReachableState> theWebServerStateService) {
        webServerService = theWebServerService;
        webServerCommandExecutor = theExecutor;
        webServerStateGateway = theWebServerStateGateway;
        controlHistoryService = theControlHistoryService;
        webServerStateService = theWebServerStateService;
    }

    @Override
    public WebServerControlHistory controlWebServer(final ControlWebServerCommand aCommand,
                                                    final User aUser) {

        try {
            aCommand.validateCommand();

            final WebServerControlHistory incompleteHistory = controlHistoryService.beginIncompleteControlHistory(new Event<>(aCommand,
                    AuditEvent.now(aUser)));

            final Identifier<WebServer> webServerId = aCommand.getWebServerId();

            webServerStateService.setCurrentState(createStateCommandWithoutMessage(aCommand),
                    aUser);

            final WebServer webServer = webServerService.getWebServer(webServerId);

            final ExecData execData = webServerCommandExecutor.controlWebServer(aCommand,
                    webServer);
            if (execData != null && (aCommand.getControlOperation().equals(WebServerControlOperation.START) || aCommand.getControlOperation().equals(WebServerControlOperation.STOP))) {
                execData.cleanStandardOutput();
                LOGGER.info("shell command output{}", execData.getStandardOutput());
            }

            webServerStateGateway.initiateWebServerStateRequest(webServer);

            final WebServerControlHistory completeHistory = controlHistoryService.completeControlHistory(new Event<>(new CompleteControlWebServerCommand(incompleteHistory.getId(), execData),
                    AuditEvent.now(aUser)));

            return completeHistory;

        } catch (final CommandFailureException cfe) {
            webServerStateService.setCurrentState(createStateCommandWithMessage(aCommand.getWebServerId(),
                            WebServerReachableState.WS_FAILED,
                            cfe.getMessage()),
                    aUser);
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE,
                    "CommandFailureException when attempting to control a Web Server: " + aCommand,
                    cfe);
        }
    }

    SetStateCommand<WebServer, WebServerReachableState> createStateCommandWithoutMessage(final ControlWebServerCommand aCommand) {
        final SetStateCommand<WebServer, WebServerReachableState> command = new WebServerSetStateCommand(new CurrentState<>(aCommand.getWebServerId(),
                aCommand.getControlOperation().getOperationState(),
                DateTime.now(),
                StateType.WEB_SERVER));
        return command;
    }

    SetStateCommand<WebServer, WebServerReachableState> createStateCommandWithMessage(final Identifier<WebServer> anId,
                                                                                      final WebServerReachableState aState,
                                                                                      final String aMessage) {
        final SetStateCommand<WebServer, WebServerReachableState> command = new WebServerSetStateCommand(new CurrentState<>(anId,
                aState,
                DateTime.now(),
                StateType.WEB_SERVER,
                aMessage));
        return command;
    }
}