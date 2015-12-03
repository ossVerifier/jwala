package com.siemens.cto.aem.ws.rest.v1.service.jvm;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.ws.rest.v1.provider.AuthenticatedUser;
import com.siemens.cto.aem.ws.rest.v1.provider.JvmIdsParameterProvider;
import com.siemens.cto.aem.ws.rest.v1.service.jvm.impl.JsonControlJvm;
import com.siemens.cto.aem.ws.rest.v1.service.jvm.impl.JsonCreateJvm;
import com.siemens.cto.aem.ws.rest.v1.service.jvm.impl.JsonUpdateJvm;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/jvms")
@Produces(MediaType.APPLICATION_JSON)
public interface JvmServiceRest {

    @GET
    Response getJvms();

    @GET
    @Path("/{jvmId}")
    Response getJvm(@PathParam("jvmId") final Identifier<Jvm> aJvmId);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    Response createJvm(final JsonCreateJvm aJvmToCreate,
                       @BeanParam final AuthenticatedUser aUser);

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    Response updateJvm(final JsonUpdateJvm aJvmToUpdate,
                       @BeanParam final AuthenticatedUser aUser);

    @DELETE
    @Path("/{jvmId}")
    Response removeJvm(@PathParam("jvmId") final Identifier<Jvm> aJvmId,
                       @BeanParam final AuthenticatedUser aUser);

    @POST
    @Path("/{jvmId}/commands")
    Response controlJvm(@PathParam("jvmId") final Identifier<Jvm> aJvmId,
                        final JsonControlJvm aJvmToControl,
                        @BeanParam final AuthenticatedUser aUser);

    @GET
    @Path("/{jvmName}/conf")
    Response generateConfig(@PathParam("jvmName") final String aJvmName);

    @PUT
    @Path("/{jvmName}/conf")
    Response generateAndDeployConf(@PathParam("jvmName") final String jvmName,
                                   @BeanParam final AuthenticatedUser aUser);

    @PUT
    @Path("/{jvmName}/conf/{fileName}")
    Response generateAndDeployFile(@PathParam("jvmName") final String jvmName,
                                   @PathParam("fileName") final String fileName,
                                   @BeanParam final AuthenticatedUser aUser);
    
    @POST
    @Path("/{jvmName}/resources/uploadTemplate")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    Response uploadConfigTemplate(@PathParam("jvmName") final String jvmName,
                                  @BeanParam final AuthenticatedUser aUser,
                                  @QueryParam("templateName") final String templateName);

    @GET
    @Path("/states/current")
    // TODO: This should be reconciled with pagination, and with how to retrieve the states for every jvm without having to explicitly specify them
    Response getCurrentJvmStates(@BeanParam final JvmIdsParameterProvider jvmIdsParameterProvider);

    /**
     * Initiate a heartbeat followed by an SSH check
     * @param aJvmId id of the jvm to diagnose
     * @return A text response indicating whether the diagnose process was initiated.
     */
    @GET
    @Path("/{jvmId}/diagnosis")
    Response diagnoseJvm(@PathParam("jvmId") final Identifier<Jvm> aJvmId);

    @GET
    @Path("/{jvmName}/resources/name")
    Response getResourceNames(@PathParam("jvmName") final String jvmName);

    /**
     * Get resource template content.
     * @param jvmName JVM name.
     * @param resourceTemplateName the resource template name.
     * @param tokensReplaced flag that indicates whether to fetch the template with its tokens replaced by their mapped values from the db.
     * @return the template contents
     */
    @GET
    @Path("/{jvmName}/resources/template/{resourceTemplateName}")
    Response getResourceTemplate(@PathParam("jvmName") final String jvmName,
                                 @PathParam("resourceTemplateName") final String resourceTemplateName,
                                 @QueryParam("tokensReplaced") final boolean tokensReplaced);

    @PUT
    @Path("/{jvmName}/resources/template/{resourceTemplateName}")
    @Consumes(MediaType.TEXT_PLAIN)
    // TODO: Pass authenticated user.
    Response updateResourceTemplate(@PathParam("jvmName") final String jvmName,
                                    @PathParam("resourceTemplateName") final String resourceTemplateName,
                                    final String content);

    /**
     * Request a preview a resource file.
     * @param jvmName the JVM name
     * @param groupName a group name
     * @param template a template
     * @return
     */
    @PUT
    @Path("/{jvmName}/resources/preview")
    @Consumes(MediaType.TEXT_PLAIN)
    Response previewResourceTemplate(@PathParam("jvmName") String jvmName,
                                     @MatrixParam("groupName") String groupName,
                                     String template);

}
