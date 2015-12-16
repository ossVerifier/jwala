package com.siemens.cto.aem.control.webserver;

import com.siemens.cto.aem.common.exec.CommandOutput;
import com.siemens.cto.aem.common.request.webserver.ControlWebServerRequest;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.exception.CommandFailureException;

public interface WebServerCommandExecutor {

    CommandOutput controlWebServer(final ControlWebServerRequest controlWebServerRequest,
                              final WebServer aJvm) throws CommandFailureException;
}
