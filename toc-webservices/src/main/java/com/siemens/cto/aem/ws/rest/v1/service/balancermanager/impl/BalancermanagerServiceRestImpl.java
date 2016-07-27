package com.siemens.cto.aem.ws.rest.v1.service.balancermanager.impl;

import com.siemens.cto.aem.service.balancermanager.BalancermanagerService;
import com.siemens.cto.aem.ws.rest.v1.service.balancermanager.BalancermanagerServiceRest;

import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

public class BalancermanagerServiceRestImpl implements BalancermanagerServiceRest {

    final BalancermanagerService balancermanagerService;

    public BalancermanagerServiceRestImpl(final BalancermanagerService balancermanagerService){
        this.balancermanagerService = balancermanagerService;
    }

    @Override
    public Response drainUserGroup(@PathParam("groupName") String groupName) {
        balancermanagerService.drainUserGroup(groupName);
        //TODO: return response
        return null;
    }

    @Override
    public Response drainUserWebServer(@PathParam("groupName") String groupName, @PathParam("webserverName") String webserverName) {
        balancermanagerService.drainUserWebServer(groupName, webserverName);
        //TODO: return response
        return null;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
