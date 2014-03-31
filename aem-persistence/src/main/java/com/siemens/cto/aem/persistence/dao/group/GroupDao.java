package com.siemens.cto.aem.persistence.dao.group;

import java.util.List;

import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.group.CreateGroupCommand;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.UpdateGroupCommand;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;

public interface GroupDao {

    Group createGroup(final Event<CreateGroupCommand> aGroupToCreate);

    Group updateGroup(final Event<UpdateGroupCommand> aGroupToUpdate);

    Group getGroup(final Identifier<Group> aGroupId) throws NotFoundException;

    List<Group> getGroups(final PaginationParameter somePagination);

    List<Group> findGroups(final String aName,
                           final PaginationParameter somePagination);

    void removeGroup(final Identifier<Group> aGroupId);

    Group getGroup(final String aGroupName) throws NotFoundException;

}
