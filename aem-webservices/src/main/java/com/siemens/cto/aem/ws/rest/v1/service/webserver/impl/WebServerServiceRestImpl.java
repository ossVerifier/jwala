package com.siemens.cto.aem.ws.rest.v1.service.webserver.impl;

import java.util.List;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.service.webserver.WebServerService;
import com.siemens.cto.aem.ws.rest.v1.provider.PaginationParamProvider;
import com.siemens.cto.aem.ws.rest.v1.response.ResponseBuilder;
import com.siemens.cto.aem.ws.rest.v1.service.webserver.WebServerServiceRest;

public class WebServerServiceRestImpl implements WebServerServiceRest {

    private final Logger logger;

    private final WebServerService webServerService;

    public WebServerServiceRestImpl(final WebServerService theWebServerService) {
        logger = LoggerFactory.getLogger(WebServerServiceRestImpl.class);
        webServerService = theWebServerService;
    }

    @Override
    public Response getWebServers(final PaginationParamProvider paginationParamProvider) {
        logger.debug("Get JVMs requested with pagination: {}", paginationParamProvider);
        final List<WebServer> jvms = webServerService.getWebServers(paginationParamProvider.getPaginationParameter());
        return ResponseBuilder.ok(jvms);
    }

    @Override
    public Response getWebServer(final Identifier<WebServer> aJvmId) {
        logger.debug("Get JVM requested: {}", aJvmId);
        return ResponseBuilder.ok(webServerService.getWebServer(aJvmId));
    }

    @Override
    public Response createWebServer(final JsonCreateWebServer aWebServerToCreate) {
        logger.debug("Create JVM requested: {}", aWebServerToCreate);
        return ResponseBuilder.created(webServerService.createWebServer(aWebServerToCreate.toCreateWebServerCommand(),
                                                            User.getHardCodedUser()));
    }

    @Override
    public Response updateWebServer(final JsonUpdateWebServer aWebServerToCreate) {
        logger.debug("Update JVM requested: {}", aWebServerToCreate);
        return ResponseBuilder.ok(webServerService.updateWebServer(aWebServerToCreate.toUpdateWebServerCommand(),
                                                       User.getHardCodedUser()));
    }

    @Override
    public Response removeWebServer(final Identifier<WebServer> aJvmId) {
        logger.debug("Delete JVM requested: {}", aJvmId);
        webServerService.removeWebServer(aJvmId);
        return ResponseBuilder.ok();
    }
}
