package com.siemens.cto.aem.ws.rest.v1.service.balancermanager.impl;

import com.siemens.cto.aem.common.domain.model.balancermanager.BalancerManagerState;
import com.siemens.cto.aem.service.balancermanager.BalancermanagerService;
import com.siemens.cto.aem.ws.rest.v1.provider.AuthenticatedUser;
import com.siemens.cto.aem.ws.rest.v1.response.ResponseBuilder;
import com.siemens.cto.aem.ws.rest.v1.service.balancermanager.BalancermanagerServiceRest;

import javax.ws.rs.core.Response;

public class BalancermanagerServiceRestImpl implements BalancermanagerServiceRest {

    final BalancermanagerService balancermanagerService;

    public BalancermanagerServiceRestImpl(final BalancermanagerService balancermanagerService) {
        this.balancermanagerService = balancermanagerService;
    }

    @Override
    public Response drainUserGroup(final String groupName, final String webServers, final AuthenticatedUser authenticatedUser) {
        BalancerManagerState balancerManagerState = balancermanagerService.drainUserGroup(groupName, webServers, authenticatedUser.getUser());
        return ResponseBuilder.ok(balancerManagerState);
    }

    @Override
    public Response drainUserWebServer(final String groupName, final String webServerName, final AuthenticatedUser authenticatedUser) {
        BalancerManagerState balancerManagerState = balancermanagerService.drainUserWebServer(groupName, webServerName, authenticatedUser.getUser());
        return ResponseBuilder.ok(balancerManagerState);
    }

    @Override
    public Response getGroup(final String groupName, final AuthenticatedUser authenticatedUser) {
        BalancerManagerState balancerManagerState = balancermanagerService.getGroupDrainStatus(groupName, authenticatedUser.getUser());
        return ResponseBuilder.ok(balancerManagerState);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    }
}
