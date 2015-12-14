package com.siemens.cto.aem.service.webserver;

import com.siemens.cto.aem.common.exec.CommandOutput;
import com.siemens.cto.aem.common.request.webserver.ControlWebServerRequest;
import com.siemens.cto.aem.common.domain.model.user.User;

public interface WebServerControlService {

    CommandOutput controlWebServer(final ControlWebServerRequest aCommand,
                                             final User aUser);
}