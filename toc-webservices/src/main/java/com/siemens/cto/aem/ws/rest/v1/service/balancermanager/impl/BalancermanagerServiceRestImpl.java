package com.siemens.cto.aem.ws.rest.v1.service.balancermanager.impl;

import com.siemens.cto.aem.common.domain.model.balancermanager.DrainStatus;
import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.exception.InternalErrorException;
import com.siemens.cto.aem.service.balancermanager.BalancermanagerService;
import com.siemens.cto.aem.service.group.GroupService;
import com.siemens.cto.aem.service.webserver.WebServerService;
import com.siemens.cto.aem.ws.rest.v1.response.ResponseBuilder;
import com.siemens.cto.aem.ws.rest.v1.service.balancermanager.BalancermanagerServiceRest;
import com.siemens.cto.aem.ws.rest.v1.service.webserver.impl.WebServerServiceRestImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.FormParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

public class BalancermanagerServiceRestImpl implements BalancermanagerServiceRest {

    private static final Logger LOGGER = LoggerFactory.getLogger(BalancermanagerServiceRestImpl.class);

    final WebServerService webServerService;
    final BalancermanagerService balancermanagerService;
    final GroupService groupService;

    public BalancermanagerServiceRestImpl(final BalancermanagerService balancermanagerService,
                                          final WebServerService webServerService,
                                          final GroupService groupService) {
        this.balancermanagerService = balancermanagerService;
        this.webServerService = webServerService;
        this.groupService = groupService;
    }

    @Override
    public Response drainUserGroup(final String groupName, final String webServers) {
        checkGroupStatus(groupName);
        DrainStatus drainStatus = balancermanagerService.drainUserGroup(groupName, webServers);
        return ResponseBuilder.ok(drainStatus);
    }

    @Override
    public Response drainUserWebServer(final String groupName, final String webServerName) {
        checkStatus(webServerService.getWebServer(webServerName));
        DrainStatus drainStatus = balancermanagerService.drainUserWebServer(groupName, webServerName);
        return ResponseBuilder.ok(drainStatus);
    }

    @Override
    public Response getGroup(final String groupName) {
        DrainStatus drainStatus = balancermanagerService.getGroupDrainStatus(groupName);
        return ResponseBuilder.ok(drainStatus);
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    public void checkGroupStatus(final String groupName) {
        final Group group = groupService.getGroup(groupName);
        List<WebServer> webServerList = webServerService.findWebServers(group.getId());
        for (WebServer webServer : webServerList) {
            if (!webServerService.isStarted(webServer)) {
                final String message = "The target Web Server " + webServer.getName() + " in group " + groupName + " must be start before attempting to drain user";
                LOGGER.error(message);
                throw new InternalErrorException(AemFaultType.INVALID_WEBSERVER_OPERATION, message);
            }
        }
    }

    public void checkStatus(WebServer webServer) {
        if (!webServerService.isStarted(webServer)) {
            final String message = "The target Web Server " + webServer.getName() + " must be start before attempting to drain user";
            LOGGER.error(message);
            throw new InternalErrorException(AemFaultType.INVALID_WEBSERVER_OPERATION, message);
        }
    }
}
