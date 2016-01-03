package com.siemens.cto.aem.persistence.service;

import com.siemens.cto.aem.common.request.group.AddJvmToGroupRequest;
import com.siemens.cto.aem.common.request.group.CreateGroupRequest;
import com.siemens.cto.aem.common.request.group.RemoveJvmFromGroupRequest;
import com.siemens.cto.aem.common.request.group.UpdateGroupRequest;
import com.siemens.cto.aem.common.domain.model.group.*;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;

public class CommonGroupPersistenceServiceBehavior {

    private final GroupPersistenceService groupPersistenceService;

    public CommonGroupPersistenceServiceBehavior(final GroupPersistenceService theGroupService) {
        groupPersistenceService = theGroupService;
    }

    public Group createGroup(final String aGroupName, final String aUserId) {
        return groupPersistenceService.createGroup(new CreateGroupRequest(aGroupName));
    }

    public Group updateGroup(final Identifier<Group> aGroupId,
                                final String aNewGroupName,
                                final String aUserId) {

        return groupPersistenceService.updateGroup(new UpdateGroupRequest(aGroupId, aNewGroupName));
    }

    public void addJvmToGroup(final Identifier<Group> aGroupId,
                              final Identifier<Jvm> aJvmId,
                              final String aUserId) {

        groupPersistenceService.addJvmToGroup(new AddJvmToGroupRequest(aGroupId, aJvmId));
    }

    public void removeJvmFromGroup(final Identifier<Group> aGroupId,
                                      final Identifier<Jvm> aJvmId,
                                      final String aUserId) {

        groupPersistenceService.removeJvmFromGroup(new RemoveJvmFromGroupRequest(aGroupId, aJvmId));
    }

}
