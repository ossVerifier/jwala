package com.siemens.cto.aem.persistence.jpa.service.group;

import com.siemens.cto.aem.common.request.group.CreateGroupRequest;
import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.common.domain.model.event.Event;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.group.GroupState;
import com.siemens.cto.aem.common.request.group.UpdateGroupRequest;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.request.state.SetStateRequest;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;

import java.util.List;

public interface GroupCrudService {

    JpaGroup createGroup(final Event<CreateGroupRequest> aGroupToCreate);

    void updateGroup(final Event<UpdateGroupRequest> aGroupToUpdate);

    JpaGroup getGroup(final Identifier<Group> aGroupId) throws NotFoundException;

    JpaGroup getGroup(final String name) throws NotFoundException;

    List<JpaGroup> getGroups();

    List<JpaGroup> findGroups(final String aName);

    void removeGroup(final Identifier<Group> aGroupId);
    
    JpaGroup updateGroupStatus(Event<SetStateRequest<Group, GroupState>> aGroupToUpdate);

    Long getGroupId(String name);

}
