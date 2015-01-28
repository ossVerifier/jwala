package com.siemens.cto.aem.service.webserver.heartbeat;

import java.util.List;

import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.service.webserver.WebServerService;

public class WebServerServiceFacade {

    private final WebServerService service;

    public WebServerServiceFacade(final WebServerService theService) {
        service = theService;
    }

    public List<WebServer> getAllWebServers() {
        return service.getWebServers(PaginationParameter.all());
    }
}
