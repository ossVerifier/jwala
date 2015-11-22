package com.siemens.cto.aem.service.webserver;

import com.siemens.cto.aem.domain.model.exec.CommandOutput;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.domain.model.webserver.command.ControlWebServerCommand;

public interface WebServerControlService {

    CommandOutput controlWebServer(final ControlWebServerCommand aCommand,
                                             final User aUser);
}