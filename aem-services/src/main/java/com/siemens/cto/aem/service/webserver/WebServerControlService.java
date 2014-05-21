package com.siemens.cto.aem.service.webserver;

import com.siemens.cto.aem.domain.model.webserver.WebServerControlHistory;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.domain.model.webserver.command.ControlWebServerCommand;

public interface WebServerControlService {

    WebServerControlHistory controlWebServer(final ControlWebServerCommand aCommand,
                                             final User aUser);
}