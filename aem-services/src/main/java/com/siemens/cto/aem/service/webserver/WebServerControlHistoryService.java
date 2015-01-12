package com.siemens.cto.aem.service.webserver;

import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.webserver.WebServerControlHistory;
import com.siemens.cto.aem.domain.model.webserver.command.CompleteControlWebServerCommand;
import com.siemens.cto.aem.domain.model.webserver.command.ControlWebServerCommand;

public interface WebServerControlHistoryService {

    WebServerControlHistory beginIncompleteControlHistory(final Event<ControlWebServerCommand> anEvent);

    WebServerControlHistory completeControlHistory(final Event<CompleteControlWebServerCommand> anEvent);
}
