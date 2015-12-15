package com.siemens.cto.aem.persistence.service.group.impl;

import com.siemens.cto.aem.common.request.jvm.UploadJvmTemplateRequest;
import com.siemens.cto.aem.common.request.state.SetStateRequest;
import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.common.request.group.*;
import com.siemens.cto.aem.common.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.common.domain.model.event.Event;
import com.siemens.cto.aem.common.domain.model.group.*;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.state.StateType;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.persistence.dao.WebServerDao;
import com.siemens.cto.aem.persistence.dao.impl.JpaWebServerDaoImpl;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;
import com.siemens.cto.aem.persistence.jpa.domain.builder.JpaGroupBuilder;
import com.siemens.cto.aem.persistence.jpa.service.GroupCrudService;
import com.siemens.cto.aem.persistence.jpa.service.GroupJvmRelationshipService;
import com.siemens.cto.aem.persistence.service.group.GroupPersistenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class JpaGroupPersistenceServiceImpl implements GroupPersistenceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JpaGroupPersistenceServiceImpl.class);
    private final GroupCrudService groupCrudService;
    private final GroupJvmRelationshipService groupJvmRelationshipService;
    private final WebServerDao webServerDao;

    public JpaGroupPersistenceServiceImpl(final GroupCrudService theGroupCrudService,
                                          final GroupJvmRelationshipService theGroupJvmRelationshipService) {
        groupCrudService = theGroupCrudService;
        groupJvmRelationshipService = theGroupJvmRelationshipService;
        webServerDao = new JpaWebServerDaoImpl();
    }

    @Override
    public Group createGroup(final Event<CreateGroupRequest> anEvent) {
        final JpaGroup group = groupCrudService.createGroup(anEvent);
        return groupFrom(group, false);
    }

    @Override
    public Group updateGroup(final Event<UpdateGroupRequest> anEvent) throws NotFoundException {
        groupCrudService.updateGroup(anEvent);
        return groupFrom(groupCrudService.getGroup(anEvent.getRequest().getId()), false);
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
    public Group addJvmToGroup(final Event<AddJvmToGroupRequest> anEvent) throws NotFoundException {
        groupJvmRelationshipService.addJvmToGroup(anEvent);
        return groupFrom(groupCrudService.getGroup(anEvent.getRequest().getGroupId()), false);
    }

    @Override
    public Group removeJvmFromGroup(final Event<RemoveJvmFromGroupRequest> anEvent) throws NotFoundException {
        groupJvmRelationshipService.removeJvmFromGroup(anEvent);
        return groupFrom(groupCrudService.getGroup(anEvent.getRequest().getGroupId()), false);
    }

    protected Group groupFrom(final JpaGroup aJpaGroup, final boolean fetchWebServers) {
        return new JpaGroupBuilder(aJpaGroup).setFetchWebServers(fetchWebServers).build();
    }

    protected Group groupFrom(final CurrentState<Group, GroupState> originalStatus, final JpaGroup aJpaGroup) {
        if(originalStatus instanceof CurrentGroupState) {
            return new JpaGroupBuilder(aJpaGroup).setStateDetail((CurrentGroupState)originalStatus).build();
        } else {
            return new JpaGroupBuilder(aJpaGroup).build();
        }
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
    public CurrentState<Group, GroupState> updateState(Event<SetStateRequest<Group, GroupState>> aNewState) {
        return groupStateFrom(groupCrudService.updateGroupStatus(aNewState));
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
    public Group updateGroupStatus(Event<SetGroupStateRequest> aGroupToUpdate) {
        LOGGER.debug("Persisting new state " + aGroupToUpdate.getRequest());
        return groupFrom(aGroupToUpdate.getRequest().getNewState(), groupCrudService.updateGroupStatus(Event.<SetStateRequest<Group, GroupState>>create(aGroupToUpdate.getRequest(), aGroupToUpdate.getAuditEvent())));
    }

    @Override
    public List<CurrentState<Group, GroupState>> markStaleStates(StateType stateType, GroupState staleState,
            Date cutoff, AuditEvent auditData) {
        throw new UnsupportedOperationException("Group stale state not implemented, supported or necessary.");
    }
    @Override
    public List<CurrentState<Group, GroupState>> markStaleStates(StateType stateType, GroupState staleState,
            Collection<GroupState> statesToCheck,
            Date cutoff, AuditEvent auditData) {
        throw new UnsupportedOperationException("Group stale state not implemented, supported or necessary.");
    }

}
