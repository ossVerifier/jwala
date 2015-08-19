package com.siemens.cto.aem.ws.rest.v1.service.app;

import com.siemens.cto.aem.domain.model.app.Application;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.ws.rest.v1.provider.AuthenticatedUser;
import com.siemens.cto.aem.ws.rest.v1.service.app.impl.JsonCreateApplication;
import com.siemens.cto.aem.ws.rest.v1.service.app.impl.JsonUpdateApplication;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/applications")
@Produces(MediaType.APPLICATION_JSON)
public interface ApplicationServiceRest {

    @GET
    Response getApplications(@QueryParam("group.id") final Identifier<Group> aGroupId);

    @GET
    @Path("/{applicationId}")
    Response getApplication(@PathParam("applicationId") final Identifier<Application> anAppId);

    @GET
    @Path("/jvm/{jvmId}")
    Response findApplicationsByJvmId(@PathParam("jvmId") final Identifier<Jvm> aJvmId);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    Response createApplication(final JsonCreateApplication anAppToCreate,
                               @BeanParam final AuthenticatedUser aUser);

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    Response updateApplication(final JsonUpdateApplication appsToUpdate,
                               @BeanParam final AuthenticatedUser aUser);

    @DELETE
    @Path("/{applicationId}")
    Response removeApplication(@PathParam("applicationId") final Identifier<Application> anAppToRemove,
                               @BeanParam final AuthenticatedUser aUser);

    @POST
    @Path("/{applicationId}/war")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    Response uploadWebArchive(@PathParam("applicationId") final Identifier<Application> anAppToGet,
                              @BeanParam final AuthenticatedUser aUser);

    @DELETE
    @Path("/{applicationId}/war")
    Response deleteWebArchive(@PathParam("applicationId") final Identifier<Application> anAppToGet,
                              @BeanParam final AuthenticatedUser aUser);

    @GET
    @Path("/{appName}/resources/name")
    Response getResourceNames(@PathParam("appName") final String appName);

}
