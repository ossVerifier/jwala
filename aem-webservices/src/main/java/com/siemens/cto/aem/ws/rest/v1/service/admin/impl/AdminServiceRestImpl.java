package com.siemens.cto.aem.ws.rest.v1.service.admin.impl;

import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.ws.rest.v1.response.ResponseBuilder;
import com.siemens.cto.aem.ws.rest.v1.service.admin.AdminServiceRest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

public class AdminServiceRestImpl implements AdminServiceRest {

    @GET
    @Path("/properties/reload")
    public Response reload() {
        ApplicationProperties.reload();
        return ResponseBuilder.ok(ApplicationProperties.getProperties());
    }

    @GET
    @Path("/properties/view")
    public Response view() {
        return ResponseBuilder.ok(ApplicationProperties.getProperties());
    }
}
