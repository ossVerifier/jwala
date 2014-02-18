package com.siemens.cto.aem.ws.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

public interface JvmInfoService {

    @GET
    @Path("/jvms")
    @Produces("application/xml")
    public Response getJvmInfoList();

}
