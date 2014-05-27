package com.siemens.cto.aem.persistence.jpa.service.webserver;

import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.webserver.command.CompleteControlWebServerCommand;
import com.siemens.cto.aem.domain.model.webserver.command.ControlWebServerCommand;
import com.siemens.cto.aem.persistence.jpa.domain.JpaWebServerControlHistory;

public interface WebServerControlCrudService {

    JpaWebServerControlHistory addIncompleteControlHistoryEvent(final Event<ControlWebServerCommand> anEvent);

    JpaWebServerControlHistory completeControlHistoryEvent(final Event<CompleteControlWebServerCommand> anEvent);
}
