package com.siemens.cto.aem.service.webserver.impl;

import org.springframework.transaction.annotation.Transactional;

import com.siemens.cto.aem.common.exception.InternalErrorException;
import com.siemens.cto.aem.control.webserver.WebServerCommandExecutor;
import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.exec.ExecData;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.model.webserver.WebServerControlHistory;
import com.siemens.cto.aem.domain.model.webserver.command.CompleteControlWebServerCommand;
import com.siemens.cto.aem.domain.model.webserver.command.ControlWebServerCommand;
import com.siemens.cto.aem.exception.CommandFailureException;
import com.siemens.cto.aem.persistence.service.webserver.WebServerControlPersistenceService;
import com.siemens.cto.aem.service.webserver.WebServerControlService;
import com.siemens.cto.aem.service.webserver.WebServerService;
import com.siemens.cto.aem.service.webserver.WebServerStateGateway;

public class WebServerControlServiceImpl implements WebServerControlService {

    private final WebServerControlPersistenceService persistenceService;
    private final WebServerService webServerService;
    private final WebServerCommandExecutor webServerCommandExecutor;
    private final WebServerStateGateway webServerStateGateway;

    public WebServerControlServiceImpl(final WebServerControlPersistenceService thePersistenceService,
                                       final WebServerService theWebServerService,
                                       final WebServerCommandExecutor theExecutor,
                                       final WebServerStateGateway theWebServerStateGateway) {
        persistenceService = thePersistenceService;
        webServerService = theWebServerService;
        webServerCommandExecutor = theExecutor;
        webServerStateGateway = theWebServerStateGateway;
    }

    @Override
    @Transactional
    public WebServerControlHistory controlWebServer(final ControlWebServerCommand aCommand,
                                                    final User aUser) {

        try {
            aCommand.validateCommand();

            final WebServerControlHistory incompleteHistory = persistenceService.addIncompleteControlHistoryEvent(
                    new Event<>(aCommand, AuditEvent.now(aUser)));

            final Identifier<WebServer> webServerId = aCommand.getWebServerId();

            webServerStateGateway.setExplicitState(webServerId,
                                                   aCommand.getControlOperation().getOperationState());

            final WebServer webServer = webServerService.getWebServer(webServerId);

            final ExecData execData = webServerCommandExecutor.controlWebServer(aCommand,
                                                                                webServer);

            webServerStateGateway.initiateWebServerStateRequest(webServer);

            final WebServerControlHistory completeHistory = persistenceService.completeControlHistoryEvent
                    (new Event<>(new CompleteControlWebServerCommand(incompleteHistory.getId(), execData),
                            AuditEvent.now(aUser)));

            return completeHistory;

        } catch (final CommandFailureException cfe) {
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE,
                                             "CommandFailureException when attempting to control a Web Server: " +
                                             aCommand, cfe);
        }
    }
}