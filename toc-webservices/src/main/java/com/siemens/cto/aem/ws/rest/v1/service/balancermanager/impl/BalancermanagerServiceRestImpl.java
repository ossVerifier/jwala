package com.siemens.cto.aem.ws.rest.v1.service.balancermanager.impl;

import com.siemens.cto.aem.service.balancermanager.BalancermanagerService;
import com.siemens.cto.aem.ws.rest.v1.response.ResponseBuilder;
import com.siemens.cto.aem.ws.rest.v1.service.balancermanager.BalancermanagerServiceRest;

import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

public class BalancermanagerServiceRestImpl implements BalancermanagerServiceRest {

    final BalancermanagerService balancermanagerService;

    public BalancermanagerServiceRestImpl(final BalancermanagerService balancermanagerService){
        this.balancermanagerService = balancermanagerService;
    }

    @Override
    public Response drainUserGroup(final String groupName) {
        balancermanagerService.drainUserGroup(groupName);
        return ResponseBuilder.ok();
    }

    @Override
    public Response drainUserWebServer(final String groupName, final String webServerName) {
        balancermanagerService.drainUserWebServer(groupName, webServerName);
        return ResponseBuilder.ok();
    }

    @Override
    public Response getGroup(@PathParam("groupName") String groupName) {

        return null;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
