package com.siemens.cto.aem.ws.rest.v1.service.app.impl;

import java.util.List;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.siemens.cto.aem.domain.model.app.Application;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.service.app.ApplicationService;
import com.siemens.cto.aem.ws.rest.v1.provider.PaginationParamProvider;
import com.siemens.cto.aem.ws.rest.v1.response.ResponseBuilder;
import com.siemens.cto.aem.ws.rest.v1.service.app.ApplicationServiceRest;
import com.siemens.cto.aem.ws.rest.v1.service.webserver.impl.WebServerServiceRestImpl;

public class ApplicationServiceRestImpl implements ApplicationServiceRest {

    private final Logger logger;

    private ApplicationService service; 
    
    public ApplicationServiceRestImpl(ApplicationService applicationService) {
        logger = LoggerFactory.getLogger(WebServerServiceRestImpl.class);
        service = applicationService;
    }

    @Override
    public Response getApplications(PaginationParamProvider paginationParamProvider) {
        logger.debug("Get Apps requested with pagination: {}", paginationParamProvider);
        PaginationParameter page = paginationParamProvider.getPaginationParameter();
        final List<Application> apps = service.getApplications(page);
        return ResponseBuilder.ok(apps);
    }

    @Override
    public Response getApplication(Identifier<Application> anAppId) {
        logger.debug("Get App by id: {}", anAppId);
        final Application app = service.getApplication(anAppId);
        return ResponseBuilder.ok(app);
    }

}
