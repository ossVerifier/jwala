package com.siemens.cto.aem.ws.rest;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Produces(MediaType.APPLICATION_JSON)
public interface JvmInfoRestService {

    @GET
    @Path("/jvms/{id}")
    Response getJvmInfoById(@PathParam("id") Long id);

    @GET
    @Path("/jvms")
    Response getAllJvmInfo();

    @POST
    @Path("/jvms")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    Response addJvmInfo(@FormParam("jvmName") String jvmName,
                        @FormParam("hostName") String hostName,
                        @FormParam("groupName") String groupName);

    @PUT
    @Path("/jvms/{id}/{jvmName}/{hostName}")
    Response updateJvmInfo(@PathParam("id") Long id,
                           @PathParam("jvmName") String jvmName,
                           @PathParam("hostName") String hostName);

    @DELETE
    @Path("/jvms/{id}")
    Response deleteJvm(@PathParam("id") Long id);

}
