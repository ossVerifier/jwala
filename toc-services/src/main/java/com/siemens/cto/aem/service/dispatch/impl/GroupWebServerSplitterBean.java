package com.siemens.cto.aem.service.dispatch.impl;

import com.siemens.cto.aem.common.dispatch.GroupWebServerDispatchCommand;
import com.siemens.cto.aem.common.dispatch.WebServerDispatchCommand;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.persistence.service.WebServerPersistenceService;

import java.util.ArrayList;
import java.util.List;

public class GroupWebServerSplitterBean {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(GroupWebServerSplitterBean.class);

    private final WebServerPersistenceService webServerPersistenceService;

    public GroupWebServerSplitterBean(final WebServerPersistenceService webServerPersistenceService) {
        this.webServerPersistenceService = webServerPersistenceService;
    }
    
    public List<WebServerDispatchCommand> split(final GroupWebServerDispatchCommand groupDispatchCommand) {
        final Group group = groupDispatchCommand.getGroup();
        final List<WebServerDispatchCommand> webServerCommands = new ArrayList<>();
        final List<WebServer> webServers;

        if (group == null) {
            webServers = webServerPersistenceService.getWebServers();
        } else {
            webServers = webServerPersistenceService.findWebServersBelongingTo(group.getId());
        }

        final String groupDesc = group == null ? "all groups" : group.getName() + " group";
        LOGGER.debug("Splitting groupDispatchCommand for {}: START", groupDesc);
        for (final WebServer webServer : webServers) {
            webServerCommands.add(new WebServerDispatchCommand(webServer, groupDispatchCommand));
            LOGGER.debug("Created dispatch command for WebServer {}", webServer.getName());
        }
        LOGGER.debug("Splitting groupDispatchCommand for {}: END", groupDesc);
        return webServerCommands;
    }
}
