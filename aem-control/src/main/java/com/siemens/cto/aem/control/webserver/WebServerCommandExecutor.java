package com.siemens.cto.aem.control.webserver;

import com.siemens.cto.aem.domain.model.exec.ExecData;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.model.webserver.command.ControlWebServerCommand;
import com.siemens.cto.aem.exception.CommandFailureException;

public interface WebServerCommandExecutor {

    ExecData controlWebServer(final ControlWebServerCommand aCommand,
                              final WebServer aJvm) throws CommandFailureException;
}
