package com.siemens.cto.aem.service.webserver;

import com.siemens.cto.aem.exec.CommandOutput;
import com.siemens.cto.aem.request.webserver.ControlWebServerRequest;
import com.siemens.cto.aem.domain.model.user.User;

public interface WebServerControlService {

    CommandOutput controlWebServer(final ControlWebServerRequest aCommand,
                                             final User aUser);
}