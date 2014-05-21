package com.siemens.cto.aem.service.webserver.impl;

import com.siemens.cto.aem.common.exception.InternalErrorException;
import com.siemens.cto.aem.control.webserver.WebServerCommandExecutor;
import com.siemens.cto.aem.domain.model.exec.ExecData;
import com.siemens.cto.aem.domain.model.exec.ExecReturnCode;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.model.webserver.WebServerControlHistory;
import com.siemens.cto.aem.domain.model.webserver.command.ControlWebServerCommand;
import com.siemens.cto.aem.exception.CommandFailureException;
import com.siemens.cto.aem.service.webserver.WebServerControlService;
import com.siemens.cto.aem.service.webserver.WebServerService;

public class WebServerControlServiceImpl implements WebServerControlService {

//    private final WebServerControlPersistenceService persistenceService;
    private final WebServerService webServerService;
    private final WebServerCommandExecutor webServerCommandExecutor;

    public WebServerControlServiceImpl(final WebServerService theWebServerService,
                                       final WebServerCommandExecutor theExecutor) {
        webServerService = theWebServerService;
        webServerCommandExecutor = theExecutor;
    }

// TODO: Use this if the team decides to implement control history as it is with JVM
//    public WebServerControlServiceImpl(final WebServerControlPersistenceService thePersistenceService,
//                                       final WebServerService theWebServerService,
//                                       final WebServerCommandExecutor theExecutor) {
//        persistenceService = thePersistenceService;
//        webServerService = theWebServerService;
//        webServerCommandExecutor = theExecutor;
//    }

    @Override
    // TODO: Discuss with team control history before doing something about it
    // @Transactional
    public WebServerControlHistory controlWebServer(final ControlWebServerCommand aCommand,
                                                    final User aUser) {

        try {
            aCommand.validateCommand();

//            final WebServerControlHistory incompleteHistory = persistenceService.addIncompleteControlHistoryEvent(
//                    new Event<>(aCommand, AuditEvent.now(aUser)));
            final WebServer webServer = webServerService.getWebServer(aCommand.getWebServerId());

            final ExecData execData = webServerCommandExecutor.controlWebServer(aCommand,
                                                                                webServer);

//            final WebServerControlHistory completeHistory = persistenceService.completeControlHistoryEvent
//                    (new Event<>(new CompleteControlWebServerCommand(incompleteHistory.getId(), execData),
//                            AuditEvent.now(aUser)));

//            return completeHistory;

            // This temporary until the team has decided on control history
            return new WebServerControlHistory(new Identifier<WebServerControlHistory>(new Long(0)),
                                               aCommand.getWebServerId(),
                                               aCommand.getControlOperation(),
                                               null,
                                               new ExecData(new ExecReturnCode(0), "", ""));

        } catch (final CommandFailureException cfe) {
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE,
                                             "CommandFailureException when attempting to control a Web Server: " + 
                                             aCommand, cfe);
        }
    }
}