package com.siemens.cto.aem.persistence.jpa.service.group;

import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.group.CreateGroupCommand;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.GroupState;
import com.siemens.cto.aem.domain.model.group.UpdateGroupCommand;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.state.command.SetStateCommand;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;

import java.util.List;

public interface GroupCrudService {

    JpaGroup createGroup(final Event<CreateGroupCommand> aGroupToCreate);

    void updateGroup(final Event<UpdateGroupCommand> aGroupToUpdate);

    JpaGroup getGroup(final Identifier<Group> aGroupId) throws NotFoundException;

    JpaGroup getGroup(final String name) throws NotFoundException;

    List<JpaGroup> getGroups();

    List<JpaGroup> findGroups(final String aName);

    void removeGroup(final Identifier<Group> aGroupId);
    
    JpaGroup updateGroupStatus(Event<SetStateCommand<Group, GroupState>> aGroupToUpdate);

    Long getGroupId(String name);

}
