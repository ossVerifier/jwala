package com.siemens.cto.aem.ws.rest.v1.service.admin.impl;

import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.ws.rest.v1.response.ResponseBuilder;
import com.siemens.cto.aem.ws.rest.v1.service.admin.AdminServiceRest;

import javax.ws.rs.core.Response;

public class AdminServiceRestImpl implements AdminServiceRest {

    @Override
    public Response reload() {
        ApplicationProperties.reload();
        return ResponseBuilder.ok(ApplicationProperties.getProperties());
    }

    @Override
    public Response view() {
        return ResponseBuilder.ok(ApplicationProperties.getProperties());
    }
}
