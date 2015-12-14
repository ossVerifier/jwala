package com.siemens.cto.aem.service.group.impl;

import com.siemens.cto.aem.common.request.group.*;
import com.siemens.cto.aem.common.request.webserver.UploadWebServerTemplateRequest;
import com.siemens.cto.aem.common.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.common.domain.model.event.Event;
import com.siemens.cto.aem.common.domain.model.group.*;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.request.jvm.UploadJvmTemplateRequest;
import com.siemens.cto.aem.common.rule.group.GroupNameRule;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.persistence.service.group.GroupPersistenceService;
import com.siemens.cto.aem.service.group.GroupService;
import com.siemens.cto.aem.service.state.GroupStateService;
import com.siemens.cto.aem.service.state.StateNotificationWorker;
import com.siemens.cto.aem.service.webserver.WebServerService;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

public class GroupServiceImpl implements GroupService {

    private final GroupPersistenceService groupPersistenceService;
    private final WebServerService webServerService;
    private final GroupStateService.API groupStateService;
    private final StateNotificationWorker stateNotificationWorker;

    public GroupServiceImpl(final GroupPersistenceService theGroupPersistenceService,
                            final WebServerService wSService,
                            final GroupStateService.API groupStateService,
                            final StateNotificationWorker stateNotificationWorker) {
        groupPersistenceService = theGroupPersistenceService;
        webServerService = wSService;
        this.groupStateService = groupStateService;
        this.stateNotificationWorker = stateNotificationWorker;
    }

    @Override
    @Transactional
    public Group createGroup(final CreateGroupRequest aCreateGroupCommand,
                             final User aCreatingUser) {

        aCreateGroupCommand.validate();

        return groupPersistenceService.createGroup(createEvent(aCreateGroupCommand,
                                                               aCreatingUser));
    }

    @Override
    @Transactional(readOnly = true)
    public Group getGroup(final Identifier<Group> aGroupId) {
        return groupPersistenceService.getGroup(aGroupId);
    }

    @Override
    @Transactional(readOnly = true)
    public Group getGroupWithWebServers(Identifier<Group> aGroupId) {
        return groupPersistenceService.getGroupWithWebServers(aGroupId);
    }

    @Override
    @Transactional(readOnly = true)
    public Group getGroup(final String name) {
        return groupPersistenceService.getGroup(name);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Group> getGroups() {
        return groupPersistenceService.getGroups();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Group> getGroups(final boolean fetchWebServers) {
        return groupPersistenceService.getGroups(fetchWebServers);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Group> findGroups(final String aGroupNameFragment) {

        new GroupNameRule(aGroupNameFragment).validate();
        return groupPersistenceService.findGroups(aGroupNameFragment);
    }

    @Override
    @Transactional
    public Group updateGroup(final UpdateGroupRequest anUpdateGroupCommand,
                             final User anUpdatingUser) {

        anUpdateGroupCommand.validate();
        Group group = groupPersistenceService.updateGroup(
                createEvent(anUpdateGroupCommand, anUpdatingUser));

        // TODO: Remove if this is no londer needed.
        // stateNotificationWorker.refreshState(groupStateService, group);
        return group;
    }

    @Override
    @Transactional
    public void removeGroup(final Identifier<Group> aGroupId) {
        groupPersistenceService.removeGroup(aGroupId);
    }

    @Override
    @Transactional
    public void removeGroup(final String name) {
        groupPersistenceService.removeGroup(name);
    }

    @Override
    @Transactional
    public Group addJvmToGroup(final AddJvmToGroupRequest aCommand,
                               final User anAddingUser) {

        aCommand.validate();
        Group group = groupPersistenceService.addJvmToGroup(createEvent(aCommand,
                                                                 anAddingUser));
        // TODO: Remove if this is no londer needed.
        // stateNotificationWorker.refreshState(groupStateService, group);
        return group;
    }

    @Override
    @Transactional
    public Group addJvmsToGroup(final AddJvmsToGroupRequest aCommand,
                                final User anAddingUser) {

        aCommand.validate();
        for (final AddJvmToGroupRequest command : aCommand.toCommands()) {
            addJvmToGroup(command,
                          anAddingUser);
        }

        Group group = getGroup(aCommand.getGroupId());

        // TODO: Remove if this is no londer needed.
        // stateNotificationWorker.refreshState(groupStateService, group);
        return group;
    }

    @Override
    @Transactional
    public Group removeJvmFromGroup(final RemoveJvmFromGroupRequest aCommand,
                                    final User aRemovingUser) {

        aCommand.validate();
        Group group = groupPersistenceService.removeJvmFromGroup(createEvent(aCommand,
                                                                      aRemovingUser));
        // TODO: Remove if this is no londer needed.
        // stateNotificationWorker.refreshState(groupStateService, group);
        return group;
    }

    @Override
    @Transactional
    public List<Jvm> getOtherGroupingDetailsOfJvms(Identifier<Group> id) {
        final List<Jvm> otherGroupConnectionDetails = new LinkedList<>();
        final Group group = groupPersistenceService.getGroup(id, false);
        final Set<Jvm> jvms = group.getJvms();

        for (Jvm jvm : jvms) {
            final Set<LiteGroup> tmpGroup = new LinkedHashSet<>();
            if (jvm.getGroups() != null && !jvm.getGroups().isEmpty()) {
                for (LiteGroup liteGroup : jvm.getGroups()) {
                    if (!id.getId().equals(liteGroup.getId().getId())) {
                        tmpGroup.add(liteGroup);
                    }
                }
                if (!tmpGroup.isEmpty()) {
                    otherGroupConnectionDetails.add(new Jvm(jvm.getId(), jvm.getJvmName(), tmpGroup));
                }
            }
        }
        return otherGroupConnectionDetails;
    }

    @Override
    @Transactional
    public List<WebServer> getOtherGroupingDetailsOfWebServers(Identifier<Group> id) {
        final List<WebServer> otherGroupConnectionDetails = new ArrayList<>();
        final Group group = groupPersistenceService.getGroup(id, true);
        final Set<WebServer> webServers = group.getWebServers();

        for (WebServer webServer: webServers) {
            final Set<Group> tmpGroup = new LinkedHashSet<>();
            if (webServer.getGroups() != null && !webServer.getGroups().isEmpty()) {
                for (Group webServerGroup : webServer.getGroups()) {
                    if (!id.getId().equals(webServerGroup.getId().getId())) {
                        tmpGroup.add(webServerGroup);
                    }
                }
                if (!tmpGroup.isEmpty()) {
                    otherGroupConnectionDetails.add(new WebServer(webServer.getId(),
                                                        webServer.getGroups(),
                                                        webServer.getName()));
                }
            }
        }

        return otherGroupConnectionDetails;
    }

    @Override
    @Transactional
    public Group populateJvmConfig(Identifier<Group> aGroupId, List<UploadJvmTemplateRequest> uploadJvmTemplateCommands, User user, boolean overwriteExisting) {
        return groupPersistenceService.populateJvmConfig(aGroupId, uploadJvmTemplateCommands, user, overwriteExisting);
    }

    @Override
    @Transactional
    public Group populateWebServerConfig(Identifier<Group> aGroupId, List<UploadWebServerTemplateRequest> uploadWSTemplateCommands, User user, boolean overwriteExisting) {
        webServerService.populateWebServerConfig(uploadWSTemplateCommands, user, overwriteExisting);
        return groupPersistenceService.getGroup(aGroupId);
    }

    protected <T> Event<T> createEvent(final T aCommand,
                                       final User aUser) {
        return new Event<T>(aCommand,
                            AuditEvent.now(aUser));
    }
}
