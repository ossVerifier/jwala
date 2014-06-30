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
    @Path("/{webServerName}/httpd/conf")
    Response generateHttpdConfig(@PathParam("webServerName") final String aWebServerName);

    @GET
    @Path("/{webServerName}/worker/properties")
    Response generateWorkerProperties(@PathParam("webServerName") final String aWebServerName,
                                      @QueryParam("loadBalancerPortType") final String loadBalancerPortType,
                                      @QueryParam("stickySessionCount") final Integer stickySessionCount,
                                      @QueryParam("loadBalancerType") final String loadBalancerType,
                                      @QueryParam("workerStatusCssPath") final String workerStatusCssPath);

}
