package com.siemens.cto.aem.ws.rest.v1.service;

import javax.ws.rs.*;
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

    /**
     * Retrieve history data.
     * @param groupName the group name
     * @param numOfRec The Number of records to fetch. If null, all records are retrieved.
     * @return {@link Response} containing history data.
     */
    @GET
    @Path("/{groupName}")
    Response findHistory(@PathParam("groupName") String groupName, @QueryParam("numOfRec") Integer numOfRec);

}
