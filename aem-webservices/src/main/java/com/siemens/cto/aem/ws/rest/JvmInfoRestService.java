package com.siemens.cto.aem.ws.rest;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

public interface JvmInfoRestService {

    @GET
    @Path("/get/jvm")
    @Produces("application/json")
    Response getJvmInfoById(@QueryParam("id") Long id);

    @GET
    @Path("/get/jvm/all")
    @Produces("application/json")
    Response getAllJvmInfo();

    @POST
    @Path("/add/jvm")
    @Produces("application/json")
    Response addJvmInfo(@FormParam("jvmName") String jvmName, @FormParam("hostName") String hostName);

    @POST
    @Path("/update/jvm")
    @Produces("application/json")
    Response updateJvmInfo(@FormParam("jvmId") Long jvmId, @FormParam("jvmName") String jvmName,
                                  @FormParam("hostName") String hostName);

    @DELETE
    @Path("/delete/jvm")
    @Produces("application/json")
    Response deleteJvm(@QueryParam("id") Long id);

}
