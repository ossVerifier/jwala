package com.siemens.cto.aem.control.webserver;

import com.siemens.cto.aem.exec.CommandOutput;
import com.siemens.cto.aem.request.webserver.ControlWebServerRequest;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.exception.CommandFailureException;

public interface WebServerCommandExecutor {

    CommandOutput controlWebServer(final ControlWebServerRequest aCommand,
                              final WebServer aJvm) throws CommandFailureException;
}
