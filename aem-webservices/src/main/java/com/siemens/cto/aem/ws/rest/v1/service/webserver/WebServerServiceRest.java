package com.siemens.cto.aem.ws.rest.v1.service.webserver;

import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.ws.rest.v1.provider.AuthenticatedUser;
import com.siemens.cto.aem.ws.rest.v1.provider.WebServerIdsParameterProvider;
import com.siemens.cto.aem.ws.rest.v1.service.webserver.impl.JsonControlWebServer;
import com.siemens.cto.aem.ws.rest.v1.service.webserver.impl.JsonCreateWebServer;
import com.siemens.cto.aem.ws.rest.v1.service.webserver.impl.JsonUpdateWebServer;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/webservers")
@Produces(MediaType.APPLICATION_JSON)
public interface WebServerServiceRest {

    @GET
    Response getWebServers(@QueryParam("groupId") final Identifier<Group> aGroupId);

    @GET
    @Path("/{webserverId}")
    Response getWebServer(@PathParam("webserverId") final Identifier<WebServer> aWebServerId);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    Response createWebServer(final JsonCreateWebServer aWebServerToCreate,
                             @BeanParam final AuthenticatedUser aUser);

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    Response updateWebServer(final JsonUpdateWebServer aWebServerToUpdate,
                             @BeanParam final AuthenticatedUser aUser);

    @DELETE
    @Path("/{webserverId}")
    Response removeWebServer(@PathParam("webserverId") final Identifier<WebServer> aWebServerId);

    @POST
    @Path("/{webServerId}/commands")
    Response controlWebServer(@PathParam("webServerId") final Identifier<WebServer> aWebServerId,
                              final JsonControlWebServer aWebServerToControl,
                              @BeanParam final AuthenticatedUser aUser);

    @GET
    @Path("/{webServerName}/conf")
    Response generateConfig(@PathParam("webServerName") final String aWebServerName,
                            @QueryParam("ssl") final Boolean withSsl);

    @PUT
    @Path("/{webServerName}/conf")
    @Consumes(MediaType.APPLICATION_JSON)
    Response generateAndDeployConfig(@PathParam("webServerName") final String aWebServerName);

    @GET
    @Path("/{webServerName}/loadbalancer/conf")
    Response generateLoadBalancerConfig(@PathParam("webServerName") final String aWebServerName);

    @GET
    @Path("/states/current")
    Response getCurrentWebServerStates(@BeanParam final WebServerIdsParameterProvider webServerIdsParameterProvider);

    @GET
    @Path("/{webServerId}/conf/current")
    Response getHttpdConfig(@PathParam("webServerId") final Identifier<WebServer> aWebServerId);

    @GET
    @Path("/{wsName}/resources/name")
    Response getResourceNames(@PathParam("wsName") final String wsName);

}
