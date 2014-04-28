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

    private final static Logger LOGGER = LoggerFactory.getLogger(WebServerServiceRestImpl.class); 

    private ApplicationService service; 
    
    public ApplicationServiceRestImpl(ApplicationService applicationService) {
        service = applicationService;
    }

    @Override
    public Response getApplication(Identifier<Application> anAppId) {
        LOGGER.debug("Get App by id: {}", anAppId);
        final Application app = service.getApplication(anAppId);
        return ResponseBuilder.ok(app);
    }

    @Override
    public Response getApplications(Identifier<Group> aGroupId, PaginationParamProvider paginationParamProvider) {
        LOGGER.debug("Get Apps requested with pagination: {}, groupId: {}", paginationParamProvider, aGroupId != null ? aGroupId : "null");
        final List<Application> apps; 
        PaginationParameter page = paginationParamProvider.getPaginationParameter();
        if(aGroupId != null) {
            apps = service.findApplications(aGroupId, page);
        } else {
            apps = service.getApplications(page);
        }
        return ResponseBuilder.ok(apps); 
    }

}
