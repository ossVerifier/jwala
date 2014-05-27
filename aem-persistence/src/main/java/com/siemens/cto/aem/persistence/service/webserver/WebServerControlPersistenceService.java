package com.siemens.cto.aem.persistence.service.webserver;

import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.webserver.WebServerControlHistory;
import com.siemens.cto.aem.domain.model.webserver.command.CompleteControlWebServerCommand;
import com.siemens.cto.aem.domain.model.webserver.command.ControlWebServerCommand;

public interface WebServerControlPersistenceService {

    WebServerControlHistory addIncompleteControlHistoryEvent(final Event<ControlWebServerCommand> anEvent);

    WebServerControlHistory completeControlHistoryEvent(final Event<CompleteControlWebServerCommand> anEvent);
}
