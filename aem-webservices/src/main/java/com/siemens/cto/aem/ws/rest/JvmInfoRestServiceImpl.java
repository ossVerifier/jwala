package com.siemens.cto.aem.ws.rest;

import javax.ws.rs.FormParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

public class JvmInfoRestServiceImpl implements JvmInfoRestService {

    @Override
    public Response getJvmInfoById(@QueryParam("id") Long id) {
        return null;
    }

    @Override
    public Response getAllJvmInfo() {
        return null;
    }

    @Override
    public Response addJvmInfo(@FormParam("jvmName") String jvmName,
                               @FormParam("hostName") String hostName) {
        return null;
    }

    @Override
    public Response updateJvmInfo(@FormParam("jvmId") Long jvmId,
                                  @FormParam("jvmName") String jvmName,
                                  @FormParam("hostName") String hostName) {
        return null;
    }

    @Override
    public Response deleteJvm(@QueryParam("id") Long id) {
        return null;
    }

}
