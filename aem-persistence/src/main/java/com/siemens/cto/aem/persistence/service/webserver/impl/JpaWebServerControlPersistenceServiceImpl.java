package com.siemens.cto.aem.persistence.service.webserver.impl;

import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.webserver.WebServerControlHistory;
import com.siemens.cto.aem.domain.model.webserver.command.CompleteControlWebServerCommand;
import com.siemens.cto.aem.domain.model.webserver.command.ControlWebServerCommand;
import com.siemens.cto.aem.persistence.jpa.domain.JpaWebServerControlHistory;
import com.siemens.cto.aem.persistence.jpa.domain.builder.JpaWebServerControlHistoryBuilder;
import com.siemens.cto.aem.persistence.jpa.service.webserver.WebServerControlCrudService;
import com.siemens.cto.aem.persistence.service.webserver.WebServerControlPersistenceService;

public class JpaWebServerControlPersistenceServiceImpl implements WebServerControlPersistenceService {

    private final WebServerControlCrudService crudService;

    public JpaWebServerControlPersistenceServiceImpl(final WebServerControlCrudService theService) {
        crudService = theService;
    }

    @Override
    public WebServerControlHistory addIncompleteControlHistoryEvent(final Event<ControlWebServerCommand> anEvent) {
        final JpaWebServerControlHistory history = crudService.addIncompleteControlHistoryEvent(anEvent);
        return new JpaWebServerControlHistoryBuilder(history).build();
    }

    @Override
    public WebServerControlHistory completeControlHistoryEvent(final Event<CompleteControlWebServerCommand> anEvent) {
        final JpaWebServerControlHistory history = crudService.completeControlHistoryEvent(anEvent);
        return new JpaWebServerControlHistoryBuilder(history).build();
    }
}