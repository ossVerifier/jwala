package com.siemens.cto.aem.ws.rest.v1.service.webserver;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.ws.rest.v1.provider.PaginationParamProvider;
import com.siemens.cto.aem.ws.rest.v1.service.webserver.impl.JsonControlWebServer;
import com.siemens.cto.aem.ws.rest.v1.service.webserver.impl.JsonCreateWebServer;
import com.siemens.cto.aem.ws.rest.v1.service.webserver.impl.JsonUpdateWebServer;

@Path("/webservers")
@Produces(MediaType.APPLICATION_JSON)
public interface WebServerServiceRest {

    @GET
    Response getWebServers(@BeanParam final PaginationParamProvider paginationParamProvider);

    @GET
    @Path("/{webserverId}")
    Response getWebServer(@PathParam("webserverId") final Identifier<WebServer> aWebServerId);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    Response createWebServer(final JsonCreateWebServer aWebServerToCreate);

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    Response updateWebServer(final JsonUpdateWebServer aWebServerToUpdate);

    @DELETE
    @Path("/{webserverId}")
    Response removeWebServer(@PathParam("webserverId") final Identifier<WebServer> aWebServerId);

    @POST
    @Path("/{webServerId}/commands")
    Response controlWebServer(@PathParam("webServerId") final Identifier<WebServer> aWebServerId,
                        final JsonControlWebServer aWebServerToControl);

    @GET
    @Path("/{webServerName}/conf")
    Response generateConfig(@PathParam("webServerName") final String aWebServerName,
                            @QueryParam("ssl") final Boolean withSsl);

    @GET
    @Path("/{webServerName}/loadbalancer/conf")
    Response generateLoadBalancerConfig(@PathParam("webServerName") final String aWebServerName);

}
