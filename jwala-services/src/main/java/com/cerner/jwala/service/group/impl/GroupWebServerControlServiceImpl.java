package com.cerner.jwala.service.group.impl;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.common.request.webserver.ControlGroupWebServerRequest;
import com.cerner.jwala.common.request.webserver.ControlWebServerRequest;
import com.cerner.jwala.service.group.GroupService;
import com.cerner.jwala.service.group.GroupWebServerControlService;
import com.cerner.jwala.service.webserver.WebServerControlService;

import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GroupWebServerControlServiceImpl implements GroupWebServerControlService {

    private final GroupService groupService;
    private final WebServerControlService webServerControlService;
    private final ExecutorService executorService;

    public GroupWebServerControlServiceImpl(final GroupService theGroupService, WebServerControlService theWebServerControlService) {
        groupService = theGroupService;
        webServerControlService = theWebServerControlService;
        executorService = Executors.newFixedThreadPool(Integer.parseInt(ApplicationProperties.get("thread-task-executor.group-control.pool.size", "25")));
    }

    @Transactional
    @Override
    public void controlGroup(final ControlGroupWebServerRequest controlGroupWebServerRequest, final User aUser) {

        controlGroupWebServerRequest.validate();

        Group group = groupService.getGroupWithWebServers(controlGroupWebServerRequest.getGroupId());

        final Set<WebServer> webServers = group.getWebServers();
        if (webServers != null) {
            for (final WebServer webServer : webServers) {
                executorService.submit(new Callable<CommandOutput>() {
                    @Override
                    public CommandOutput call() throws Exception {
                        final ControlWebServerRequest controlWebServerRequest = new ControlWebServerRequest(webServer.getId(), controlGroupWebServerRequest.getControlOperation());
                        return webServerControlService.controlWebServer(controlWebServerRequest, aUser);
                    }
                });
            }
        }
    }

    @Override
    public void controlAllWebSevers(final ControlGroupWebServerRequest controlGroupWebServerRequest, final User user) {
        for (Group group : groupService.getGroups()) {
            controlGroup(new ControlGroupWebServerRequest(group.getId(), controlGroupWebServerRequest.getControlOperation()), user);
        }
    }

}
