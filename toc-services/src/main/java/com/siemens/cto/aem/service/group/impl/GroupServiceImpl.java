package com.siemens.cto.aem.service.group.impl;

import com.siemens.cto.aem.common.request.group.*;
import com.siemens.cto.aem.common.request.webserver.UploadWebServerTemplateRequest;
import com.siemens.cto.aem.common.domain.model.group.*;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.request.jvm.UploadJvmTemplateRequest;
import com.siemens.cto.aem.common.rule.group.GroupNameRule;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.persistence.service.GroupPersistenceService;
import com.siemens.cto.aem.service.group.GroupService;
import com.siemens.cto.aem.service.state.GroupStateService;
import com.siemens.cto.aem.service.webserver.WebServerService;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

public class GroupServiceImpl implements GroupService {

    private final GroupPersistenceService groupPersistenceService;
    private final WebServerService webServerService;
    private final GroupStateService.API groupStateService;

    public GroupServiceImpl(final GroupPersistenceService theGroupPersistenceService,
                            final WebServerService wSService,
                            final GroupStateService.API groupStateService) {
        groupPersistenceService = theGroupPersistenceService;
        webServerService = wSService;
        this.groupStateService = groupStateService;
    }

    @Override
    @Transactional
    public Group createGroup(final CreateGroupRequest createGroupRequest,
                             final User aCreatingUser) {

        createGroupRequest.validate();

        return groupPersistenceService.createGroup(createGroupRequest);
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
    public Group updateGroup(final UpdateGroupRequest anUpdateGroupRequest,
                             final User anUpdatingUser) {

        anUpdateGroupRequest.validate();
        Group group = groupPersistenceService.updateGroup(anUpdateGroupRequest);

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
    public Group addJvmToGroup(final AddJvmToGroupRequest addJvmToGroupRequest,
                               final User anAddingUser) {

        addJvmToGroupRequest.validate();
        Group group = groupPersistenceService.addJvmToGroup(addJvmToGroupRequest);
        // TODO: Remove if this is no londer needed.
        // stateNotificationWorker.refreshState(groupStateService, group);
        return group;
    }

    @Override
    @Transactional
    public Group addJvmsToGroup(final AddJvmsToGroupRequest addJvmsToGroupRequest,
                                final User anAddingUser) {

        addJvmsToGroupRequest.validate();
        for (final AddJvmToGroupRequest command : addJvmsToGroupRequest.toRequests()) {
            addJvmToGroup(command,
                          anAddingUser);
        }

        Group group = getGroup(addJvmsToGroupRequest.getGroupId());

        // TODO: Remove if this is no londer needed.
        // stateNotificationWorker.refreshState(groupStateService, group);
        return group;
    }

    @Override
    @Transactional
    public Group removeJvmFromGroup(final RemoveJvmFromGroupRequest removeJvmFromGroupRequest,
                                    final User aRemovingUser) {

        removeJvmFromGroupRequest.validate();
        Group group = groupPersistenceService.removeJvmFromGroup(removeJvmFromGroupRequest);
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
            final Set<Group> tmpGroup = new LinkedHashSet<>();
            if (jvm.getGroups() != null && !jvm.getGroups().isEmpty()) {
                for (Group liteGroup : jvm.getGroups()) {
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
    public Group populateJvmConfig(Identifier<Group> aGroupId, List<UploadJvmTemplateRequest> uploadJvmTemplateRequests, User user, boolean overwriteExisting) {
        return groupPersistenceService.populateJvmConfig(aGroupId, uploadJvmTemplateRequests, user, overwriteExisting);
    }

    @Override
    @Transactional
    public Group populateWebServerConfig(Identifier<Group> aGroupId, List<UploadWebServerTemplateRequest> uploadWSTemplateCommands, User user, boolean overwriteExisting) {
        webServerService.populateWebServerConfig(uploadWSTemplateCommands, user, overwriteExisting);
        return groupPersistenceService.getGroup(aGroupId);
    }
}
