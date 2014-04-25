package com.siemens.cto.aem.ws.rest.v1.service.app;

import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.siemens.cto.aem.domain.model.app.Application;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.ws.rest.v1.provider.PaginationParamProvider;

@Path("/applications")
@Produces(MediaType.APPLICATION_JSON)
public interface ApplicationServiceRest {
    
    @GET
    Response getApplications(   
            @QueryParam("group.id") final Identifier<Group> aGroupId,
            @BeanParam final PaginationParamProvider paginationParamProvider );

    @GET
    @Path("/{applicationId}")
    Response getApplication(@PathParam("applicationId") final Identifier<Application> anAppId);
    
}
