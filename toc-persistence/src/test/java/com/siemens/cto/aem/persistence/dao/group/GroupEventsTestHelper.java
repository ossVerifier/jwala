package com.siemens.cto.aem.persistence.dao.group;

import com.siemens.cto.aem.common.request.group.CreateGroupRequest;
import com.siemens.cto.aem.common.request.group.UpdateGroupRequest;
import com.siemens.cto.aem.common.domain.model.event.Event;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.persistence.dao.AuditTestHelper;

public class GroupEventsTestHelper {

	

    public static Event<CreateGroupRequest> createCreateGroupEvent(final String aGroupName,
                                                               final String aUserId) {

        final Event<CreateGroupRequest> createGroup = new Event<>(new CreateGroupRequest(aGroupName),
        										AuditTestHelper.createAuditEvent(aUserId));

        return createGroup;
    }

    public static Event<UpdateGroupRequest> createUpdateGroupEvent(final Identifier<Group> aGroupId,
                                                               final String aNewGroupName,
                                                               final String aUserId) {

        final Event<UpdateGroupRequest> updateGroup = new Event<>(new UpdateGroupRequest(aGroupId,
                                                                     aNewGroupName),
                                              AuditTestHelper.createAuditEvent(aUserId));

        return updateGroup;
    }

}
