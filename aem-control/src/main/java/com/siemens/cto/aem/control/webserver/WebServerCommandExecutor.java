package com.siemens.cto.aem.control.webserver;

import com.siemens.cto.aem.domain.command.exec.CommandOutput;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.command.webserver.ControlWebServerCommand;
import com.siemens.cto.aem.exception.CommandFailureException;

public interface WebServerCommandExecutor {

    CommandOutput controlWebServer(final ControlWebServerCommand aCommand,
                              final WebServer aJvm) throws CommandFailureException;
}
