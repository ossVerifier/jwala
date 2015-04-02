package com.siemens.cto.aem.persistence.service.group;

import java.util.List;

import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.group.AddJvmToGroupCommand;
import com.siemens.cto.aem.domain.model.group.CreateGroupCommand;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.GroupState;
import com.siemens.cto.aem.domain.model.group.RemoveJvmFromGroupCommand;
import com.siemens.cto.aem.domain.model.group.UpdateGroupCommand;
import com.siemens.cto.aem.domain.model.group.command.SetGroupStateCommand;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.persistence.service.state.StatePersistenceService;

public interface GroupPersistenceService extends StatePersistenceService<Group, GroupState> {

    Group createGroup(final Event<CreateGroupCommand> anEvent);

    Group updateGroup(final Event<UpdateGroupCommand> anEvent) throws NotFoundException;

    Group getGroup(final Identifier<Group> aGroupId) throws NotFoundException;

    Group getGroup(final String name) throws NotFoundException;

    List<Group> getGroups(final PaginationParameter somePagination);

    List<Group> findGroups(final String aName,
                           final PaginationParameter somePagination);

    void removeGroup(final Identifier<Group> aGroupId) throws NotFoundException;

    Group addJvmToGroup(final Event<AddJvmToGroupCommand> anEvent) throws NotFoundException;

    Group removeJvmFromGroup(final Event<RemoveJvmFromGroupCommand> anEvent) throws NotFoundException;

    Group updateGroupStatus(Event<SetGroupStateCommand> aGroupToUpdate);

    public Group getGroup(final Identifier<Group> aGroupId, final boolean fetchWebServers) throws NotFoundException;
}
