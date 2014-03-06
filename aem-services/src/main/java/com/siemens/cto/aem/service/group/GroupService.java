package com.siemens.cto.aem.service.group;

import java.util.List;

import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.domain.model.temporary.User;

public interface GroupService {

    Group createGroup(final String aNewGroupName,
                      final User aCreatingUser);

    Group getGroup(final Identifier<Group> aGroupId);

    List<Group> getGroups(final PaginationParameter aPaginationParam);

    List<Group> findGroups(final String aGroupNameFragment,
                           final PaginationParameter aPaginationParam);

    void removeGroup(final Identifier<Group> aGroupId);
}
