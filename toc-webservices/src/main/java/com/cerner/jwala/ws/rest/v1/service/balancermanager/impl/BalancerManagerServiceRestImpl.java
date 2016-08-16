package com.cerner.jwala.ws.rest.v1.service.balancermanager.impl;

import com.cerner.jwala.common.domain.model.balancermanager.BalancerManagerState;
import com.cerner.jwala.service.balancermanager.BalancerManagerService;
import com.cerner.jwala.ws.rest.v1.response.ResponseBuilder;
import com.cerner.jwala.ws.rest.v1.service.balancermanager.BalancerManagerServiceRest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

public class BalancerManagerServiceRestImpl implements BalancerManagerServiceRest {

    final BalancerManagerService balancerManagerService;

    public BalancerManagerServiceRestImpl(final BalancerManagerService balancerManagerService) {
        this.balancerManagerService = balancerManagerService;
    }

    @Override
    public Response drainUserGroup(final String groupName, final String webServers) {
        BalancerManagerState balancerManagerState = balancerManagerService.drainUserGroup(groupName, webServers, getUser());
        return ResponseBuilder.ok(balancerManagerState);
    }

    @Override
    public Response drainUserWebServer(final String groupName, final String webServerName) {
        BalancerManagerState balancerManagerState = balancerManagerService.drainUserWebServer(groupName, webServerName, getUser());
        return ResponseBuilder.ok(balancerManagerState);
    }

    @Override
    public Response drainUserJvm(final String groupName, final String hostName, final String jvmName) {
        BalancerManagerState balancerManagerState = balancerManagerService.drainUserJvm(groupName, hostName, jvmName, getUser());
        return ResponseBuilder.ok(balancerManagerState);
    }

    @Override
    public Response getGroup(final String groupName) {
        BalancerManagerState balancerManagerState = balancerManagerService.getGroupDrainStatus(groupName, getUser());
        return ResponseBuilder.ok(balancerManagerState);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    }

    public String getUser(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }
}
