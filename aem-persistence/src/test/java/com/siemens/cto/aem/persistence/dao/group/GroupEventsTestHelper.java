package com.siemens.cto.aem.persistence.dao.group;

import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.command.group.CreateGroupCommand;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.command.group.UpdateGroupCommand;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.persistence.dao.AuditTestHelper;

public class GroupEventsTestHelper {

	

    public static Event<CreateGroupCommand> createCreateGroupEvent(final String aGroupName,
                                                               final String aUserId) {

        final Event<CreateGroupCommand> createGroup = new Event<>(new CreateGroupCommand(aGroupName),
        										AuditTestHelper.createAuditEvent(aUserId));

        return createGroup;
    }

    public static Event<UpdateGroupCommand> createUpdateGroupEvent(final Identifier<Group> aGroupId,
                                                               final String aNewGroupName,
                                                               final String aUserId) {

        final Event<UpdateGroupCommand> updateGroup = new Event<>(new UpdateGroupCommand(aGroupId,
                                                                     aNewGroupName),
                                              AuditTestHelper.createAuditEvent(aUserId));

        return updateGroup;
    }

}
