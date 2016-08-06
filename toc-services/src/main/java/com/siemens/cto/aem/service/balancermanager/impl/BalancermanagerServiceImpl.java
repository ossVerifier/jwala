package com.siemens.cto.aem.service.balancermanager.impl;

import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.domain.model.balancermanager.DrainStatus;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.service.HistoryService;
import com.siemens.cto.aem.service.MessagingService;
import com.siemens.cto.aem.service.app.ApplicationService;
import com.siemens.cto.aem.service.balancermanager.BalancermanagerService;
import com.siemens.cto.aem.service.group.GroupService;
import com.siemens.cto.aem.service.webserver.WebServerService;
import com.siemens.cto.aem.service.webserver.component.ClientFactoryHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class BalancermanagerServiceImpl extends BalancermanagerCommon implements BalancermanagerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BalancermanagerServiceImpl.class);

    private GroupService groupService;
    private ApplicationService applicationService;
    private WebServerService webServerService;

    public BalancermanagerServiceImpl(final GroupService groupService,
                                      final ApplicationService applicationService,
                                      final WebServerService webServerService,
                                      final ClientFactoryHelper clientFactoryHelper,
                                      final MessagingService messagingService,
                                      final HistoryService historyService) {
        super(clientFactoryHelper, messagingService, historyService);
        this.groupService = groupService;
        this.applicationService = applicationService;
        this.webServerService = webServerService;
    }

    @Override
    public DrainStatus drainUserGroup(final String groupName, final String webServers) {
        LOGGER.info("Entering drainUserGroup, groupName: " + groupName + " webServers: " + webServers);
        String[] webServerArray = getRequireWebServers(webServers);
        List<DrainStatus.WebServerDrainStatus> webServerDrainStatusList = new ArrayList<>();
        Group group = groupService.getGroup(groupName);
        for (Application application : applicationService.findApplications(group.getId())) {
            List<WebServer> webServerList;
            if (webServerArray.length == 0) {
                webServerList = webServerService.findWebServers(group.getId());
            } else {
                webServerList = findMatchWebServers(webServerService.findWebServers(group.getId()), webServerArray);
            }
            for (WebServer webServer : webServerList) {
                DrainStatus.WebServerDrainStatus webServerDrainStatus = doDrainAndgetDrainStatus(webServer, application, true);
                webServerDrainStatusList.add(webServerDrainStatus);
            }
        }
        return new DrainStatus(groupName, webServerDrainStatusList);
    }

    @Override
    public DrainStatus drainUserWebServer(final String groupName, final String webServerName) {
        LOGGER.info("Entering drainUserGroup, groupName: " + groupName + " webServerName: " + webServerName);
        List<DrainStatus.WebServerDrainStatus> webServerDrainStatusList = new ArrayList<>();
        Group group = groupService.getGroup(groupName);
        for (Application application : applicationService.findApplications(group.getId())) {
            WebServer webServer = webServerService.getWebServer(webServerName);
            DrainStatus.WebServerDrainStatus webServerDrainStatus = doDrainAndgetDrainStatus(webServer, application, true);
            webServerDrainStatusList.add(webServerDrainStatus);
        }
        return new DrainStatus(groupName, webServerDrainStatusList);
    }

    @Override
    public DrainStatus getGroupDrainStatus(String groupName) {
        LOGGER.info("Entering getGroupDrainStatus: " + groupName);
        List<DrainStatus.WebServerDrainStatus> webServerDrainStatusList = new ArrayList<>();
        Group group = groupService.getGroup(groupName);
        for (Application application : applicationService.findApplications(group.getId())) {
            for (WebServer webServer : webServerService.findWebServers(group.getId())) {
                DrainStatus.WebServerDrainStatus webServerDrainStatus = doDrainAndgetDrainStatus(webServer, application, false);
                webServerDrainStatusList.add(webServerDrainStatus);
            }
        }
        return new DrainStatus(groupName, webServerDrainStatusList);
    }

}
