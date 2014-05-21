package com.siemens.cto.aem.ws.rest.v1.service.webserver;

import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
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

}
