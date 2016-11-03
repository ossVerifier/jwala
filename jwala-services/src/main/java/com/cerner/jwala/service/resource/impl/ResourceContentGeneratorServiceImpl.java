package com.cerner.jwala.service.resource.impl;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.app.ApplicationState;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmState;
import com.cerner.jwala.common.domain.model.resource.ResourceGroup;
import com.cerner.jwala.common.domain.model.state.CurrentState;
import com.cerner.jwala.common.domain.model.state.StateType;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.domain.model.webserver.WebServerReachableState;
import com.cerner.jwala.persistence.jpa.type.EventType;
import com.cerner.jwala.persistence.service.ApplicationPersistenceService;
import com.cerner.jwala.persistence.service.GroupPersistenceService;
import com.cerner.jwala.persistence.service.JvmPersistenceService;
import com.cerner.jwala.persistence.service.WebServerPersistenceService;
import com.cerner.jwala.service.HistoryService;
import com.cerner.jwala.service.MessagingService;
import com.cerner.jwala.service.resource.ResourceContentGeneratorService;
import com.cerner.jwala.template.ResourceFileGenerator;
import com.cerner.jwala.template.exception.ResourceFileGeneratorException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Implement {@link ResourceContentGeneratorService}
 * <p>
 * Created by JC043760 on 7/26/2016.
 */
@Service
public class ResourceContentGeneratorServiceImpl implements ResourceContentGeneratorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceContentGeneratorServiceImpl.class);
    private final GroupPersistenceService groupPersistenceService;
    private final WebServerPersistenceService webServerPersistenceService;
    private final JvmPersistenceService jvmPersistenceService;
    private final ApplicationPersistenceService applicationPersistenceService;
    private final HistoryService historyService;
    private final MessagingService messagingService;

    @Autowired
    public ResourceContentGeneratorServiceImpl(final GroupPersistenceService groupPersistenceService,
                                               final WebServerPersistenceService webServerPersistenceService,
                                               final JvmPersistenceService jvmPersistenceService,
                                               final ApplicationPersistenceService applicationPersistenceService,
                                               final HistoryService historyService,
                                               final MessagingService messagingService) {
        this.groupPersistenceService = groupPersistenceService;
        this.webServerPersistenceService = webServerPersistenceService;
        this.jvmPersistenceService = jvmPersistenceService;
        this.applicationPersistenceService = applicationPersistenceService;
        this.historyService = historyService;
        this.messagingService = messagingService;
    }

    @Override
    public <T> String generateContent(final String fileName, final String template, final ResourceGroup resourceGroup, final T entity, ResourceGeneratorType resourceGeneratorType) {
        try {
            return ResourceFileGenerator.generateResourceConfig(fileName, template, null == resourceGroup ? generateResourceGroup() : resourceGroup, entity);
        } catch (ResourceFileGeneratorException e) {
            final String logMessage = resourceGeneratorType.name() + ": " + e.getMessage();
            LOGGER.error(logMessage, e);
            final String userName = SecurityContextHolder.getContext().getAuthentication().getName();
            if (!resourceGeneratorType.equals(ResourceGeneratorType.PREVIEW)) {
                if (entity instanceof WebServer) {
                    WebServer webServer = (WebServer) entity;
                    messagingService.send(new CurrentState<>(webServer.getId(), WebServerReachableState.WS_FAILED, DateTime.now(), StateType.WEB_SERVER, logMessage, userName));
                    historyService.createHistory("Web Server " + webServer.getName(), new ArrayList<Group>(webServer.getGroups()), logMessage, EventType.SYSTEM_ERROR, userName);
                } else if (entity instanceof Jvm) {
                    Jvm jvm = (Jvm) entity;
                    messagingService.send(new CurrentState<>(jvm.getId(), JvmState.JVM_FAILED, DateTime.now(), StateType.JVM, logMessage, userName));
                    historyService.createHistory("JVM " + jvm.getJvmName(), new ArrayList<Group>(jvm.getGroups()), logMessage, EventType.SYSTEM_ERROR, userName);
                } else {
                    Application application = (Application) entity;
                    messagingService.send(new CurrentState<>(application.getId(), ApplicationState.FAILED, DateTime.now(), StateType.APPLICATION, logMessage, userName));
                    ArrayList<Group> groups = new ArrayList<Group>();
                    groups.add(application.getGroup());
                    historyService.createHistory("App " + application.getName(), groups, logMessage, EventType.SYSTEM_ERROR, userName);
                }
            }
            throw new ResourceFileGeneratorException(logMessage, e);
        }
    }

    /**
     * Create pertinent data to pass to the template generator engine
     *
     * @return {@link ResourceGroup}
     */
    private ResourceGroup generateResourceGroup() {
        final List<Group> groups = groupPersistenceService.getGroups();
        List<Group> groupsToBeAdded = null;

        for (Group group : groups) {
            if (groupsToBeAdded == null) {
                groupsToBeAdded = new ArrayList<>(groups.size());
            }
            final List<Jvm> jvms = jvmPersistenceService.getJvmsAndWebAppsByGroupName(group.getName());
            final List<WebServer> webServers = webServerPersistenceService.getWebServersByGroupName(group.getName());
            final List<Application> applications = applicationPersistenceService.findApplicationsBelongingTo(group.getName());
            groupsToBeAdded.add(new Group(group.getId(),
                    group.getName(),
                    null != jvms ? new LinkedHashSet<>(jvms) : new LinkedHashSet<Jvm>(),
                    null != webServers ? new LinkedHashSet<>(webServers) : new LinkedHashSet<WebServer>(),
                    group.getCurrentState(),
                    group.getHistory(),
                    null != applications ? new LinkedHashSet<>(applications) : new LinkedHashSet<Application>()));
        }
        return new ResourceGroup(groupsToBeAdded);
    }
}
