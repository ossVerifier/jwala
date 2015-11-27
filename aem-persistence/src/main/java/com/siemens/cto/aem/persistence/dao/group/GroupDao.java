package com.siemens.cto.aem.persistence.dao.group;

import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.command.group.CreateGroupCommand;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.command.group.UpdateGroupCommand;
import com.siemens.cto.aem.domain.model.id.Identifier;

import java.util.List;

public interface GroupDao {

    Group createGroup(final Event<CreateGroupCommand> aGroupToCreate);

    Group updateGroup(final Event<UpdateGroupCommand> aGroupToUpdate);

    Group getGroup(final Identifier<Group> aGroupId) throws NotFoundException;

    List<Group> getGroups();

    List<Group> findGroups(final String aName);

    void removeGroup(final Identifier<Group> aGroupId);
}
