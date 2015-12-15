package com.siemens.cto.aem.persistence.dao;

import com.siemens.cto.aem.common.request.group.UpdateGroupRequest;
import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.common.domain.model.event.Event;
import com.siemens.cto.aem.common.request.group.CreateGroupRequest;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;

import java.util.List;

public interface GroupDao {

    Group createGroup(final Event<CreateGroupRequest> aGroupToCreate);

    Group updateGroup(final Event<UpdateGroupRequest> aGroupToUpdate);

    Group getGroup(final Identifier<Group> aGroupId) throws NotFoundException;

    List<Group> getGroups();

    List<Group> findGroups(final String aName);

    void removeGroup(final Identifier<Group> aGroupId);
}
