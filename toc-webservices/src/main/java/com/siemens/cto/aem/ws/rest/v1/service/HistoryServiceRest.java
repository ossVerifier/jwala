package com.siemens.cto.aem.ws.rest.v1.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Restful history service contract.
 *
 * Created by JC043760 on 12/7/2015.
 */
@Path("/history")
@Produces(MediaType.APPLICATION_JSON)
public interface HistoryServiceRest {

    @GET
    @Path("/{groupName}")
    Response read(@PathParam("groupName") String groupName);

}
