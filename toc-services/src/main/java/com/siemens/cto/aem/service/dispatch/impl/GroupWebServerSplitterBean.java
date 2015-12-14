package com.siemens.cto.aem.service.dispatch.impl;

import com.siemens.cto.aem.common.dispatch.GroupWebServerDispatchCommand;
import com.siemens.cto.aem.common.dispatch.WebServerDispatchCommand;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.service.webserver.WebServerService;

import java.util.ArrayList;
import java.util.List;

public class GroupWebServerSplitterBean {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(GroupWebServerSplitterBean.class);

    private WebServerService webServerService;

    public GroupWebServerSplitterBean(final WebServerService webServerService) {
        this.webServerService = webServerService;
    }
    
    public List<WebServerDispatchCommand> split(GroupWebServerDispatchCommand groupDispatchCommand) {
        
        Group group = groupDispatchCommand.getGroup();
        
        LOGGER.debug("splitting GroupJvmDispatchCommand for group {}", group.getName());

        List<WebServerDispatchCommand> webServerCommands = new ArrayList<>();

        List<WebServer> webServers = webServerService.findWebServers(group.getId());

        for (WebServer webServer : webServers) {
            webServerCommands.add(new WebServerDispatchCommand(webServer, groupDispatchCommand));
            LOGGER.debug("Created dispatch command for WebServer {}", webServer.getName());
        }

        LOGGER.debug("end splitting GroupJvmDispatchCommand for group {}", group.getName());
        return webServerCommands;
    }
}
