package com.siemens.cto.aem.persistence.dao.group;

import java.util.List;

import com.siemens.cto.aem.domain.model.group.CreateGroup;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.UpdateGroup;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;

public interface GroupDao {

    Group createGroup(final CreateGroup aGroupToCreate);

    Group updateGroup(final UpdateGroup aGroupToUpdate);

    Group getGroup(final Identifier<Group> aGroupId);

    List<Group> getGroups(final PaginationParameter somePagination);

    List<Group> findGroups(final String aName,
                           final PaginationParameter somePagination);

    void removeGroup(final Identifier<Group> aGroupId);

}
