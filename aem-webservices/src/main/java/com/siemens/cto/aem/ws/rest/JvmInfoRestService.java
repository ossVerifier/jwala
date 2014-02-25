package com.siemens.cto.aem.ws.rest;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Produces(MediaType.APPLICATION_JSON)
public interface JvmInfoRestService {

    @GET
    @Path("/jvm/{id}")
    Response getJvmInfoById(@PathParam("id") Long id);

    @GET
    @Path("/jvm/all")
    Response getAllJvmInfo();

    @POST
    @Path("/jvm")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    Response addJvmInfo(@FormParam("jvmName") String jvmName, @FormParam("hostName") String hostName);

    @PUT
    @Path("/jvm")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    Response updateJvmInfo(@FormParam("jvmId") Long jvmId,
                           @FormParam("jvmName") String jvmName,
                           @FormParam("hostName") String hostName);

    @DELETE
    @Path("/jvm/{id}")
    Response deleteJvm(@PathParam("id") Long id);

}
