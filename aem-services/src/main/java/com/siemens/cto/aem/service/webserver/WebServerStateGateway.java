package com.siemens.cto.aem.service.webserver;

import com.siemens.cto.aem.domain.model.webserver.WebServer;

public interface WebServerStateGateway {
    void initiateWebServerStateRequest(final WebServer aWebServer);
}
