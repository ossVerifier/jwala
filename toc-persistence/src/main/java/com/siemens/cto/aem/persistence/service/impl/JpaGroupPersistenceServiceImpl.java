package com.siemens.cto.aem.persistence.service.impl;

import com.siemens.cto.aem.common.request.jvm.UploadJvmTemplateRequest;
import com.siemens.cto.aem.common.request.state.SetStateRequest;
import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.common.request.group.*;
import com.siemens.cto.aem.common.domain.model.group.*;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.request.webserver.UploadWebServerTemplateRequest;
import com.siemens.cto.aem.persistence.jpa.service.WebServerCrudService;
import com.siemens.cto.aem.persistence.jpa.service.impl.WebServerCrudServiceImpl;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;
import com.siemens.cto.aem.persistence.jpa.domain.builder.JpaGroupBuilder;
import com.siemens.cto.aem.persistence.jpa.service.GroupCrudService;
import com.siemens.cto.aem.persistence.jpa.service.GroupJvmRelationshipService;
import com.siemens.cto.aem.persistence.service.GroupPersistenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class JpaGroupPersistenceServiceImpl implements GroupPersistenceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JpaGroupPersistenceServiceImpl.class);
    private final GroupCrudService groupCrudService;
    private final GroupJvmRelationshipService groupJvmRelationshipService;
    private final WebServerCrudService webServerCrudService;

    public JpaGroupPersistenceServiceImpl(final GroupCrudService theGroupCrudService,
                                          final GroupJvmRelationshipService theGroupJvmRelationshipService) {
        groupCrudService = theGroupCrudService;
        groupJvmRelationshipService = theGroupJvmRelationshipService;
        webServerCrudService = new WebServerCrudServiceImpl();
    }

    @Override
    public Group createGroup(final CreateGroupRequest createGroupRequest) {
        final JpaGroup group = groupCrudService.createGroup(createGroupRequest);
        return groupFrom(group, false);
    }

    @Override
    public Group updateGroup(UpdateGroupRequest updateGroupRequest) throws NotFoundException {
        groupCrudService.updateGroup(updateGroupRequest);
        return groupFrom(groupCrudService.getGroup(updateGroupRequest.getId()), false);
    }

    @Override
    public Group getGroup(final Identifier<Group> aGroupId) throws NotFoundException {
        final JpaGroup group = groupCrudService.getGroup(aGroupId);
        return groupFrom(group, false);
    }

    @Override
    public Group getGroupWithWebServers(final Identifier<Group> aGroupId) throws NotFoundException {
        final JpaGroup group = groupCrudService.getGroup(aGroupId);
        return groupFrom(group, true);
    }

    @Override
    public Group getGroup(final String name) throws NotFoundException {
        final JpaGroup group = groupCrudService.getGroup(name);
        return groupFrom(group, false);
    }

    @Override
    public Group getGroup(final Identifier<Group> aGroupId, final boolean fetchWebServers) throws NotFoundException {
        final JpaGroup group = groupCrudService.getGroup(aGroupId);
        return new JpaGroupBuilder(group).setFetchWebServers(fetchWebServers).build();
    }

    @Override
    public List<Group> getGroups() {
        final List<JpaGroup> groups = groupCrudService.getGroups();
        return groupsFrom(groups, false);
    }

    @Override
    public List<Group> getGroups(boolean fetchWebServers) {
        final List<JpaGroup> groups = groupCrudService.getGroups();
        return groupsFrom(groups, fetchWebServers);
    }

    @Override
    public List<Group> findGroups(final String aName) {
        final List<JpaGroup> groups = groupCrudService.findGroups(aName);
        return groupsFrom(groups, false);
    }

    @Override
    public void removeGroup(final Identifier<Group> aGroupId) throws NotFoundException {
        groupJvmRelationshipService.removeRelationshipsForGroup(aGroupId);
        groupCrudService.removeGroup(aGroupId);
    }

    @Override
    public void removeGroup(final String name) throws NotFoundException {
        removeGroup(new Identifier<Group>(groupCrudService.getGroupId(name)));
    }

    @Override
    public Group addJvmToGroup(AddJvmToGroupRequest addJvmToGroupRequest) throws NotFoundException {
        groupJvmRelationshipService.addJvmToGroup(addJvmToGroupRequest);
        return groupFrom(groupCrudService.getGroup(addJvmToGroupRequest.getGroupId()), false);
    }

    @Override
    public Group removeJvmFromGroup(RemoveJvmFromGroupRequest removeJvmFromGroupRequest) throws NotFoundException {
        groupJvmRelationshipService.removeJvmFromGroup(removeJvmFromGroupRequest);
        return groupFrom(groupCrudService.getGroup(removeJvmFromGroupRequest.getGroupId()), false);
    }

    protected Group groupFrom(final JpaGroup aJpaGroup, final boolean fetchWebServers) {
        return new JpaGroupBuilder(aJpaGroup).setFetchWebServers(fetchWebServers).build();
    }

    protected Group groupFrom(final CurrentState<Group, GroupState> originalStatus, final JpaGroup aJpaGroup) {
        return new JpaGroupBuilder(aJpaGroup).setStateDetail((CurrentGroupState) originalStatus).build();
    }
    protected CurrentState<Group, GroupState> groupStateFrom(final JpaGroup aJpaGroup) {
        return new JpaGroupBuilder(aJpaGroup).build().getCurrentState();
    }

    protected List<Group> groupsFrom(final List<JpaGroup> someJpaGroups, final boolean fetchWebServers) {
        final List<Group> groups = new ArrayList<>();
        for (final JpaGroup jpaGroup : someJpaGroups) {
            groups.add(groupFrom(jpaGroup, fetchWebServers));
        }
        return groups;
    }

    protected Set<CurrentState<Group, GroupState>> groupStatesFrom(final List<JpaGroup> someJpaGroups) {
        final Set<CurrentState<Group, GroupState>> groupStates = new HashSet<>();
        for (final JpaGroup jpaGroup : someJpaGroups) {
            groupStates.add(groupStateFrom(jpaGroup));
        }
        return groupStates;
    }

    @Override
    public CurrentState<Group, GroupState> updateState(SetStateRequest<Group, GroupState> setStateRequest) {
        return groupStateFrom(groupCrudService.updateGroupStatus(setStateRequest));
    }

    @Override
    public CurrentState<Group, GroupState> getState(Identifier<Group> anId) {
        return groupStateFrom(groupCrudService.getGroup(anId));
    }

    @Override
    public Set<CurrentState<Group, GroupState>> getAllKnownStates() {
        final List<JpaGroup> groups = groupCrudService.getGroups();
        return groupStatesFrom(groups);
    }

    @Override
    public Group populateJvmConfig(Identifier<Group> aGroupId, List<UploadJvmTemplateRequest> uploadJvmTemplateCommands, User user, boolean overwriteExisting) {
        groupJvmRelationshipService.populateJvmConfig(uploadJvmTemplateCommands, user, overwriteExisting);
        return groupFrom(groupCrudService.getGroup(aGroupId), false);
    }

    @Override
    public Group updateGroupStatus(SetGroupStateRequest setGroupStateRequest) {
        LOGGER.debug("Persisting new state " + setGroupStateRequest);
        return groupFrom(setGroupStateRequest.getNewState(), groupCrudService.updateGroupStatus(setGroupStateRequest));
    }

    @Override
    public Group populateGroupJvmTemplates(String groupName, List<UploadJvmTemplateRequest> uploadJvmTemplateRequests, User user) {
        final JpaGroup group = groupCrudService.getGroup(groupName);
        for (UploadJvmTemplateRequest uploadRequest : uploadJvmTemplateRequests) {
            groupCrudService.uploadGroupJvmTemplate(uploadRequest, group);
        }
        return groupFrom(group, false);
    }

    @Override
    public Group populateGroupWebServerTemplates(String groupName, List<UploadWebServerTemplateRequest> uploadWSTemplateRequests, User user) {
        final JpaGroup group = groupCrudService.getGroup(groupName);
        for (UploadWebServerTemplateRequest uploadRequest : uploadWSTemplateRequests) {
            groupCrudService.uploadGroupWebServerTemplate(uploadRequest, group);
        }
        return groupFrom(group, false);
    }

    @Override
    public List<String> getGroupJvmsResourceTemplateNames(String groupName) {
        return groupCrudService.getGroupJvmsResourceTemplateNames(groupName);
    }

    @Override
    public List<String> getGroupWebServersResourceTemplateNames(String groupName) {
        return groupCrudService.getGroupWebServersResourceTemplateNames(groupName);
    }
}
