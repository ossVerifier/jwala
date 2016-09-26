package com.cerner.jwala.ws.rest.v1.service.balancermanager.impl;

import com.cerner.jwala.common.domain.model.balancermanager.BalancerManagerState;
import com.cerner.jwala.common.domain.model.fault.AemFaultType;
import com.cerner.jwala.common.exception.FaultCodeException;
import com.cerner.jwala.common.exception.InternalErrorException;
import com.cerner.jwala.service.balancermanager.BalancerManagerService;
import com.cerner.jwala.ws.rest.v1.response.ResponseBuilder;
import com.cerner.jwala.ws.rest.v1.service.balancermanager.BalancerManagerServiceRest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.ws.rs.core.Response;

public class BalancerManagerServiceRestImpl implements BalancerManagerServiceRest {

    final BalancerManagerService balancerManagerService;
    private static final Logger LOGGER = LoggerFactory.getLogger(BalancerManagerServiceRestImpl.class);

    public BalancerManagerServiceRestImpl(final BalancerManagerService balancerManagerService) {
        this.balancerManagerService = balancerManagerService;
    }

    @Override
    public Response drainUserGroup(final String groupName, final String webServerNames) {
        try {
            BalancerManagerState balancerManagerState = balancerManagerService.drainUserGroup(groupName, webServerNames, getUser());
            return ResponseBuilder.ok(balancerManagerState);
        } catch (InternalErrorException iee) {
            LOGGER.error("The target Web Server " + webServerNames + " in group " + groupName + " must be STARTED before attempting to drain users");
            final String message = "The target Web Server " + webServerNames + " in group " + groupName + " must be STARTED before attempting to drain users";
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    AemFaultType.INVALID_WEBSERVER_OPERATION, message + " " + iee.getMessage(), iee));
        }
    }

    @Override
    public Response drainUserWebServer(final String groupName, final String webServerName, final String jvmNames) {
        try {
            BalancerManagerState balancerManagerState = balancerManagerService.drainUserWebServer(groupName, webServerName, jvmNames, getUser());
            return ResponseBuilder.ok(balancerManagerState);
        } catch (InternalErrorException iee) {
            LOGGER.error(iee.getMessage());
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    AemFaultType.INVALID_WEBSERVER_OPERATION, iee.getMessage(), iee));
        }
    }

    @Override
    public Response drainUserJvm(final String jvmName) {
        BalancerManagerState balancerManagerState = balancerManagerService.drainUserJvm(jvmName, getUser());
        return ResponseBuilder.ok(balancerManagerState);
    }

    @Override
    public Response drainUserGroupJvm(final String groupName, final String jvmName) {
        try {
            BalancerManagerState balancerManagerState = balancerManagerService.drainUserGroupJvm(groupName, jvmName, getUser());
            return ResponseBuilder.ok(balancerManagerState);
        } catch (InternalErrorException iee) {
            LOGGER.error(iee.getMessage());
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR, new FaultCodeException(
                    AemFaultType.INVALID_WEBSERVER_OPERATION, iee.getMessage(), iee));
        }
    }

    @Override
    public Response getGroup(final String groupName) {
        BalancerManagerState balancerManagerState = balancerManagerService.getGroupDrainStatus(groupName, getUser());
        return ResponseBuilder.ok(balancerManagerState);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    }

    public String getUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }
}
