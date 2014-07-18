package com.siemens.cto.aem.service.dispatch.impl;

import java.util.ArrayList;
import java.util.List;

import com.siemens.cto.aem.domain.model.dispatch.GroupWebServerDispatchCommand;
import com.siemens.cto.aem.domain.model.dispatch.WebServerDispatchCommand;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.service.webserver.WebServerService;

public class GroupWebServerSplitterBean {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(GroupWebServerSplitterBean.class);

    private WebServerService webServerService;

    public GroupWebServerSplitterBean(final WebServerService webServerService) {
        this.webServerService = webServerService;
    }
    
    public List<WebServerDispatchCommand> split(GroupWebServerDispatchCommand groupDispatchCommand) {
        
        Group group = groupDispatchCommand.getGroup();
        
        LOGGER.debug("splitting GroupJvmDispatchCommand for group {}", group.getName());

        List<WebServerDispatchCommand> webServerCommands = new ArrayList<WebServerDispatchCommand>();

        List<WebServer> webServers = webServerService.findWebServers(group.getId(), PaginationParameter.all());
//        if (webServers == null) {
//            // temporary hack for testing.  My mock isn't working
//            webServers = new ArrayList<WebServer>();
//            Identifier<WebServer> identifier = new Identifier<WebServer>(new Long(555));
//            Collection<Group> groups = new ArrayList<Group>();
//            groups.add(group);
//            WebServer ws = new WebServer(identifier, groups, "someGroup", "localhost", 8080, 8443);
//            webServers.add(ws);
//        }
        for (WebServer webServer : webServers) {
            webServerCommands.add(new WebServerDispatchCommand(webServer, groupDispatchCommand));
            LOGGER.debug("Created dispatch command for WebServer {}", webServer.getName());
        }

        return webServerCommands;
    }
}
