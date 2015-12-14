package com.siemens.cto.aem.ws.rest.v1.service.app;

import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
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

    /**
     * Get resource template content.
     * @param appName web application name.
     * @param resourceTemplateName the resource template name.
     * @param tokensReplaced flag that indicates whether to fetch the template with its tokens replaced by their mapped values from the db.
     * @return the template contents
     */
    @GET
    @Path("/{appName}/resources/template/{resourceTemplateName}")
    Response getResourceTemplate(@PathParam("appName") String appName,
                                 @MatrixParam("groupName") String groupName,
                                 @MatrixParam("jvmName") String jvmName,
                                 @PathParam("resourceTemplateName") String resourceTemplateName,
                                 @QueryParam("tokensReplaced") boolean tokensReplaced);

    @PUT
    @Path("/{appName}/resources/template/{resourceTemplateName}")
    @Consumes(MediaType.TEXT_PLAIN)
    Response updateResourceTemplate(@PathParam("appName") final String appName,
                                    @PathParam("resourceTemplateName") final String resourceTemplateName,
                                    final String content);

    @PUT
    @Path("/{appName}/conf/{resourceTemplateName}")
    Response deployConf(@PathParam("appName") String appName,
                        @MatrixParam("groupName") String groupName,
                        @MatrixParam("jvmName") String jvmName,
                        @PathParam("resourceTemplateName") String resourceTemplateName,
                        @BeanParam AuthenticatedUser aUser);

    @POST
    @Path("/{appName}/resources/uploadTemplate")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    Response uploadConfigTemplate(@PathParam("appName") final String appName,
                                  @BeanParam final AuthenticatedUser aUser,
                                  @QueryParam("templateName") final String templateName);

    @PUT
    @Path("/{appName}/resources/preview")
    @Consumes(MediaType.TEXT_PLAIN)
    Response previewResourceTemplate(@PathParam("appName") String appName,
                                     @MatrixParam("groupName") String groupName,
                                     @MatrixParam("jvmName") String jvmName,
                                     String template);

}
