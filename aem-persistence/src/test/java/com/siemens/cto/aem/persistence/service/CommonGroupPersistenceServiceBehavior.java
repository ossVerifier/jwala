package com.siemens.cto.aem.persistence.service;

import com.siemens.cto.aem.domain.command.group.AddJvmToGroupCommand;
import com.siemens.cto.aem.domain.command.group.CreateGroupCommand;
import com.siemens.cto.aem.domain.command.group.RemoveJvmFromGroupCommand;
import com.siemens.cto.aem.domain.command.group.UpdateGroupCommand;
import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.group.*;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.user.User;
import com.siemens.cto.aem.persistence.service.group.GroupPersistenceService;

public class CommonGroupPersistenceServiceBehavior {

    private final GroupPersistenceService groupPersistenceService;

    public CommonGroupPersistenceServiceBehavior(final GroupPersistenceService theGroupService) {
        groupPersistenceService = theGroupService;
    }

    public Group createGroup(final String aGroupName,
                                final String aUserId) {

        final Event<CreateGroupCommand> event = createCreateGroupEvent(aGroupName,
                                                                       aUserId);

        return groupPersistenceService.createGroup(event);
    }

    public Group updateGroup(final Identifier<Group> aGroupId,
                                final String aNewGroupName,
                                final String aUserId) {

        final Event<UpdateGroupCommand> event = createUpdateGroupEvent(aGroupId,
                                                                       aNewGroupName,
                                                                       aUserId);

        return groupPersistenceService.updateGroup(event);
    }

    public void addJvmToGroup(final Identifier<Group> aGroupId,
                              final Identifier<Jvm> aJvmId,
                              final String aUserId) {

        final Event<AddJvmToGroupCommand> event = createAddJvmToGroupEvent(aGroupId,
                                                                           aJvmId,
                                                                           aUserId);

        groupPersistenceService.addJvmToGroup(event);
    }

    public void removeJvmFromGroup(final Identifier<Group> aGroupId,
                                      final Identifier<Jvm> aJvmId,
                                      final String aUserId) {

        final Event<RemoveJvmFromGroupCommand> event = createRemoveJvmFromGroupEvent(aGroupId,
                                                                                     aJvmId,
                                                                                     aUserId);

        groupPersistenceService.removeJvmFromGroup(event);
    }

    protected Event<CreateGroupCommand> createCreateGroupEvent(final String aGroupName,
                                                               final String aUserId) {

        final Event<CreateGroupCommand> createGroup = new Event<>(new CreateGroupCommand(aGroupName),
                                                                  createAuditEvent(aUserId));

        return createGroup;
    }

    protected Event<UpdateGroupCommand> createUpdateGroupEvent(final Identifier<Group> aGroupId,
                                                               final String aNewGroupName,
                                                               final String aUserId) {

        final Event<UpdateGroupCommand> updateGroup = new Event<>(new UpdateGroupCommand(aGroupId,
                                                                                         aNewGroupName),
                                                                  createAuditEvent(aUserId));

        return updateGroup;
    }

    protected Event<AddJvmToGroupCommand> createAddJvmToGroupEvent(final Identifier<Group> aGroupId,
                                                                   final Identifier<Jvm> aJvmId,
                                                                   final String aUserId) {

        final Event<AddJvmToGroupCommand> addJvm = new Event<>(new AddJvmToGroupCommand(aGroupId,
                                                                                        aJvmId),
                                                               createAuditEvent(aUserId));

        return addJvm;
    }

    protected Event<RemoveJvmFromGroupCommand> createRemoveJvmFromGroupEvent(final Identifier<Group> aGroupId,
                                                                             final Identifier<Jvm> aJvmId,
                                                                             final String aUserId) {

        final Event<RemoveJvmFromGroupCommand> removeJvm = new Event<>(new RemoveJvmFromGroupCommand(aGroupId,
                                                                                                     aJvmId),
                                                                       createAuditEvent(aUserId));

        return removeJvm;
    }

    protected AuditEvent createAuditEvent(final String aUserId) {
        return AuditEvent.now(new User(aUserId));
    }
}
