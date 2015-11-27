package com.siemens.cto.aem.service.webserver;

import com.siemens.cto.aem.domain.command.exec.CommandOutput;
import com.siemens.cto.aem.domain.model.user.User;
import com.siemens.cto.aem.domain.command.webserver.ControlWebServerCommand;

public interface WebServerControlService {

    CommandOutput controlWebServer(final ControlWebServerCommand aCommand,
                                             final User aUser);
}