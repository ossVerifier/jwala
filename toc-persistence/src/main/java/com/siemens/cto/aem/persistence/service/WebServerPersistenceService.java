package com.siemens.cto.aem.persistence.service;

import com.siemens.cto.aem.common.domain.model.webserver.WebServer;

/**
 * Web server persistence service.
 *
 * Created by JC043760 on 12/17/2015.
 */
public interface WebServerPersistenceService {

    WebServer createWebServer(WebServer webServer, String createdBy);

    WebServer updateWebServer(WebServer webServer, String updatedBy);

}
