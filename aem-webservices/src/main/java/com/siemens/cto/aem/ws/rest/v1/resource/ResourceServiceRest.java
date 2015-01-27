package com.siemens.cto.aem.ws.rest.v1.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/resources")
@Produces(MediaType.APPLICATION_JSON)
public interface ResourceServiceRest {

    @GET
    @Path("/types")
    Response getAll();

}
