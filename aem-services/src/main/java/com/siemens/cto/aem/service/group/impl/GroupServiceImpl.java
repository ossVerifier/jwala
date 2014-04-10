package com.siemens.cto.aem.service.group.impl;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.group.AddJvmToGroupCommand;
import com.siemens.cto.aem.domain.model.group.AddJvmsToGroupCommand;
import com.siemens.cto.aem.domain.model.group.CreateGroupCommand;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.RemoveJvmFromGroupCommand;
import com.siemens.cto.aem.domain.model.group.UpdateGroupCommand;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.rule.group.GroupNameRule;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.persistence.service.group.GroupPersistenceService;
import com.siemens.cto.aem.service.group.GroupService;

public class GroupServiceImpl implements GroupService {

    private final GroupPersistenceService groupPersistenceService;

    public GroupServiceImpl(final GroupPersistenceService theGroupPersistenceService) {
        groupPersistenceService = theGroupPersistenceService;
    }

    @Override
    @Transactional
    public Group createGroup(final CreateGroupCommand aCreateGroupCommand,
                             final User aCreatingUser) {

        aCreateGroupCommand.validateCommand();

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
    public List<Group> getGroups(final PaginationParameter aPaginationParam) {
        return groupPersistenceService.getGroups(aPaginationParam);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Group> findGroups(final String aGroupNameFragment,
                                  final PaginationParameter aPaginationParam) {

        new GroupNameRule(aGroupNameFragment).validate();
        return groupPersistenceService.findGroups(aGroupNameFragment,
                                                  aPaginationParam);
    }

    @Override
    @Transactional
    public Group updateGroup(final UpdateGroupCommand anUpdateGroupCommand,
                             final User anUpdatingUser) {

        anUpdateGroupCommand.validateCommand();
        return groupPersistenceService.updateGroup(createEvent(anUpdateGroupCommand,
                                                               anUpdatingUser));
    }

    @Override
    @Transactional
    public void removeGroup(final Identifier<Group> aGroupId) {
        groupPersistenceService.removeGroup(aGroupId);
    }

    @Override
    @Transactional
    public Group addJvmToGroup(final AddJvmToGroupCommand aCommand,
                               final User anAddingUser) {

        aCommand.validateCommand();
        return groupPersistenceService.addJvmToGroup(createEvent(aCommand,
                                                                 anAddingUser));
    }

    @Override
    @Transactional
    public Group addJvmsToGroup(final AddJvmsToGroupCommand aCommand,
                                final User anAddingUser) {

        aCommand.validateCommand();
        for (final AddJvmToGroupCommand command : aCommand.toCommands()) {
            addJvmToGroup(command,
                          anAddingUser);
        }

        return getGroup(aCommand.getGroupId());
    }

    @Override
    @Transactional
    public Group removeJvmFromGroup(final RemoveJvmFromGroupCommand aCommand,
                                    final User aRemovingUser) {

        aCommand.validateCommand();
        return groupPersistenceService.removeJvmFromGroup(createEvent(aCommand,
                                                                      aRemovingUser));
    }

    protected <T> Event<T> createEvent(final T aCommand,
                                       final User aUser) {
        return new Event<T>(aCommand,
                            AuditEvent.now(aUser));
    }
}
