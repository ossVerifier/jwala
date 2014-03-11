package com.siemens.cto.aem.ws.rest;

import com.siemens.cto.aem.ws.rest.parameter.JvmInfoBean;

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
    @Consumes(MediaType.APPLICATION_JSON)
    Response addJvmInfo(JvmInfoBean jvmInfoBean);

    @PUT
    @Path("/jvms/{id}/{jvmName}/{hostName}")
    Response updateJvmInfo(@PathParam("id") Long id,
                           @PathParam("jvmName") String jvmName,
                           @PathParam("hostName") String hostName);

    @PUT
    @Path("/jvms/{id}/{jvmName}/{hostName}/{groupName}")
    Response updateJvmInfo(@PathParam("id") Long id,
                           @PathParam("jvmName") String jvmName,
                           @PathParam("hostName") String hostName,
                           @PathParam("groupName") String groupName);

    @DELETE
    @Path("/jvms/{id}")
    Response deleteJvm(@PathParam("id") Long id);

    @GET
    @Path("/jvm")
    Response getJvmInfoByName(@QueryParam("name") String name);

}
