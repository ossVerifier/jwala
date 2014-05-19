package com.siemens.cto.aem.ws.rest.v1.service.app.impl;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.siemens.cto.aem.domain.model.app.Application;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.domain.model.temporary.User;
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

    @Override
    public Response findApplicationsByJvmId(Identifier<Jvm> aJvmId, PaginationParamProvider paginationParamProvider) {
        LOGGER.debug("Find Apps requested with pagination: {}, aJvmId: {}", paginationParamProvider, aJvmId != null ? aJvmId : "null");
        PaginationParameter page = paginationParamProvider.getPaginationParameter();
        if(aJvmId != null) {
            final List<Application> apps = service.findApplicationsByJvmId(aJvmId, page);
            return ResponseBuilder.ok(apps);
        } else {
            final List<Application> apps = service.getApplications(page);
            return ResponseBuilder.ok(apps);
        }
    }

    @Override
    public Response createApplication(final ArrayList<JsonCreateApplication> appsToCreate) {
        ArrayList<Application> result = new ArrayList<>();
        for(JsonCreateApplication anAppToCreate : appsToCreate) {
            LOGGER.debug("Create Application requested: {}", anAppToCreate);            
            Application created = service.createApplication(anAppToCreate.toCreateCommand(),
                    User.getHardCodedUser());
            result.add(created);
        }
        return ResponseBuilder.ok(result);
    }

    @Override
    public Response updateApplication(final ArrayList<JsonUpdateApplication> appsToUpdate) {
        ArrayList<Application> result = new ArrayList<>();
        for(JsonUpdateApplication anAppToUpdate : appsToUpdate) {
            LOGGER.debug("Update Application requested: {}", anAppToUpdate);
            Application created = service.updateApplication(anAppToUpdate.toUpdateCommand(),
                    User.getHardCodedUser());
            result.add(created);
        }
        return ResponseBuilder.ok(result);
    }

    @Override
    public Response removeApplication(final Identifier<Application> anAppToRemove) {
        LOGGER.debug("Delete JVM requested: {}", anAppToRemove);
        service.removeApplication(anAppToRemove, User.getHardCodedUser());
        return ResponseBuilder.ok();
    }
}
