package com.siemens.cto.aem.service.webserver.impl;

import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.webserver.WebServerControlHistory;
import com.siemens.cto.aem.domain.model.webserver.command.CompleteControlWebServerCommand;
import com.siemens.cto.aem.domain.model.webserver.command.ControlWebServerCommand;
import com.siemens.cto.aem.persistence.service.webserver.WebServerControlPersistenceService;
import com.siemens.cto.aem.service.webserver.WebServerControlHistoryService;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class WebServerControlHistoryServiceImpl implements WebServerControlHistoryService {

    private final WebServerControlPersistenceService persistenceService;

    public WebServerControlHistoryServiceImpl(final WebServerControlPersistenceService thePersistenceService) {
        persistenceService = thePersistenceService;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public WebServerControlHistory beginIncompleteControlHistory(final Event<ControlWebServerCommand> anEvent) {
        final WebServerControlHistory incompleteHistory = persistenceService.addIncompleteControlHistoryEvent(anEvent);
        return incompleteHistory;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public WebServerControlHistory completeControlHistory(final Event<CompleteControlWebServerCommand> anEvent) {
        final WebServerControlHistory completeHistory = persistenceService.completeControlHistoryEvent(anEvent);
        return completeHistory;
    }
}
