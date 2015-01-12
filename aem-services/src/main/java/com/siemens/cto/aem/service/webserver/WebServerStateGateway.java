package com.siemens.cto.aem.service.webserver;

import org.springframework.integration.annotation.Header;
import org.springframework.integration.annotation.Payload;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.model.webserver.WebServerReachableState;

public interface WebServerStateGateway {

    void setExplicitState(@Payload final Identifier<WebServer> anId,
                          @Header("reachableState") final WebServerReachableState anIntendedState);

    void initiateWebServerStateRequest(@Payload final WebServer aWebServer);
}
