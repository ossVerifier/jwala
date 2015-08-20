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
    Response removeJvm(@PathParam("jvmId") final Identifier<Jvm> aJvmId);

    @POST
    @Path("/{jvmId}/commands")
    Response controlJvm(@PathParam("jvmId") final Identifier<Jvm> aJvmId,
                        final JsonControlJvm aJvmToControl,
                        @BeanParam final AuthenticatedUser aUser);

    @GET
    @Path("/{jvmName}/conf")
    Response generateConfig(@PathParam("jvmName") final String aJvmName);

    // todo make @put and rename deployconf to conf just like in webserverservicerest.java when connecting the UI gear button
    @GET
    @Path("/{jvmId}/deployConf")
    Response generateAndDeployConfig(@PathParam("jvmId") final Identifier<Jvm> aJvmId,
                                     @BeanParam final AuthenticatedUser aUser);

    @POST
    @Path("/{jvmId}/serverXml")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    Response uploadServerXMLTemplate(@PathParam("jvmId") final Identifier<Jvm> aJvmId,
                                     @BeanParam final AuthenticatedUser aUser);

    @GET
    @Path("/states/current")
//    TODO This should be reconciled with pagination, and with how to retrieve the states for every jvm without having to explicitly specify them
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

}
