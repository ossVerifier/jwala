package com.siemens.cto.aem.persistence.service.impl;

import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.persistence.jpa.service.GroupCrudService;
import com.siemens.cto.aem.persistence.jpa.service.WebServerCrudService;
import com.siemens.cto.aem.persistence.service.WebServerPersistenceService;

/**
 * {@link WebServerPersistenceService} implementation.
 *
 * Created by JC043760 on 12/17/2015.
 */
public class WebServerPersistenceServiceImpl implements WebServerPersistenceService {

    private final GroupCrudService groupCrudService;
    private final WebServerCrudService webServerCrudService;

    public WebServerPersistenceServiceImpl(final GroupCrudService groupCrudService,
                                           final WebServerCrudService webServerCrudService) {
        this.groupCrudService = groupCrudService;
        this.webServerCrudService = webServerCrudService;
    }

    @Override
    public WebServer createWebServer(final WebServer webServer, final String createdBy) {
        WebServer createdWebServer = webServerCrudService.createWebServer(webServer, createdBy);
        groupCrudService.linkWebServer(createdWebServer.getId(), webServer);
        return webServerCrudService.getWebServer(createdWebServer.getId());
    }

    @Override
    public WebServer updateWebServer(final WebServer webServer, final String updatedBy) {
        webServerCrudService.updateWebServer(webServer, updatedBy);
        groupCrudService.linkWebServer(webServer);
        return webServerCrudService.getWebServer(webServer.getId());
    }

}
