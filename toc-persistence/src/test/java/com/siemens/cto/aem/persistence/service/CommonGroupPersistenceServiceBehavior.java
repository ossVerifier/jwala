package com.siemens.cto.aem.persistence.service;

import com.siemens.cto.aem.request.group.AddJvmToGroupRequest;
import com.siemens.cto.aem.request.group.CreateGroupRequest;
import com.siemens.cto.aem.request.group.RemoveJvmFromGroupRequest;
import com.siemens.cto.aem.request.group.UpdateGroupRequest;
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

        final Event<CreateGroupRequest> event = createCreateGroupEvent(aGroupName,
                                                                       aUserId);

        return groupPersistenceService.createGroup(event);
    }

    public Group updateGroup(final Identifier<Group> aGroupId,
                                final String aNewGroupName,
                                final String aUserId) {

        final Event<UpdateGroupRequest> event = createUpdateGroupEvent(aGroupId,
                                                                       aNewGroupName,
                                                                       aUserId);

        return groupPersistenceService.updateGroup(event);
    }

    public void addJvmToGroup(final Identifier<Group> aGroupId,
                              final Identifier<Jvm> aJvmId,
                              final String aUserId) {

        final Event<AddJvmToGroupRequest> event = createAddJvmToGroupEvent(aGroupId,
                                                                           aJvmId,
                                                                           aUserId);

        groupPersistenceService.addJvmToGroup(event);
    }

    public void removeJvmFromGroup(final Identifier<Group> aGroupId,
                                      final Identifier<Jvm> aJvmId,
                                      final String aUserId) {

        final Event<RemoveJvmFromGroupRequest> event = createRemoveJvmFromGroupEvent(aGroupId,
                                                                                     aJvmId,
                                                                                     aUserId);

        groupPersistenceService.removeJvmFromGroup(event);
    }

    protected Event<CreateGroupRequest> createCreateGroupEvent(final String aGroupName,
                                                               final String aUserId) {

        final Event<CreateGroupRequest> createGroup = new Event<>(new CreateGroupRequest(aGroupName),
                                                                  createAuditEvent(aUserId));

        return createGroup;
    }

    protected Event<UpdateGroupRequest> createUpdateGroupEvent(final Identifier<Group> aGroupId,
                                                               final String aNewGroupName,
                                                               final String aUserId) {

        final Event<UpdateGroupRequest> updateGroup = new Event<>(new UpdateGroupRequest(aGroupId,
                                                                                         aNewGroupName),
                                                                  createAuditEvent(aUserId));

        return updateGroup;
    }

    protected Event<AddJvmToGroupRequest> createAddJvmToGroupEvent(final Identifier<Group> aGroupId,
                                                                   final Identifier<Jvm> aJvmId,
                                                                   final String aUserId) {

        final Event<AddJvmToGroupRequest> addJvm = new Event<>(new AddJvmToGroupRequest(aGroupId,
                                                                                        aJvmId),
                                                               createAuditEvent(aUserId));

        return addJvm;
    }

    protected Event<RemoveJvmFromGroupRequest> createRemoveJvmFromGroupEvent(final Identifier<Group> aGroupId,
                                                                             final Identifier<Jvm> aJvmId,
                                                                             final String aUserId) {

        final Event<RemoveJvmFromGroupRequest> removeJvm = new Event<>(new RemoveJvmFromGroupRequest(aGroupId,
                                                                                                     aJvmId),
                                                                       createAuditEvent(aUserId));

        return removeJvm;
    }

    protected AuditEvent createAuditEvent(final String aUserId) {
        return AuditEvent.now(new User(aUserId));
    }
}
