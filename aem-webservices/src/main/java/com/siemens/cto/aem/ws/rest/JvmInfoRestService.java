package com.siemens.cto.aem.ws.rest;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Produces(MediaType.APPLICATION_JSON)
public interface JvmInfoRestService {

    @GET
    @Path("/get/jvm/{id}")
    Response getJvmInfoById(@PathParam("id") Long id);

    @GET
    @Path("/get/jvm/all")
    Response getAllJvmInfo();

    @POST
    @Path("/add/jvm")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    Response addJvmInfo(@FormParam("jvmName") String jvmName, @FormParam("hostName") String hostName);

    @POST
    @Path("/update/jvm")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    Response updateJvmInfo(@FormParam("jvmId") Long jvmId, @FormParam("jvmName") String jvmName,
                                  @FormParam("hostName") String hostName);

    @DELETE
    @Path("/delete/jvm/{id}")
    Response deleteJvm(@PathParam("id") Long id);

}
