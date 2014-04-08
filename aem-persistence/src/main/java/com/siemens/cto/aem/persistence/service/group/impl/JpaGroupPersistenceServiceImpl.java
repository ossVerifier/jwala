package com.siemens.cto.aem.persistence.service.group.impl;

import java.util.ArrayList;
import java.util.List;

import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.group.AddJvmToGroupCommand;
import com.siemens.cto.aem.domain.model.group.CreateGroupCommand;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.RemoveJvmFromGroupCommand;
import com.siemens.cto.aem.domain.model.group.UpdateGroupCommand;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;
import com.siemens.cto.aem.persistence.jpa.domain.builder.JpaGroupBuilder;
import com.siemens.cto.aem.persistence.jpa.service.group.GroupCrudService;
import com.siemens.cto.aem.persistence.jpa.service.groupjvm.GroupJvmRelationshipService;
import com.siemens.cto.aem.persistence.service.group.GroupPersistenceService;

public class JpaGroupPersistenceServiceImpl implements GroupPersistenceService {

    private final GroupCrudService groupCrudService;
    private final GroupJvmRelationshipService groupJvmRelationshipService;

    public JpaGroupPersistenceServiceImpl(final GroupCrudService theGroupCrudService,
                                          final GroupJvmRelationshipService theGroupJvmRelationshipService) {
        groupCrudService = theGroupCrudService;
        groupJvmRelationshipService = theGroupJvmRelationshipService;
    }

    @Override
    public Group createGroup(final Event<CreateGroupCommand> anEvent) {

        final JpaGroup group = groupCrudService.createGroup(anEvent);

        return groupFrom(group);
    }

    @Override
    public Group updateGroup(final Event<UpdateGroupCommand> anEvent) throws NotFoundException {
        groupCrudService.updateGroup(anEvent);
        return groupFrom(groupCrudService.getGroup(anEvent.getCommand().getId()));
    }

    @Override
    public Group getGroup(final Identifier<Group> aGroupId) throws NotFoundException {

        final JpaGroup group = groupCrudService.getGroup(aGroupId);

        return groupFrom(group);
    }

    @Override
    public List<Group> getGroups(final PaginationParameter somePagination) {

        final List<JpaGroup> groups = groupCrudService.getGroups(somePagination);

        return groupsFrom(groups);
    }

    @Override
    public List<Group> findGroups(final String aName,
                                  final PaginationParameter somePagination) {

        final List<JpaGroup> groups = groupCrudService.findGroups(aName,
                                                                  somePagination);

        return groupsFrom(groups);
    }

    @Override
    public void removeGroup(final Identifier<Group> aGroupId) throws NotFoundException {
        groupJvmRelationshipService.removeRelationshipsForGroup(aGroupId);
        groupCrudService.removeGroup(aGroupId);
    }

    @Override
    public Group addJvmToGroup(final Event<AddJvmToGroupCommand> anEvent) throws NotFoundException {
        groupJvmRelationshipService.addJvmToGroup(anEvent);
        return groupFrom(groupCrudService.getGroup(anEvent.getCommand().getGroupId()));
    }

    @Override
    public Group removeJvmFromGroup(final Event<RemoveJvmFromGroupCommand> anEvent) throws NotFoundException {
        groupJvmRelationshipService.removeJvmFromGroup(anEvent);
        return groupFrom(groupCrudService.getGroup(anEvent.getCommand().getGroupId()));
    }

    protected Group groupFrom(final JpaGroup aJpaGroup) {
        return new JpaGroupBuilder(aJpaGroup).build();
    }

    protected List<Group> groupsFrom(final List<JpaGroup> someJpaGroups) {
        final List<Group> groups = new ArrayList<>();
        for (final JpaGroup jpaGroup : someJpaGroups) {
            groups.add(groupFrom(jpaGroup));
        }
        return groups;
    }

}
