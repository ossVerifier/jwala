package com.siemens.cto.aem.persistence.dao.group;

import java.util.List;

import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.domain.model.group.CreateGroupEvent;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.UpdateGroupEvent;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;

public interface GroupDao {

    Group createGroup(final CreateGroupEvent aGroupToCreate);

    Group updateGroup(final UpdateGroupEvent aGroupToUpdate);

    Group getGroup(final Identifier<Group> aGroupId) throws NotFoundException;

    List<Group> getGroups(final PaginationParameter somePagination);

    List<Group> findGroups(final String aName,
                           final PaginationParameter somePagination);

    void removeGroup(final Identifier<Group> aGroupId);

}
